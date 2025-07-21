package com.zemenbank.atmMonitoring.repositories.bankworld;

import com.zemenbank.atmMonitoring.model.bankworld.ATM_CASH;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ATMCashRepository extends JpaRepository<ATM_CASH, String> {

    ATM_CASH findFirstByAtmId(String atmId);

    List<ATM_CASH> findByAtmId(String atmId);

    List<ATM_CASH> findAllByCleftLessThan(Double cleft);
}
