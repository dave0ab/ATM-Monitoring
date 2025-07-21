package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;


import com.zemenbank.atmMonitoring.dto.request.LocationRequest;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;
import com.zemenbank.atmMonitoring.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/settings")
public class SettingController {

    private final SettingService settingService;

    @Autowired
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }


    @PostMapping("/addOrUpdateLocation")
    public AtmLocation addOrUpdateLocation(@RequestBody LocationRequest request) {
        return settingService.addOrUpdateLocation(request.getTID(), request.getLO(), request.getLA());
    }




    @GetMapping("/get/{TID}")
    public AtmLocation getLocationByTID(@PathVariable String TID) {
        return settingService.getLocationByTID(TID);
    }
}
