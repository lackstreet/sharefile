package com.company.sharefile.service;

import com.company.sharefile.exception.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class EncryptionService {
    @Inject
    Logger log;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public String generateKey(){
        try{
            log.info("Generating encryption key...");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();

            String encodedkey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            log.info("Encryption key generated successfully.");
            return encodedkey;
        }catch(Exception e){
            throw new ApiException(
                    String.format("Error generating encryption key: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }
    }

    public InputStream encrypt(InputStream inputStream, String encodedKey) {
        try{
            log.info("Encrypting data...");

            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec GCMspec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMspec);

            byte[] inputBytes = inputStream.readAllBytes();
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(iv);
            output.write(encryptedBytes);

            log.info("Data encrypted successfully.");
            return new ByteArrayInputStream(output.toByteArray());


        }catch(Exception e) {
            throw new ApiException(
                    String.format("Error encrypting data: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }

    public InputStream decrypt(InputStream input, String decodeKey){
        try {
            log.info("Decrypting data...");


            byte[] keyBytes = Base64.getDecoder().decode(decodeKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] allBytes = input.readAllBytes();
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(allBytes, 0, iv, 0, IV_SIZE);

            byte[] encryptedBytes = new byte[allBytes.length - IV_SIZE];
            System.arraycopy(allBytes, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec GCMspec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMspec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            log.info("Data decrypted successfully.");
            return new ByteArrayInputStream(decryptedBytes);
        }catch(Exception e) {
            throw new ApiException(
                    String.format("Error decrypting data: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-003"
            );
        }

    }

    public byte[] decrypt(byte[] encryptedData, @Size(max = 512, message = "Encryption key must be less than 512 characters") String encryptionKey) {
        try{
            log.infof("Decrypting data...");

            byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(encryptedData, 0, iv, 0, IV_SIZE);

            byte[] ciphertext = new byte[encryptedData.length - IV_SIZE];
            System.arraycopy(encryptedData, IV_SIZE, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec GCMspec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMspec);

            byte[] decryptedData = cipher.doFinal(ciphertext);

            log.infof("Decrypted successfully: %d bytes → %d bytes",
                    encryptedData.length, decryptedData.length);

            return decryptedData;


        }catch(Exception e){
            throw new ApiException(
                    String.format("Error decrypting data: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-003"
            );
        }
    }
}
