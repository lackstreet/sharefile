package com.company.sharefile.mapper;

import com.company.sharefile.dto.v1.response.UserCreateResponseDTO;
import com.company.sharefile.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface UserMapper {
    UserCreateResponseDTO toCreateResponseDTO(UserEntity userEntity);
}
