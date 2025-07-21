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
@Table(name = "ATM_LOCATION")
public class AtmLocation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "TID", nullable = false, unique = true)
        private String TID;

        @Column(name = "LO", nullable = false)
        private String LO;

        @Column(name = "LA", nullable = false)
        private String LA;

        // Getters, Setters, etc.
    }
