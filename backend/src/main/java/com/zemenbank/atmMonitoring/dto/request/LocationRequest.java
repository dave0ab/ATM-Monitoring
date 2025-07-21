package com.zemenbank.atmMonitoring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationRequest {
    private String TID;
    private String LO;
    private String LA;

    // Getters and setters
}
