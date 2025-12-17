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

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/27/2025
 */
public class DefaultVerifier implements Verifier {
    private String outputDirectory;
    private int releaseNumber;
    private int fileSizePercentDropTolerance;

    private String stepName;

    public DefaultVerifier(String stepName) {
        this.stepName = stepName;
    }

    @Override
    public ParsedArguments parseCommandLineArgs(String[] args) {
        JSAPResult config = getCommandLineArgumentsConfig(args);

        this.outputDirectory = config.getString("output");
        this.releaseNumber = config.getInt("releaseNumber");
        this.fileSizePercentDropTolerance = config.getInt("sizeDropTolerance");

        return new JsapParsedArguments(config);
    }

    @Override
    public List<CommandLineParameter> getCommandLineParameters() {
        return Arrays.asList(
            CommandLineParameter.create("output", OptionType.STRING, "", CommandLineParameter.IS_REQUIRED, 'o', "output", "The folder to where the results are written"),
            CommandLineParameter.create("releaseNumber", OptionType.INTEGER, "", CommandLineParameter.IS_REQUIRED, 'r', "releaseNumber", "The most recent Reactome release version"),
            CommandLineParameter.create("sizeDropTolerance", OptionType.INTEGER, "10", CommandLineParameter.NOT_REQUIRED, 'd', "sizeDropTolerance", "The percentage drop in expected files' sizes allowed before an error is produced")
        );
    }

    @Override
    public Results verifyStepRanCorrectly() throws IOException {
        Results finalResults = new Results();

        finalResults.mergeResults(verifyStepFolderExists());
        if (!finalResults.hasErrors()) {
            finalResults.mergeResults(verifyStepFilesExist());
            finalResults.mergeResults(verifyStepFileSizesComparedToPreviousRelease());
        }

        return finalResults;
    }

    @Override
    public String getStepName() {
        return this.stepName;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    public int getReleaseNumber() {
        return this.releaseNumber;
    }

    private JSAPResult getCommandLineArgumentsConfig(String[] args) {
        Parameter[] parameters = getCommandLineParameters().stream()
            .map(JsapParameterMapper::toJsapParameter)
            .toArray(Parameter[]::new);

        SimpleJSAP jsap = prepareCommandLineArguments(parameters);
        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);
        return config;
    }

    private SimpleJSAP prepareCommandLineArguments(Parameter[] parameters) {
        SimpleJSAP jsap;
        try {
            jsap = new SimpleJSAP(Verifier.class.getName(), "Verify " + getStepName() + " ran correctly", parameters);
        } catch (JSAPException e) {
            throw new RuntimeException("Unable to create simple JSAP", e);
        }
        return jsap;
    }

    private Results verifyStepFolderExists() {
        List<String> errorMessages = !Files.exists(Paths.get(this.outputDirectory)) ?
            Arrays.asList(this.outputDirectory + " does not exist; " +
                "Expected " + getStepName() + " output files at this location") :
            new ArrayList<>();

        Results results = new Results();
        results.addErrorMessages(errorMessages);
        return results;
    }

    private Results verifyStepFilesExist() throws IOException {
        List<String> errorMessages = new ArrayList<>();

        downloadFilesAndSizesListFromS3(getPreviousReleaseNumber());
        for (String fileName : getFileNames()) {
            Path filePath = Paths.get(this.outputDirectory, fileName);
            if (!Files.exists(filePath)) {
                errorMessages.add("File " + filePath + " does not exist");
            }
        }

        Results results = new Results();
        results.addErrorMessages(errorMessages);
        return results;
    }

    private Results verifyStepFileSizesComparedToPreviousRelease() throws IOException {
        downloadFilesAndSizesListFromS3(getPreviousReleaseNumber());

        Results results = new Results();
        for (String fileName : getFileNames()) {
            Path filePath = Paths.get(this.outputDirectory, fileName);

            if (Files.exists(filePath)) {
                FileSizer fileSizer = new FileSizer(filePath);
                if (fileSizer.currentFileTooSmall(this.fileSizePercentDropTolerance)) {
                    results.addErrorMessage(fileSizer.toString());
                } else {
                    results.addInfoMessage(fileSizer.toString());
                }
            }
        }

        return results;
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
