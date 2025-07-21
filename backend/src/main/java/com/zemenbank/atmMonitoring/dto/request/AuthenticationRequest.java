package com.zemenbank.atmMonitoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

	@Schema(
        description = "domain ID"
    )
	@NotBlank
	private String domain;
	@NotBlank
	private String userPass;
}