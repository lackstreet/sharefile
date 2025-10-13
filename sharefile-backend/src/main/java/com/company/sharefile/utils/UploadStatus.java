package com.company.sharefile.utils;


public enum UploadStatus {

    PENDING("In attesa"),
    UPLOADING("Caricamento in corso"),
    COMPLETED("Completato"),
    FAILED("Fallito"),
    DEDUPLICATED("Deduplicato");

    private final String displayName;

    UploadStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    public boolean isUsable() {
        return this == COMPLETED || this == DEDUPLICATED;
    }
}