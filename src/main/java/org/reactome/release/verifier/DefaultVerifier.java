package org.reactome.release.verifier;

import com.martiansoftware.jsap.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.reactome.release.verifier.FileUtils.downloadFileFromS3;
import static org.reactome.release.verifier.TooSmallFile.currentFileIsSmaller;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/27/2025
 */
public class DefaultVerifier implements Verifier {
    private String outputDirectory;
    private int releaseNumber;

    private String stepName;

    public DefaultVerifier(String stepName) {
        this.stepName = stepName;
    }

    @Override
    public void parseCommandLineArgs(String[] args) {
        SimpleJSAP jsap;
        try {
            jsap = new SimpleJSAP(Verifier.class.getName(), "Verify " + getStepName() + " ran correctly",
                new Parameter[]{
                    new FlaggedOption("output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'o', "output", "The folder where the results are written to."),
                    new FlaggedOption("releaseNumber", JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'r', "releaseNumber", "The most recent Reactome release version")
                }
            );
        } catch (JSAPException e) {
            throw new RuntimeException("Unable to create simple JSAP", e);
        }

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        this.outputDirectory = config.getString("output");
        this.releaseNumber = config.getInt("releaseNumber");
    }

    @Override
    public Results verifyStepRanCorrectly() throws IOException {
        Results results = new Results();

        results.addErrorMessages(verifyStepFolderExists());
        if (!results.hasErrors()) {
            results.addErrorMessages(verifyStepFilesExist());
            results.addErrorMessages(verifyStepFileSizesComparedToPreviousRelease());
        }
        return results;
    }

    @Override
    public String getStepName() {
        return this.stepName;
    }

    private List<String> verifyStepFolderExists() {
        return !Files.exists(Paths.get(this.outputDirectory)) ?
            Arrays.asList(this.outputDirectory + " does not exist; Expected " + getStepName() + " output files at this location") :
            new ArrayList<>();
    }

    private List<String> verifyStepFilesExist() throws IOException {
        List<String> errorMessages = new ArrayList<>();

        downloadFilesAndSizesListFromS3(getPreviousReleaseNumber());
        for (String fileName : getFileNames()) {
            Path filePath = Paths.get(this.outputDirectory, fileName);
            if (!Files.exists(filePath)) {
                errorMessages.add("File " + filePath + " does not exist");
            }
        }

        return errorMessages;
    }

    private List<String> verifyStepFileSizesComparedToPreviousRelease() throws IOException {
        downloadFilesAndSizesListFromS3(getPreviousReleaseNumber());
        List<TooSmallFile> tooSmallFiles = new ArrayList<>();
        for (String fileName : getFileNames()) {
            Path filePath = Paths.get(this.outputDirectory, fileName);

            if (Files.exists(filePath) && currentFileIsSmaller(filePath)) {
                tooSmallFiles.add(new TooSmallFile(filePath));
            }
        }

        return tooSmallFiles
            .stream()
            .map(TooSmallFile::toString)
            .collect(Collectors.toList());
    }

    private List<String> getFileNames() throws IOException {
        return Files.lines(Paths.get(getFilesAndSizesListName()))
            .map(this::getFileName)
            .collect(Collectors.toList());
    }

    private String getFileName(String line) {
        return line.split("\t")[1];
    }

    private int getPreviousReleaseNumber() {
        return this.releaseNumber - 1;
    }

    private void downloadFilesAndSizesListFromS3(int versionNumber) {
        if (Files.notExists(Paths.get(getFilesAndSizesListName()))) {
            downloadFileFromS3("reactome", getFilesAndSizesListPathInS3(versionNumber));
        }
    }

    private String getFilesAndSizesListPathInS3(int versionNumber) {
        return String.format("private/releases/%d/%s/data/%s",
            versionNumber, getStepName(), getFilesAndSizesListName()
        );
    }

    private String getFilesAndSizesListName() {
        return "files_and_sizes.txt";
    }
}
