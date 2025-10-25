package com.company.sharefile.dto.v1.records;


public record QuotaInfo(
        Long usedStorageBytes,
        Long totalStorageBytes,
        Long availableStorageBytes,
        Double usedPercentage,
        String usedStorageFormatted,
        String totalStorageFormatted,
        String availableStorageFormatted,
        String planType,
        Boolean isStorageAlmostFull,
        Boolean isStorageFull
) {


    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String prefix = "KMGTPE".charAt(exp - 1) + "";

        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), prefix);
    }


    public static QuotaInfo from(
            long usedBytes,
            long totalBytes,
            String planType
    ) {
        long availableBytes = Math.max(0, totalBytes - usedBytes);
        double usedPercentage = totalBytes > 0 ?
                (double) usedBytes / totalBytes * 100.0 : 0.0;

        return new QuotaInfo(
                usedBytes,
                totalBytes,
                availableBytes,
                usedPercentage,
                formatBytes(usedBytes),
                formatBytes(totalBytes),
                formatBytes(availableBytes),
                planType,
                usedPercentage >= 80.0,
                usedBytes >= totalBytes
        );
    }
}