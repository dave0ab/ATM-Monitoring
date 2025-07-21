package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;

import com.zemenbank.atmMonitoring.dto.request.ATMAccessRequestDTO;
import com.zemenbank.atmMonitoring.dto.response.ATMAccessResponseDTO;
import com.zemenbank.atmMonitoring.security.AuthService;
import com.zemenbank.atmMonitoring.services.ATMAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/atm-access")

public class ATMAccessController {

    @Autowired
    private ATMAccessService service;



    @PostMapping
    public ResponseEntity<List<ATMAccessResponseDTO>> createOrUpdateATMAccesses(@RequestBody List<ATMAccessRequestDTO> requestDTOs) {
        List<ATMAccessResponseDTO> responses = service.createOrUpdateATMAccesses(requestDTOs);
        return ResponseEntity.ok(responses);
    }


    @GetMapping
    public ResponseEntity<List<ATMAccessResponseDTO>> getAllATMAccesses() {
        return ResponseEntity.ok(service.getAllATMAccesses());
    }



    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ATMAccessResponseDTO>> getPermissionsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.getPermissionsByUserId(userId));
    }

    @GetMapping("/terminal/{terminalId}")
    public ResponseEntity<List<ATMAccessResponseDTO>> getPermissionsByTerminalId(@PathVariable String terminalId) {
        return ResponseEntity.ok(service.getPermissionsByTerminalId(terminalId));
    }
}
