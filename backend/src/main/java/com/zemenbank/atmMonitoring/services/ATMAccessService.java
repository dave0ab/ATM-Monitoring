package com.zemenbank.atmMonitoring.services;


import com.zemenbank.atmMonitoring.dto.request.ATMAccessRequestDTO;
import com.zemenbank.atmMonitoring.dto.response.ATMAccessResponseDTO;

import java.util.List;

public interface ATMAccessService {
    List<ATMAccessResponseDTO> createOrUpdateATMAccesses(List<ATMAccessRequestDTO> requestDTOs);


    List<ATMAccessResponseDTO> getAllATMAccesses();

    List<ATMAccessResponseDTO> getPermissionsByUserId(String userId);

    List<ATMAccessResponseDTO> getPermissionsByTerminalId(String terminalId);
}
