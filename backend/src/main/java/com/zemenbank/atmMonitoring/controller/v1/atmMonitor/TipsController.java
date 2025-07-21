package com.zemenbank.atmMonitoring.controller.v1.atmMonitor;

import com.zemenbank.atmMonitoring.model.atmMonitor.Tips;
import com.zemenbank.atmMonitoring.services.TipsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tips")
public class TipsController {

    @Autowired
    private TipsService tipsService;

    @PostMapping
    public ResponseEntity<Tips> createTips(@RequestBody Tips tips) {
        Tips createdTips = tipsService.createTips(tips);
        return ResponseEntity.ok(createdTips);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tips> updateTips(@PathVariable Long id, @RequestBody Tips tips) {
        Tips updatedTips = tipsService.updateTips(id, tips);
        return ResponseEntity.ok(updatedTips);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTips(@PathVariable Long id) {
        tipsService.deleteTips(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tips> getTipsById(@PathVariable Long id) {
        Tips tips = tipsService.getTipsById(id);
        return ResponseEntity.ok(tips);
    }

    @GetMapping
    public ResponseEntity<List<Tips>> getAllTips() {
        List<Tips> tipsList = tipsService.getAllTips();
        return ResponseEntity.ok(tipsList);
    }
}
