package com.zemenbank.atmMonitoring.repositories.atmMonitor;


import com.zemenbank.atmMonitoring.model.atmMonitor.ATMAccesses;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMCashBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ATMCashBalanceRepository extends JpaRepository<ATMCashBalance, Long> {
    Optional<ATMCashBalance> findByAtmId(String atmId);

}
