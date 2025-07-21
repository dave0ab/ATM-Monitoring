package com.zemenbank.atmMonitoring.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Domain ID must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Domain ID cannot contain spaces. Use only letters, numbers, dots, underscores, and hyphens (e.g., john.doe)")
    private String domainId;
    
    @Pattern(regexp = "^(ADMIN|IT|BRANCH|OMNI_CHANNEL|OMNI_CHANNEL_RECONCILIATION)$", 
        message = "Role must be one of: ADMIN, IT, BRANCH, OMNI_CHANNEL, OMNI_CHANNEL_RECONCILIATION")
    private String role;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status;

    @Pattern(regexp = "^(\\+251[79]\\d{8}|0[79]\\d{8})$", 
        message = "Phone number must be Ethiopian format: +251XXXXXXXXX or 0XXXXXXXXX (starting with 07 or 09)")
    private String phoneNumber;
    
    @Pattern(regexp = "^(FIRST|SECOND|THIRD|FOURTH|FIFTH)$", message = "Notification level must be FIRST, SECOND, THIRD, FOURTH, or FIFTH")
    private String notificationLevel;

    private boolean emailSubscribe;
}
