package com.zemenbank.atmMonitoring.repositories.atmMonitor;




import com.zemenbank.atmMonitoring.model.atmMonitor.AtmMonitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorStatusRepository extends JpaRepository<AtmMonitorStatus, Long> {

    Optional<AtmMonitorStatus> findByTerminalId(String terminalId);

    @Query("SELECT s FROM AtmMonitorStatus s WHERE s.status = 'No Issue'")
    List<AtmMonitorStatus> findAllStatusNoIssue();

    // Find all statuses that are NOT 'No Issue'
    @Query("SELECT s FROM AtmMonitorStatus s WHERE s.status != 'No Issue'")
    List<AtmMonitorStatus> findAllStatusNotNoIssue();

    // Find all statuses based on custom status parameter
    @Query("SELECT s FROM AtmMonitorStatus s WHERE s.status = :status")
    List<AtmMonitorStatus> findAllStatus(String status);



    @Query("SELECT s FROM AtmMonitorStatus s WHERE s.terminalId IN :terminalIds AND s.status = :status")
    List<AtmMonitorStatus> findAllByTerminalIdInAndStatus(@Param("status") String status, @Param("terminalIds") List<String> terminalIds);

    // Custom query using JPQL for finding terminalIds with a status different from "No Issue"
    @Query("SELECT s FROM AtmMonitorStatus s WHERE s.terminalId IN :terminalIds AND s.status != :status")
    List<AtmMonitorStatus> findAllByTerminalIdInAndStatusNot(@Param("status") String status, @Param("terminalIds") List<String> terminalIds);
}