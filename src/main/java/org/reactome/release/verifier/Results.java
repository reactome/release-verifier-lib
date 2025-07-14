package org.reactome.release.verifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/25/2024
 */
public class Results {
    private List<String> infoMessages;
    private List<String> errorMessages;

    public Results() {
        this.infoMessages = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
    }

    public void reportInfoMessages() {
        System.out.println("Info Messages:");
        System.out.println();
        for (String infoMessage : this.infoMessages) {
            System.out.println(infoMessage);
        }
        System.out.println();
    }

    public void reportErrors() {
        System.err.println("Error Messages:");
        System.err.println();
        for (String errorMessage : this.errorMessages) {
            System.err.println(errorMessage);
        }
        System.err.println();
    }

    public boolean hasInfoMessages() {
        return this.infoMessages != null && !this.infoMessages.isEmpty();
    }

    public boolean hasErrors() {
        return this.errorMessages != null && !this.errorMessages.isEmpty();
    }

    public List<String> getInfoMessages() {
        return this.infoMessages;
    }

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    public void addInfoMessages(List<String> infoMessages) {
        if (infoMessages == null) {
            return;
        }

        for (String infoMessage : infoMessages) {
            addInfoMessage(infoMessage);
        }
    }

    public void addInfoMessage(String infoMessage) {
        this.infoMessages.add(infoMessage);
    }

    public void addErrorMessages(List<String> errorMessages) {
        if (errorMessages == null) {
            return;
        }

        for (String errorMessage : errorMessages) {
            addErrorMessage(errorMessage);
        }
    }

    public void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }
}
