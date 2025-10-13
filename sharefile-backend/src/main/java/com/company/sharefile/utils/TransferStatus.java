package com.company.sharefile.utils;

public enum TransferStatus {
    PENDING("In attesa", false),

    UPLOADING("Caricamento in corso", false),

    COMPLETED("Completato", true),

    EXPIRED("Scaduto", true),

    CANCELLED("Cancellato", true),

    FAILED("Fallito", true);

    private final String displayName;
    private final boolean terminal;

    TransferStatus(String displayName, boolean terminal) {
        this.displayName = displayName;
        this.terminal = terminal;
    }

    public String getDisplayName() {
        return displayName;
    }

}
