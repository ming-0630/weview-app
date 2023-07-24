package org.weviewapp.service;

import org.springframework.http.ResponseEntity;
import org.weviewapp.dto.ReportDTO;
import org.weviewapp.entity.Report;

import java.util.UUID;

public interface ReportService {
    ResponseEntity<?> getAllReports();
    Report addReport(ReportDTO reportDTO);
    ResponseEntity<?> reportAction(ReportDTO reportDTO);
    ResponseEntity<?> getOneReport(UUID reportId);
}
