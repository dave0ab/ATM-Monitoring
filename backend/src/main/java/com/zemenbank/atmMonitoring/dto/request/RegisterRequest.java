
package com.zemenbank.atmMonitoring.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
	
	@NotBlank(message = "Domain ID is required")
	@Size(min = 3, max = 50, message = "Domain ID must be between 3 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Domain ID cannot contain spaces. Use only letters, numbers, dots, underscores, and hyphens (e.g., john.doe)")
	private String domainId;

	@NotBlank(message = "Role is required")
	@Pattern(regexp = "^(ADMIN|IT|BRANCH|OMNI_CHANNEL|OMNI_CHANNEL_RECONCILIATION)$", 
		message = "Role must be one of: ADMIN, IT, BRANCH, OMNI_CHANNEL, OMNI_CHANNEL_RECONCILIATION")
	private String role;

	@NotBlank(message = "Notification level is required")
	@Pattern(regexp = "^(FIRST|SECOND|THIRD|FOURTH|FIFTH)$", message = "Notification level must be FIRST, SECOND, THIRD, FOURTH, or FIFTH")
	private String notificationLevel;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^(\\+251[79]\\d{8}|0[79]\\d{8})$", 
		message = "Phone number must be Ethiopian format: +251XXXXXXXXX or 0XXXXXXXXX (starting with 07 or 09)")
	private String phoneNumber;

	@NotNull(message = "Email subscription preference is required")
	private boolean emailSubscribe;
}
