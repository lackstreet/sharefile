package com.company.sharefile.dto.v1.records.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public record AuthenticationResponseDTO(
        @JsonProperty("access_token")
        String accessToken
) {}