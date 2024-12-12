package org.reactome.release.verifier;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/20/2024
 */
public class FileUtils {

    /**
     * Deletes a directory and its contents.  If the directory does not exist, this method does nothing.
     *
     * @param path Path to directory to delete
     * @throws IOException Thrown if unable to walk contents of directory to delete
     */
    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Extracts a gzipped tar file to a given location.
     *
     * @param tgzFilePath Path to the tar gzipped file to extract
     * @param outputDir Path to the location of where to extract
     * @throws IOException Thrown if unable to gunzip or extract the tar file
     */
    public static void untarTgzFile(String tgzFilePath, String outputDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(tgzFilePath);
             GzipCompressorInputStream gis = new GzipCompressorInputStream(fis);
             TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {

            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + outputFile);
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = tis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    /**
     * Decompresses a gzipped file by writing its contents to a new file of the same name without the .gz extension.
     * If a null value is passed, the method does nothing.
     *
     * @param filePath Path to gzipped file
     * @throws IOException Thrown if unable to read or write files
     */
    public static void gunzipFile(Path filePath) throws IOException {
        if (filePath == null) {
            return;
        }

        String outputFilePath = filePath.toString().replace(".gz","");

        try (
            GZIPInputStream gzipInputStream =
                new GZIPInputStream(new BufferedInputStream(new FileInputStream(filePath.toString())));
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(new FileOutputStream(outputFilePath));
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Downloads a folder's contents from S3 bucket to a folder named according to the folder key's name
     *
     * @param bucketName Name of S3 bucket
     * @param folderKey Path to folder
     */
    public static void downloadFolderFromS3(String bucketName, String folderKey) {
        S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(folderKey)
            .build();

        ListObjectsV2Response listResponse;
        do {
            listResponse = s3.listObjectsV2(listRequest);
            List<S3Object> objects = listResponse.contents();

            List<S3Object> filteredObjects = objects.stream()
                .filter(s3Object -> !s3Object.key().endsWith("/"))
                .collect(Collectors.toList());

            for (S3Object s3Object : filteredObjects) {
                downloadFile(s3, bucketName, s3Object.key(), folderKey);
            }

            listRequest = listRequest.toBuilder()
                .continuationToken(listResponse.nextContinuationToken())
                .build();
        } while (listResponse.isTruncated());
    }

    /**
     * Downloads list of files from S3 bucket and saved as local files of the same names
     *
     * @param bucketName Name of S3 bucket
     * @param fileKeys List of paths to files
     * @throws S3Exception Thrown if problem accessing S3 files
     */
    public static void downloadFilesFromS3(String bucketName, List<String> fileKeys) throws S3Exception {
        for (String fileKey : fileKeys) {
            downloadFileFromS3(bucketName, fileKey);
        }
    }

    /**
     * Downloads file from S3 bucket and saved as a local file of the same name
     *
     * @param bucketName Name of S3 bucket
     * @param fileKey Path to file
     * @throws S3Exception Thrown if problem accessing S3 files
     */
    public static void downloadFileFromS3(String bucketName, String fileKey) throws S3Exception {
        S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();

        Path fileKeyPath = Paths.get(fileKey);
        s3.getObject(
            GetObjectRequest.builder().bucket(bucketName).key(fileKey).build(),
            ResponseTransformer.toFile(fileKeyPath.getFileName())
        );
        s3.close();
    }

    private static void downloadFile(S3Client s3, String bucketName, String key, String folderKey) {
        try {
            String folderName = Paths.get(folderKey).getFileName().toString();
            Path localFilePath = Paths.get(folderName, key.substring(folderKey.length()));

            localFilePath.getParent().toFile().mkdirs();

            s3.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build(),
                ResponseTransformer.toFile(localFilePath)
            );
        } catch (Exception e) {
            System.err.println("Failed to download file: " + key);
            e.printStackTrace();
        }
    }
}
