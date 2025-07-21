package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.model.atmMonitor.ATMAccesses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ATMAccessRepository extends JpaRepository<ATMAccesses, Long> {
    Optional<ATMAccesses> findByTID(String terminalId);
    List<ATMAccesses> findByUserId(String userId);

    Optional<ATMAccesses> findByTIDAndUserId(String terminalId, String userId);
}
