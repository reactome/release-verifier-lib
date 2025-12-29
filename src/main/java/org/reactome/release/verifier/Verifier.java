package org.reactome.release.verifier;

import java.io.IOException;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/27/2025
 */
public interface Verifier {

    ParsedArguments parseCommandLineArgs(String[] args);

    default void run() throws IOException {
        Results results = verifyStepRanCorrectly();
        if (!results.hasErrors()) {
            System.out.println("The " + getStepName() + " step ran correctly!");

            if (results.hasInfoMessages()) {
                results.reportInfoMessages();
            }
        } else {
            results.reportErrors();
            System.exit(1);
        }
    }

    List<CommandLineParameter> getCommandLineParameters();

    Results verifyStepRanCorrectly() throws IOException;

    String getStepName();
}
