package com.zemenbank.atmMonitoring.model.atmMonitor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AtmStatusReport")
public class AtmStatusReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(name = "TERMINAL_ID", nullable = false)
    private String terminalId;
    @Column(name = "ATM_NAME", nullable = false)
    private String atmName;
    @Column(name = "STATUS", nullable = false)
    private String status;


    // Constructors, getters, and setters omitted for brevity
}