package com.zemenbank.atmMonitoring.model.bankworld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GTW_GATEWAY_INFO", schema = "BANKWORLD")
public class GatewayInfo {


    @Column(name = "SYSTEM_ID", length = 8, nullable = false)
    private String systemId;

    @Id
    @Column(name = "NETWORK", length = 6, nullable = false , unique = true)
    private String network;

    @Column(name = "IGNORE_REM_DES_STAT", precision = 1)
    private Integer ignoreRemDesStat;

    @Column(name = "LOC_DES_STAT", precision = 1)
    private Integer locDesStat;

    @Column(name = "REM_DES_STAT", precision = 1)
    private Integer remDesStat;

    @Column(name = "ACTUAL_UP_TIME", precision = 10)
    private Long actualUpTime;

    @Column(name = "ACTUAL_DOWN_TIME", precision = 10)
    private Long actualDownTime;

    @Column(name = "ACTUAL_RESET_TIME", precision = 10)
    private Long actualResetTime;

    @Column(name = "COMMS_STATUS", precision = 1)
    private Integer commsStatus;

    @Column(name = "ACTUAL_STAT", precision = 1)
    private Integer actualStat;

    @Column(name = "LINE_STAT", precision = 1)
    private Integer lineStat;

    @Column(name = "LINE_CHANGE_TIME", precision = 10)
    private Long lineChangeTime;

    @Column(name = "ACTUAL_CHANGE_TIME", precision = 10)
    private Long actualChangeTime;

    @Column(name = "LOC_DES_SON", precision = 1)
    private Integer locDesSon;

    @Column(name = "REM_DES_SON", precision = 1)
    private Integer remDesSon;

    @Column(name = "LAST_INC_ECHO_TIME", precision = 10)
    private Long lastIncEchoTime;

    @Column(name = "INSTANCE", precision = 2, nullable = false)
    private Integer instance;

    @Column(name = "SERVER_ADDR", length = 20)
    private String serverAddr;

    @Column(name = "SERVER_PORT", precision = 5)
    private Integer serverPort;

    @Column(name = "SOURCE_STATION_ID", length = 6)
    private String sourceStationId;
}
