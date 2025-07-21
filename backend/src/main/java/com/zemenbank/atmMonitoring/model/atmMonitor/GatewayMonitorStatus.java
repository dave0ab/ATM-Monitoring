package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.enums.EscalationLevels;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "gateway_monitor_status")
public class GatewayMonitorStatus {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "ID", nullable = false, unique = true)
        private Long id;


        @Column(name = "NETWORK", length = 6, nullable = false)
        private String network;

        @Column(name = "NETWORK_NAME", nullable = false)
        private String networkName;
        @Column(name = "COMMS_STATUS", length = 6)
        private Integer commsStatus;

        @Column(name = "ACTUAL_STAT", length = 6)
        private Integer actualStat;
        @Column(name = "LOC_DEC_STAT")
        private Integer locDesStat;



        @Column(name = "STATUS")
        private String CombinedStatus;
        @Column(name = "REM_DES_STAT")
        private Integer remDesStat;

        @Column(name = "LINE_STAT")
        private Integer lineStat;



        @Column(name = "REGISTERED_AT", nullable = false)
        private LocalDateTime registeredAt;

        @Column(name = "SINCE_DATE_TIME")
        private LocalDateTime sinceDateTime;

        @Column(name = "TOTAL_DAYS")
        private Long totalDays;

        @Enumerated(EnumType.STRING)
        @Column(name="EscalationLevel")
        private EscalationLevels level;




}