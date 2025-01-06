package org.reactome.release.verifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.reactome.release.verifier.FileUtils.getExpectedFileNameToSizeMap;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 11/24/2024
 */
public class TooSmallFile {
    private Path currentFileNamePath;
    private long expectedFileSizeInBytes;
    private long actualFileSizeInBytes;

    public TooSmallFile(Path currentFileNamePath) throws IOException {
        this.currentFileNamePath = currentFileNamePath;
        this.actualFileSizeInBytes = getCurrentFileSize(currentFileNamePath);
        this.expectedFileSizeInBytes = getExpectedFileSize(currentFileNamePath);
    }

    public static boolean currentFileIsSmaller(Path currentFileNamePath) throws IOException {
        long actualFileSizeInBytes = getCurrentFileSize(currentFileNamePath);
        long expectedFileSizeInBytes = getExpectedFileSize(currentFileNamePath);

        return actualFileSizeInBytes < expectedFileSizeInBytes;
    }

    @Override
    public String toString() {
        return String.format("%s (expected %d bytes but got %d bytes - decrease of %d bytes (%.2f%%))",
            getCurrentFileNamePath(),
            getExpectedFileSizeInBytes(),
            getActualFileSizeInBytes(),
            getDifferenceInFileSize(),
            getPercentDifferenceInFileSize()
        );
    }

    private Path getCurrentFileNamePath() {
        return this.currentFileNamePath;
    }

    private long getExpectedFileSizeInBytes() {
        return this.expectedFileSizeInBytes;
    }

    private long getActualFileSizeInBytes() {
        return this.actualFileSizeInBytes;
    }

    private long getDifferenceInFileSize() {
        return getExpectedFileSizeInBytes() - getActualFileSizeInBytes();
    }

    private double getPercentDifferenceInFileSize() {
        return getDifferenceInFileSize() * 100d / getExpectedFileSizeInBytes();
    }

    private static long getCurrentFileSize(Path currentFileNamePath) {
        try {
            return Files.size(currentFileNamePath);
        } catch (IOException e) {
            // TODO: Add logger (warn?) statement for file that could not be sized
            return 0L;
        }
    }

    private static long getExpectedFileSize(Path currentFileNamePath) throws IOException {
        long expectedFileSize = getExpectedFileNameToSizeMap().computeIfAbsent(
            currentFileNamePath.getFileName().toString(), k -> 0L);
        //System.out.println(currentFileNamePath + "\t" + expectedFileSize);
        return expectedFileSize;
    }
}
