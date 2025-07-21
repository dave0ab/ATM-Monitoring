package com.zemenbank.atmMonitoring.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("domain_id")
    private String domainId; // Optional: only included on success

    @JsonProperty("role")
    private String role; // Optional: only included on success

    @JsonProperty("error")
    private boolean error; // Indicates whether the operation was successful or not
}
