package com.zemenbank.atmMonitoring.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ATMAccessRequestDTO {
    private String terminalId;
    private String userId;
    private String status; // Example: "GRANTED" or "REVOKED"
}
