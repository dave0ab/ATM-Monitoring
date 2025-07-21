package com.zemenbank.atmMonitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ATMAccessResponseDTO {
    private Long id;
    private String terminalId;
    private String userId;
    private String status;
    private LocalDateTime createdAt;
}
