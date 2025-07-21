package com.zemenbank.atmMonitoring.services;

import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;

import java.util.List;

public interface ATM_NetworkService {
    ATM_Network getLogsOrderedByLastUpdated(String atmId);

}
