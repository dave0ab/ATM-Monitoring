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
@Table(name = "ATM_ACCESS", uniqueConstraints = @UniqueConstraint(columnNames = {"TERMINAL_ID", "USER_ID"}))
public class ATMAccesses {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "TERMINAL_ID", nullable = false) // Removed `unique = true`
        private String TID;

        @Column(name = "USER_ID", nullable = false)
        private String userId;

        @Column(name = "CREATED_AT", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "STATUS", nullable = false)
        private String status;
}
