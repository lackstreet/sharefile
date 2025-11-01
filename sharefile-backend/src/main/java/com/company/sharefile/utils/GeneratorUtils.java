package com.company.sharefile.utils;

import com.company.sharefile.config.ValidationConstants;
import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.entity.TransferRecipientEntity;
import com.company.sharefile.exception.ApiException;
import jakarta.validation.Validation;
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;

public class GeneratorUtils {
    private static final String SAFE_CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateUniqueShareLink() {
        String shareLink;
        int attempts = 0;

        do {
            shareLink = generateRandomString(ValidationConstants.RANDOM_STRING_LENGTH);
            attempts++;

            if (attempts > 10) {
                throw new ApiException(
                        "Failed to generate unique share link",
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "LAM-500-001"
                );
            }
        } while (TransferEntity.find("shareLink", shareLink).firstResult() != null);

        return shareLink;
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SAFE_CHARS.charAt(RANDOM.nextInt(SAFE_CHARS.length())));
        }
        return sb.toString();
    }


    public static String generateSecureToken() {
        String token;
        int attempts = 0;

        do {
            token = generateRandomString(ValidationConstants.LINK_TOKEN_LENGTH);
            attempts++;

            if (attempts > 10) {
                throw new ApiException(
                        String.format("Failed to generate secure token after %d attempts", attempts),
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "LAM-500-002"
                );
            }
        } while (TransferRecipientEntity.find("accessToken", token).firstResult() != null);

        return token;
    }
}
