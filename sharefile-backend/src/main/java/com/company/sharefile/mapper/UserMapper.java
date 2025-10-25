package com.company.sharefile.mapper;

import com.company.sharefile.dto.v1.records.QuotaInfo;
import com.company.sharefile.dto.v1.records.UserInfoDTO;
import com.company.sharefile.dto.v1.records.response.UserCreateResponseDTO;
import com.company.sharefile.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface UserMapper {
    UserCreateResponseDTO toCreateResponseDTO(UserEntity userEntity);
    UserInfoDTO toUserInfoDTO(UserEntity userEntity);
    public default QuotaInfo toQuotaInfo(UserEntity user) {
        return QuotaInfo.from(
                user.getUsedStorageBytes(),
                user.getStoragePlan().getStorageQuotaBytes(),
                user.getStoragePlan().getPlanType().name()
        );
    }
}
