package com.zemenbank.atmMonitoring.services.impl.atmMonitor;


import com.zemenbank.atmMonitoring.dto.request.ATMAccessRequestDTO;
import com.zemenbank.atmMonitoring.dto.response.ATMAccessResponseDTO;
import com.zemenbank.atmMonitoring.model.atmMonitor.ATMAccesses;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.ATMAccessRepository;
import com.zemenbank.atmMonitoring.services.ATMAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ATMAccessServiceImpl implements ATMAccessService {

    @Autowired
    private ATMAccessRepository repository;

    @Override
    public List<ATMAccessResponseDTO> createOrUpdateATMAccesses(List<ATMAccessRequestDTO> requestDTOs) {
        return requestDTOs.stream()
                .map(requestDTO -> {
                    // Find the existing record for the user and terminal
                    Optional<ATMAccesses> existingAccess = repository.findByTIDAndUserId(
                            requestDTO.getTerminalId(), requestDTO.getUserId());

                    ATMAccesses atmAccess = existingAccess.orElseGet(() -> ATMAccesses.builder()
                            .TID(requestDTO.getTerminalId())
                            .userId(requestDTO.getUserId())
                            .createdAt(LocalDateTime.now())
                            .build());

                    // Update or set the status
                    atmAccess.setStatus(requestDTO.getStatus());

                    // Save the record
                    ATMAccesses savedAccess = repository.save(atmAccess);
                    return toResponseDTO(savedAccess);
                })
                .collect(Collectors.toList());
    }







    @Override
    public List<ATMAccessResponseDTO> getAllATMAccesses() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ATMAccessResponseDTO> getPermissionsByUserId(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ATMAccessResponseDTO> getPermissionsByTerminalId(String terminalId) {
        return repository.findByTID(terminalId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private ATMAccessResponseDTO toResponseDTO(ATMAccesses entity) {
        return ATMAccessResponseDTO.builder()
                .id(entity.getId())
                .terminalId(entity.getTID())
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
