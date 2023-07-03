package org.weviewapp.dto;

import lombok.Data;
import org.weviewapp.entity.Report;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportListDTO {
    private String id;
    private ReviewDTO review;
    private List<Report> reports;
    private LocalDateTime latestReportDate;
}
