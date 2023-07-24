package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.ReportDTO;
import org.weviewapp.service.ReportService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/review/report")
public class ReportController {
    @Autowired
    ReportService reportService;

    @PostMapping("/add")
    public ResponseEntity<?> addReport(@RequestBody ReportDTO reportDTO) {
        reportService.addReport(reportDTO);

        Map<String, Object> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/getAll")
    public ResponseEntity<?> getAllReports() {
        return reportService.getAllReports();

//        Map<String, Object> response = new HashMap<>();
//        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/admin/action")
    public ResponseEntity<?> acceptReport(@RequestBody ReportDTO reportDTO) {
        return new ResponseEntity<>(reportService.reportAction(reportDTO), HttpStatus.OK);
    }
    @GetMapping("/get")
    public ResponseEntity<?> getReport(@RequestParam String reportId) {
        return reportService.getOneReport(UUID.fromString(reportId));
    }
}
