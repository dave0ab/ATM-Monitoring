package com.zemenbank.atmMonitoring.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String domainId;
    private String role;
    private String status;

    private String phoneNumber;
    private String notificationLevel;

    private String createdAt;
    private String lastSignOn;
    private boolean emailSubscribe;
}

