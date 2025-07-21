package com.zemenbank.atmMonitoring.repositories.atmMonitor;

import com.zemenbank.atmMonitoring.model.atmMonitor.GatewayMonitorStatus;
import com.zemenbank.atmMonitoring.model.bankworld.GatewayInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GatewayStatusReportRepository extends JpaRepository<GatewayMonitorStatus, Long> {


    Optional<GatewayMonitorStatus> findByNetwork(String network);

    @Query("SELECT g FROM GatewayMonitorStatus g WHERE g.CombinedStatus NOT LIKE %:status%")
    List<GatewayMonitorStatus> findAllStatusNotContainingOnlineAll(String status);




}
