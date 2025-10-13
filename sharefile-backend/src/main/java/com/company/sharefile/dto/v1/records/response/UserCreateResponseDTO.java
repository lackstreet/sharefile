package com.company.sharefile.dto.v1.records.response;


import java.util.UUID;

public record UserCreateResponseDTO(
     UUID id,
     String email,
     String username,
     String firstName,
     String lastName
){}
