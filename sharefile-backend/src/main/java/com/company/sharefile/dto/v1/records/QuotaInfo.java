package com.company.sharefile.dto.v1.records;


public record QuotaInfo(
        Long totalBytes,
        Long usedBytes,
        Long availableBytes
) {
    public double usagePercentage() {
        return totalBytes > 0 ? (usedBytes * 100.0) / totalBytes : 0;
    }
}