package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PingStatusRepository extends JpaRepository<PingStatus, Long> {

    PingStatus findByTerminalId(String terminalId);

    List<PingStatus> findBySTATUSIn(List<String> STATUS);

    List<PingStatus> findBySTATUS(String STATUS);
}