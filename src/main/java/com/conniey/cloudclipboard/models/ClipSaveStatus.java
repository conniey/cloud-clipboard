package com.conniey.cloudclipboard.models;

public class ClipSaveStatus {
    private final Boolean wasSuccessful;
    private final String status;

    public ClipSaveStatus() {
        wasSuccessful = null;
        status = "n/a";
    }

    public ClipSaveStatus(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
        this.status = wasSuccessful ? "Saved successfully! :)" : "Failed to save clip. :(";
    }

    public String getStatus() {
        return status;
    }

    public boolean isVisible() {
        return wasSuccessful != null;
    }

    public Boolean wasSaved() {
        return wasSuccessful;
    }
}
