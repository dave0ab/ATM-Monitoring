package com.zemenbank.atmMonitoring.model.atmMonitor;

import com.zemenbank.atmMonitoring.enums.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PING_STATUS")
public class PingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(name = "IP_ADDRESS", nullable = false)
    private String IP_ADDRESS;

    @Column(name = "TERMINAL_ID", nullable = false)
    private String terminalId;

    @Column(nullable = false)
    private String STATUS;

    @Column(nullable = false)
    private LocalDateTime TIMESTAMP;

    @Column(columnDefinition = "TEXT")
    private String DETAILS;



    // Constructors, getters, and setters omitted for brevity
}