package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.enums.EscalationLevels;
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
@Table(name = "ATM_CASH_BALANCE")
public class ATMCashBalance {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    @Column(name = "ID", nullable = false)
    private Long id;  // Surrogate primary key


    @Column(name = "ATM_ID", nullable = false, length = 10)
    private String atmId;


    @Column(name = "ATM_NAME", nullable = false)
    private String name;

    @Column(name = "BALANCE", nullable = false)
    private double balance;
    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "SINCE_DATE_TIME")
    private LocalDateTime sinceDateTime;

    @Column(name = "LAST_TRANSACTION")
    private LocalDateTime lastTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name="EscalationLevel")
    private EscalationLevels level;
    @Column(name = "TOTAL_DAYS")
    private Long totalDays;
}
