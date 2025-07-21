package com.zemenbank.atmMonitoring.dto.request;



import lombok.Data;

@Data
public class TipsDTO {
    private Long id;
    private String category;
    private String title;
    private String details;
}
