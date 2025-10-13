package com.company.sharefile.utils;

public enum PlanType {
    FREE("Free", 1L * 1024 * 1024 * 1024),      // 1 GB
    BASIC("Base", 5L * 1024 * 1024 * 1024),         // 5 GB
    PREMIUM("Premium", 20L * 1024 * 1024 * 1024),   // 20 GB
    ENTERPRISE("Aziendale", 100L * 1024 * 1024 * 1024); // 100 GB

    private final String displayName;
    private final Long defaultQuotaBytes;

    PlanType(String displayName, Long defaultQuotaBytes) {
        this.displayName = displayName;
        this.defaultQuotaBytes = defaultQuotaBytes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getDefaultQuotaBytes() {
        return defaultQuotaBytes;
    }
}
