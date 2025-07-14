package org.reactome.release.verifier;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/27/2025
 */
public interface Verifier {

    void parseCommandLineArgs(String[] args);
    default void run() throws IOException {
        Results results = verifyStepRanCorrectly();
        if (!results.hasErrors()) {
            System.out.println(getStepName() + " ran correctly!");

            if (results.hasInfoMessages()) {
                results.reportInfoMessages();
            }
        } else {
            results.reportErrors();
            System.exit(1);
        }
    }
    Results verifyStepRanCorrectly() throws IOException;

    String getStepName();
}
