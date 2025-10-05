package com.company.sharefile.utils;

import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserUtils {
    @Inject
    Logger log;

    public boolean hasAvailableQuota(@NotBlank UserEntity userEntity, Long fileSizeByte) {
        if(userEntity == null){
            log.warn("User entity is null during quota check.");
            throw new ApiException(
                    "User entity cannot be null.",
                    jakarta.ws.rs.core.Response.Status.BAD_REQUEST,
                    "LAM-400-001"
            );
        }
        if(fileSizeByte == null || fileSizeByte < 0){
            log.warnf("Invalid file size during quota check: %s, user: %s", fileSizeByte, userEntity.getId());
            throw new ApiException(
                    "UploadingFile size must be a non-negative value.",
                    jakarta.ws.rs.core.Response.Status.BAD_REQUEST,
                    "LAM-400-002"
            );
        }
        long available = userEntity.getStorageQuotaBytes() - userEntity.getUsedStorageBytes();
        boolean hasQuota = available >= fileSizeByte;

        log.debugf("Quota check for user %s: available=%d, requested=%d, result=%s",
                userEntity.getId(), available, fileSizeByte, hasQuota);

        return hasQuota;
    }
}
