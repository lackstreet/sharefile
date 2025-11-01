package com.company.sharefile.config;

/**
 * Costanti per validazioni, pattern regex e valori statici
 * utilizzati in tutto il sistema ShareFile
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Utility class - prevent instantiation
    }

    // ========== EMAIL PATTERNS ==========
    public static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static final String EMAIL_MESSAGE = "Email must be valid";

    // ========== USERNAME PATTERNS ==========
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_.-]{3,50}$";
    public static final String USERNAME_MESSAGE =
            "Username must be 3-50 characters long and contain only letters, numbers, dots, underscores and dashes";

    // ========== PASSWORD PATTERNS ==========
    public static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String PASSWORD_MESSAGE =
            "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                    "one lowercase letter, one number and one special character";

    // ========== IP ADDRESS PATTERNS ==========
    public static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static final String IPV6_PATTERN =
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$";

    // ========== FILE PATTERNS ==========
    public static final String FILENAME_PATTERN = "^[^<>:\"/\\|?*\\x00-\\x1f]+$";
    public static final String FILENAME_MESSAGE =
            "Filename contains invalid characters";

    public static final String SAFE_FILENAME_PATTERN = "^[a-zA-Z0-9._-]+$";
    public static final String SAFE_FILENAME_MESSAGE =
            "Filename must contain only letters, numbers, dots, underscores and dashes";

    // ========== LINK TOKEN PATTERNS ==========
    public static final String LINK_TOKEN_PATTERN = "^[a-zA-Z0-9]{16,32}$";
    public static final String LINK_TOKEN_MESSAGE =
            "Link token must be 16-32 alphanumeric characters";

    // ========== KEYCLOAK ID PATTERN ==========
    public static final String KEYCLOAK_ID_PATTERN = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    public static final String KEYCLOAK_ID_MESSAGE = "Keycloak ID must be a valid UUID format";

    // ========== FIELD LENGTH LIMITS ==========
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_FIRST_NAME_LENGTH = 100;
    public static final int MAX_LAST_NAME_LENGTH = 100;
    public static final int MAX_FULL_NAME_LENGTH = 255;
    public static final int MAX_COMPANY_LENGTH = 255;
    public static final int MAX_DEPARTMENT_LENGTH = 255;

    public static final int MAX_FILENAME_LENGTH = 512;
    public static final int MAX_STORED_FILENAME_LENGTH = 255;
    public static final int MAX_FILE_PATH_LENGTH = 1024;
    public static final int MAX_MIME_TYPE_LENGTH = 255;
    public static final int MAX_CHECKSUM_LENGTH = 64;

    public static final int MAX_LINK_TOKEN_LENGTH = 32;
    public static final int MAX_LINK_TITLE_LENGTH = 255;
    public static final int MAX_LINK_DESCRIPTION_LENGTH = 2048;
    public static final int MAX_SENDER_MESSAGE_LENGTH = 2048;
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    public static final int MAX_IP_ADDRESS_LENGTH = 45; // IPv6 max length
    public static final int MAX_VIRUS_SCAN_RESULT_LENGTH = 50;

    // ========== FILE SIZE LIMITS ==========
    public static final long MIN_FILE_SIZE = 1L; // 1 byte
    public static final long MAX_FILE_SIZE_FREE = 2L * 1024 * 1024 * 1024; // 2GB
    public static final long MAX_FILE_SIZE_PREMIUM = 10L * 1024 * 1024 * 1024; // 10GB

    // ========== STORAGE LIMITS ==========
    public static final long DEFAULT_STORAGE_QUOTA = 5L * 1024 * 1024 * 1024; // 5GB
    public static final long PREMIUM_STORAGE_QUOTA = 100L * 1024 * 1024 * 1024; // 100GB

    // ========== SHARED LINK LIMITS ==========
    public static final int DEFAULT_LINK_EXPIRATION_DAYS = 7;
    public static final int MAX_LINK_EXPIRATION_DAYS = 30;
    public static final int MIN_DOWNLOAD_LIMIT = 1;
    public static final int MAX_DOWNLOAD_LIMIT = 1000;
    public static final int MAX_LINKS_PER_USER = 100;

    // ========== ALLOWED MIME TYPES ==========
    public static final String[] ALLOWED_MIME_TYPES = {
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",

            // Images
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",

            // Audio
            "audio/mpeg",
            "audio/wav",
            "audio/ogg",

            // Video
            "video/mp4",
            "video/webm",
            "video/ogg",

            // Archives
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "application/gzip"
    };

    // ========== BLOCKED FILE EXTENSIONS ==========
    public static final String[] BLOCKED_EXTENSIONS = {
            ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar", ".app",
            ".deb", ".pkg", ".rpm", ".dmg", ".iso", ".msi", ".sh", ".ps1"
    };

    // ========== VALIDATION MESSAGES ==========
    public static final String NOT_BLANK_MESSAGE = "Field cannot be blank";
    public static final String NOT_NULL_MESSAGE = "Field cannot be null";
    public static final String INVALID_UUID_MESSAGE = "Invalid UUID format";
    public static final String FILE_TOO_LARGE_MESSAGE = "File size exceeds maximum allowed limit";
    public static final String UNSUPPORTED_FILE_TYPE_MESSAGE = "File type not supported";
    public static final String QUOTA_EXCEEDED_MESSAGE = "Storage quota exceeded";
    public static final String LINK_EXPIRED_MESSAGE = "Share link has expired";
    public static final String LINK_DOWNLOAD_LIMIT_REACHED_MESSAGE = "Download limit reached for this link";
    public static final String INVALID_PASSWORD_MESSAGE = "Invalid password for protected link";

    // ========== SYSTEM DEFAULTS ==========
    public static final String DEFAULT_VIRUS_SCAN_RESULT = "PENDING";
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final int LINK_TOKEN_LENGTH = 32;
    public static final int CLEANUP_BATCH_SIZE = 100;
    public static final int RANDOM_STRING_LENGTH = 16;

    // Timezone
    public static final String DEFAULT_TIMEZONE = "UTC";

    // Date formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String ROLE_ADMIN = "sharefile_admin";
    public static final String ROLE_USER = "sharefile_user";
    public static final String ROLE_ADMIN_USER = "sharefile_admin,sharefile_user";
    public static final String ROLE_ALL = "sharefile_user";
}