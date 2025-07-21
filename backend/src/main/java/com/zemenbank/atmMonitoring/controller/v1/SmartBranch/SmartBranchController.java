package com.zemenbank.atmMonitoring.controller.v1.SmartBranch;

import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.services.AtmsService;
import com.zemenbank.atmMonitoring.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/SmartBranch")
@RequiredArgsConstructor
public class SmartBranchController {


    private final AtmsService atmsService;


    @GetMapping("/atms")
    public ResponseEntity<Page<AtmsResponse>> getAllAtms(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String problem) {


        Page<AtmsResponse> responsePage = atmsService.getAllAtmsSmartBranch(pageNumber, pageSize);
        return ResponseEntity.ok(responsePage);
    }



    @GetMapping("/atms/{atmId}")
    public ResponseEntity<AtmsResponse> getAtmById(@PathVariable String atmId) {
        AtmsResponse response = atmsService.getAtmBYIdSmartBranch(atmId);
        return ResponseEntity.ok(response);
    }



}