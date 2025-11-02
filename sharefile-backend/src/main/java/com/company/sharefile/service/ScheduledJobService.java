package com.company.sharefile.service;

import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.utils.TransferStatus;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ScheduledJobService {
    @Inject
    Logger log;

    @ConfigProperty(name = "sharefile.jobs.cleanup-expired.enabled", defaultValue = "true")
    boolean cleanupExpiredEnabled;

    @Scheduled(
            cron = "${sharefile.jobs.cleanup-expired.cron:0 0 2 * * ?}",
            identity = "clean-expired-transfers"
    )
    @Transactional
    public void cleanupExpiredTransfers(){
        if (!cleanupExpiredEnabled) {
            log.debug("Cleanup expired transfers job is disabled");
            return;
        }

        log.info("Starting cleanup of expired transfers...");

        long startTime = System.currentTimeMillis();

        try {
            // Trova transfer scaduti e ancora attivi
            List<TransferEntity> expiredTransfers = TransferEntity.find(
                    "expiresAt < ?1 AND status != ?2 AND isExpired = false",
                    LocalDateTime.now(),
                    TransferStatus.EXPIRED
            ).list();

            log.infof("Found %d expired transfers to cleanup", expiredTransfers.size());

            int cleaned = 0;
            long totalStorageFreed = 0L;

            for (TransferEntity transfer : expiredTransfers) {
                try {
                    // Marca come scaduto
                    transfer.setIsExpired(true);
                    transfer.setStatus(TransferStatus.EXPIRED);
                    transfer.persist();
                    cleaned++;

                } catch (Exception e) {
                    log.errorf(e, "Failed to cleanup transfer: %s", transfer.getId());
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            log.infof("Cleanup completed: %d transfers expired (took %dms)",
                    cleaned, duration);

        } catch (Exception e) {
            log.errorf(e, "Error in cleanup expired transfers job");
        }
    }


}
