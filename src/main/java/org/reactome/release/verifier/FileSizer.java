package org.reactome.release.verifier;

import java.io.IOException;
import java.nio.file.Path;

import static org.reactome.release.verifier.FileUtils.*;

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

    public static boolean currentFileTooSmall(Path currentFileNamePath, int dropTolerancePercentage)
        throws IOException {

        if (dropTolerancePercentage < 0 || dropTolerancePercentage > 100) {
            throw new IllegalStateException("Drop tolerance percentage must be between 0 and 100");
        }

        long actualFileSizeInBytes = getCurrentFileSize(currentFileNamePath);
        long expectedFileSizeInBytes = getExpectedFileSize(currentFileNamePath);

        long minimumAcceptableFileSizeInBytes = expectedFileSizeInBytes * ((100 - dropTolerancePercentage) / 100);

        return actualFileSizeInBytes < minimumAcceptableFileSizeInBytes;
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
}
