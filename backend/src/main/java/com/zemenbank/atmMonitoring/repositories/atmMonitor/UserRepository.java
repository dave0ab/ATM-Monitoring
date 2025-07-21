package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    Optional<User> findByDomainId(String domainid);
    Page<User> findByDomainIdContainingIgnoreCase(String search, Pageable pageable);

    List<User> findByNotificationLevel(String notificationLevel);

    List<User> findAllByRoleInAndNotificationLevel(List<Roles> roles, String notificationLevel);
}
