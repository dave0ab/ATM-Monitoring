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
@Table(name = "APP_USER")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long user_id;
//    @Column(name = "NAME", nullable = true)
//    private String name;
    @Column(name = "DOMAIN_ID", unique = true, nullable = false)
    private String domainId;
    @Column(name = "LAST_SIGNON")
    private Instant lastSignedOn;
//    @Column(name = "PASSWORD")
//    private String password;

//    @Column(name = "ACCESS")
//    private String access;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "NOTIFICATION_LEVEL")
    private String notificationLevel;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(name="ROLE")
    private Roles role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    @Column(name="CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name="EMAIL_SUBSCRIBE")
    private Boolean emailSubscribe;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }


    @Override
    public String getUsername() {
        return domainId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @PrePersist
    public void prePersist() {
        if (!this.emailSubscribe) {
            this.emailSubscribe = true;
        }
    }


}
