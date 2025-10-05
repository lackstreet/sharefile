package com.company.sharefile.repository;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FileRepository implements PanacheRepository<FileEntity> {

    public FileEntity findById(UUID id) {
        return find("id", id).firstResult();
    }
    public FileEntity findByCheckSum(String checkSum) {
        return find("checksumSha256", checkSum).firstResult();
    }

    public FileEntity findByStoredFileName(String storedFileName){
        return find("storedFileName", storedFileName).firstResult();
    }

    public List<FileEntity> findByUser(UUID userId){
        return find("uploadedBy.id =?1 and isDeleted = false", userId).list();
    }

    public List<FileEntity> findDeletedFiles() {
        return list("isDeleted = true");
    }

    public UUID createDuplicateReference(UserEntity user, String originalFileName, String storedFileName, String mimeType, Long fileSize, String checksum, String uploadIpAddress) {
        FileEntity newFile = new FileEntity();
        newFile.setUploadedBy(user);
        newFile.setOriginalFileName(originalFileName);
        newFile.setStoredFileName(storedFileName);
        newFile.setMimeType(mimeType);
        newFile.setFileSizeBytes(fileSize);
        newFile.setChecksumSha256(checksum);
        newFile.setUploadIp(uploadIpAddress);
        newFile.setIsDeleted(false);
        persist(newFile);
        if(newFile.isPersistent())
            return newFile.getId();
        else throw new ApiException ("File record could not be created", Response.Status.INTERNAL_SERVER_ERROR, "LAM-500-001");
    }
}
