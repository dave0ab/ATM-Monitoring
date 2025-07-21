package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.enums.EscalationLevels;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "monitor_status")
public class AtmMonitorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "TERMINAL_ID", nullable = false, unique = true)
    private String terminalId;


    @Column(name = "ATM_NAME", nullable = false, unique = true)
    private String atmName;
    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "SINCE_DATE_TIME")
    private LocalDateTime sinceDateTime;

    @Column(name = "TOTAL_DAYS")
    private Long totalDays;

    @Enumerated(EnumType.STRING)
    @Column(name="EscalationLevel")
    private EscalationLevels level;

    @Column(name = "LAST_NOTIFIED_AT")
    private LocalDateTime lastNotifiedAt;

    @Column(name = "LAST_TRANSACTION")
    private LocalDateTime lastTransaction;

    @Column(name = "REMARK")
    private String remark;


}