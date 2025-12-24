package org.reactome.release.verifier;

import java.io.IOException;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/27/2025
 */
public class DefaultVerifier implements Verifier {

    private final DefaultVerificationLogic logic;
    private final String stepName;

    public DefaultVerifier(String stepName) {
        this.logic = new DefaultVerificationLogic(stepName);
        this.stepName = stepName;
    }

    @Override
    public ParsedArguments parseCommandLineArgs(String[] args) {
        return logic.parseCommandLineArgs(args, getCommandLineParameters());
    }

    @Override
    public List<CommandLineParameter> getCommandLineParameters() {
        return logic.defaultParameters();
    }

    @Override
    public Results verifyStepRanCorrectly() throws IOException {
        return logic.verify();
    }

    @Override
    public String getStepName() {
        return this.stepName;
    }
}
