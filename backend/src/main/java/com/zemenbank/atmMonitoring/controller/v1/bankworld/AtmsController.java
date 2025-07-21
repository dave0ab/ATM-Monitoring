package com.zemenbank.atmMonitoring.controller.v1.bankworld;

import com.zemenbank.atmMonitoring.controller.BaseController;
import com.zemenbank.atmMonitoring.dto.request.AddRemarkRequest;
import com.zemenbank.atmMonitoring.dto.request.LocationRequest;
import com.zemenbank.atmMonitoring.dto.response.AtmsReportResponseDTO;
import com.zemenbank.atmMonitoring.dto.response.AtmsResponse;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmLocation;
import com.zemenbank.atmMonitoring.model.atmMonitor.AtmStatusReport;
import com.zemenbank.atmMonitoring.services.AtmsService;
import com.zemenbank.atmMonitoring.services.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Foreclosure REST APIs for Legal Department user",
        description = "REST APIs - Retrieve and Manage ATMs")
@RestController
@RequestMapping("/v1/atms")
@RequiredArgsConstructor
public class AtmsController extends BaseController {

    private final AtmsService atmsService;

    private final ReportService reportService;


    @GetMapping
    public ResponseEntity<Page<AtmsResponse>> getAllAtms(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String problem)
    {


        Page<AtmsResponse> responsePage = atmsService.getAllAtms(pageNumber, pageSize, status, search  ,sort,  sortBy ,problem);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{atmId}")
    public ResponseEntity<AtmsResponse> getAtmById(@PathVariable String atmId) {
        AtmsResponse response = atmsService.getAtmById(atmId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    public List<AtmsReportResponseDTO> getAtmBReport() {
        List<AtmsReportResponseDTO> response = reportService.generateAllAtmsReport();
        return ResponseEntity.ok(response).getBody();
    }


    // Endpoint to generate the report and return the download URL
    // Export report
    @GetMapping("/excelExport/{type}")
    public ResponseEntity<Map<String, String>> exportReport(@PathVariable String type) {
        String downloadUrl=null;
        if(type.toLowerCase().equals("exportTxt".toLowerCase()) ){

            downloadUrl= reportService.exportAtmsReportToTxt(type);


        }
        else {
            downloadUrl= reportService.exportAtmsReportToExcel(type);

        }

        if (downloadUrl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate report."));
        }

        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    // Download file
    @GetMapping("/document/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Path to the exports directory
            Path filePath = Paths.get("exports").resolve(fileName).normalize();

            // Load the file as a resource
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Return the file as a downloadable response
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }






    @GetMapping("/currentStatus")
    public ResponseEntity<List<AtmStatusReport>> getAllCurrentAtmStatuses() {
        List<AtmStatusReport> atmStatuses = reportService.getAllAtmStatuses();
        return ResponseEntity.ok(atmStatuses);
    }

    @GetMapping("/currentStatus/{terminalId}")
    public ResponseEntity<?> getAtmStatus(@PathVariable String terminalId) {
        if (terminalId != null) {
            // Fetch status for a specific terminalId
            return reportService.getAtmStatusByTerminalId(terminalId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            // Fetch all statuses if terminalId is not provided
            List<AtmStatusReport> atmStatuses = reportService.getAllAtmStatuses();
            return ResponseEntity.ok(atmStatuses);
        }
    }



    @PostMapping("/addRemark")
    public  String addRemark(@RequestBody AddRemarkRequest request) {
        return atmsService.addRemark(request.getAtmId() , request.getRemark());
    }



}




