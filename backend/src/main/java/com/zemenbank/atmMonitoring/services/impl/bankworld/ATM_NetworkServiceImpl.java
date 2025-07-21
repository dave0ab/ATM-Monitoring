package com.zemenbank.atmMonitoring.services.impl.bankworld;

import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import com.zemenbank.atmMonitoring.repositories.bankworld.ATM_NetworkRepository;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.BaseServices;
import com.zemenbank.atmMonitoring.services.ATM_NetworkService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class ATM_NetworkServiceImpl extends BaseServices implements ATM_NetworkService {


    private final ATM_NetworkRepository atmNetworkRepository;

    private final AuthService authService;

    private final SimpMessagingTemplate messagingTemplate;


    public ATM_NetworkServiceImpl(ATM_NetworkRepository atmNetworkRepository, AuthService authService , SimpMessagingTemplate messagingTemplate) {
        this.atmNetworkRepository = atmNetworkRepository;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
    }


    public ATM_Network getLogsOrderedByLastUpdated(String atmId) {
        return atmNetworkRepository.findByAtmId(atmId);
    }

}