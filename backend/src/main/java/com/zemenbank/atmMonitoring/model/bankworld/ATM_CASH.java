package com.zemenbank.atmMonitoring.model.bankworld;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MON_ATM_REPLENISHMENT", schema = "BANKWORLD")

public class ATM_CASH {

    @Id
    @Column(name = "ATM_ID", nullable = false, length = 10)
    private String atmId;

    @Column(name = "CDISP", nullable = false, precision = 18)
    private Double cdisp;

    @Column(name = "CLEFT", nullable = false, precision = 18)
    private Double cleft;

    @Column(name = "CLOAD", nullable = false, precision = 18)
    private Double cload;

    @Column(name = "CPURGE", nullable = false, precision = 18)
    private Double cpurge;

    @Column(name = "C2ADD", nullable = false, precision = 18)
    private Double c2add;

    @Column(name = "C2DISP", nullable = false, precision = 18)
    private Double c2disp;

    @Column(name = "C2LEFT", nullable = false, precision = 18)
    private Double c2left;

    @Column(name = "C2LOAD", nullable = false, precision = 18)
    private Double c2load;

    @Column(name = "C2PURGE", nullable = false, precision = 18)
    private Double c2purge;

    @Column(name = "VCUR2", nullable = false, precision = 18)
    private Double vcur2;

    @Column(name = "LAST_UPDATED")
    private Timestamp lastUpdated;

    @Column(name = "LAST_UPDATED_EPOCH")
    private Long lastUpdatedEpoch;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;

    @Column(name = "CADD", nullable = false, precision = 18)
    private Double cadd;
}
