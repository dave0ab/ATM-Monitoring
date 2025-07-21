package com.zemenbank.atmMonitoring.controller.v1.bankworld;

import com.zemenbank.atmMonitoring.controller.BaseController;
import com.zemenbank.atmMonitoring.model.bankworld.ATM_Network;
import com.zemenbank.atmMonitoring.services.ATM_NetworkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Foreclosure REST APIs for Legal Department user",
        description = "REST APIs - Retrieve and Manage ATMs")
@RestController
@RequestMapping("/v1/atmsNetwork")
@RequiredArgsConstructor
public class ATM_NetworkController extends BaseController {

    private final ATM_NetworkService atm_NetworkService;


    @GetMapping("/{atmId}")
    public ATM_Network getOrderedLogsByAtmId(@PathVariable String atmId) {
        return atm_NetworkService.getLogsOrderedByLastUpdated(atmId);
    }
}
