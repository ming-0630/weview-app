package org.weviewapp.service;

import org.springframework.http.ResponseEntity;
import org.weviewapp.dto.ReportDTO;
import org.weviewapp.entity.Report;

public interface ReportService {
    ResponseEntity<?> getAllReports();
    Report addReport(ReportDTO reportDTO);
    ResponseEntity<?> reportAction(ReportDTO reportDTO);
}
