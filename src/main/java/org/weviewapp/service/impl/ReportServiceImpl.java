package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.ReportDTO;
import org.weviewapp.dto.ReportListDTO;
import org.weviewapp.entity.Report;
import org.weviewapp.entity.ReportReason;
import org.weviewapp.entity.Review;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ReportAction;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ReportReasonRepository;
import org.weviewapp.repository.ReportRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.service.ReportService;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ReportReasonRepository reportReasonRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;
    @Override
    public ResponseEntity<?> getAllReports() {
        List<Report> allReports = reportRepository.findAll();
        Map<Review, List<Report>> groupedReports = new HashMap<>();

        for (Report report : allReports) {
            Review review = report.getReview();
            if (groupedReports.containsKey(review)) {
                // Add the report to the existing list
                groupedReports.get(review).add(report);
            } else {
                // Create a new list and add the report
                List<Report> newList = new ArrayList<>();
                newList.add(report);
                groupedReports.put(review, newList);
            }
        }
        List<ReportListDTO> reportResponseList = new ArrayList<>();

        int i = 0;
        for (Map.Entry<Review, List<Report>> entry : groupedReports.entrySet()) {
            Review review = entry.getKey();
            List<Report> reports = entry.getValue();
            ReportListDTO reportResponse = new ReportListDTO();
            reportResponse.setId("id-" + i);
            i++;
            reportResponse.setReview(reviewService.mapToReviewDTO(List.of(review)).get(0));
            reportResponse.setReports(reports);
            reportResponse.setLatestReportDate(
                    reports.stream()
                    .map(Report::getDateUpdated)
                    .max(LocalDateTime::compareTo).get()
            );

            reportResponseList.add(reportResponse);
        }
        reportResponseList.sort(Comparator.comparing(ReportListDTO::getLatestReportDate).reversed());
        return new ResponseEntity<>(reportResponseList, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> getOneReport(UUID reportId) {
        Optional<Report> report = reportRepository.findById(reportId);
        if (report.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find report!");
        }
        return new ResponseEntity<>(report.get(), HttpStatus.OK);
    }
    @Override
    public Report addReport(ReportDTO reportDTO) {
        Optional<Review> review = reviewRepository.findById(UUID.fromString(reportDTO.getReviewId()));

        if (review.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find review");
        }

        User user = userService.getCurrentUser();

        Optional<Report> prevReport = reportRepository.findByReporterIdAndReviewId(user.getId(), review.get().getId());

        if (!prevReport.isEmpty()) {
            Optional<List<ReportReason>> reasons =  reportReasonRepository.findByNameIn(reportDTO.getReportReasons());

            if (reasons.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find this reason!");
            }

            if (reasons.get().equals(prevReport.get().getReportReasons()) &&
                    reportDTO.getDescription().equalsIgnoreCase(prevReport.get().getDescription())) {
                if (prevReport.get().getAction().equals(ReportAction.REVIEWING)) {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Your report is under review, unable to submit same report!");
                } else if ((prevReport.get().getAction().equals(ReportAction.ACCEPTED) || prevReport.get().getAction().equals(ReportAction.RESOLVED))) {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "This review has been deleted, no additional reports needed. Thank you!");
                } else {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Your report has been reviewed and dismissed by admin! Please submit another reason.");
                }
            }

            // Different report
            if (prevReport.get().getAction().equals(ReportAction.DISMISSED)) {
                // Resend the report
                prevReport.get().setAction(ReportAction.REVIEWING);
            }

            // Update report
            prevReport.get().setReportReasons(reasons.get());
            prevReport.get().setDescription(reportDTO.getDescription());
            return reportRepository.save(prevReport.get());
        }

        Report newReport = new Report();
        newReport.setId(UUID.randomUUID());
        newReport.setReview(review.get());
        newReport.setReporter(user);
        newReport.setDescription(reportDTO.getDescription());
        newReport.setAction(ReportAction.REVIEWING);

        Optional<List<ReportReason>> reasons =  reportReasonRepository.findByNameIn(reportDTO.getReportReasons());

        if (reasons.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find this reason!");
        }

        newReport.setReportReasons(reasons.get());
        return reportRepository.save(newReport);
    }
    @Override
    public ResponseEntity<?> reportAction(ReportDTO reportDTO) {

        Optional<Report> report = reportRepository.findById(reportDTO.getReportId());

        if (report.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find report!");
        }

        if (!report.get().getAction().equals(ReportAction.REVIEWING)) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Report status is not under review!");
        }

        if (reportDTO.getAction().equals(ReportAction.DISMISSED)) {
            report.get().setAction(reportDTO.getAction());
            if (report.get().getReporter().getEmail().equals("ML")) {
                report.get().getReview().setVerified(true);
                reviewRepository.save(report.get().getReview());
            }
            reportRepository.save(report.get());
        }

        // Accept Report
        if (reportDTO.getAction().equals(ReportAction.ACCEPTED)) {
            report.get().setAction(reportDTO.getAction());
            report.get().setDescription(reportDTO.getDescription());

            Optional<List<ReportReason>> reasons =  reportReasonRepository.findByNameIn(reportDTO.getReportReasons());

            if (reasons.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find this reason!");
            }

            if(!reasons.get().equals(report.get().getReportReasons())) {
                report.get().setReportReasons(reasons.get());
            }
            report.get().setAction(ReportAction.ACCEPTED);
            reportRepository.save(report.get());
            resolveOtherReports(report.get().getReview().getId());

            report.get().getReview().setReport(report.get());
            reviewRepository.save(report.get().getReview());
        }

        Map<String, Object> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    private void resolveOtherReports(UUID reviewId) {
        Optional<List<Report>> reports = reportRepository.findAllByReviewId(reviewId);
        if (!reports.isEmpty()) {
            for (Report r: reports.get()) {
                if(r.getAction().equals(ReportAction.REVIEWING) ) {
                    // Change other reports that are not accepted
                    r.setAction(ReportAction.RESOLVED);
                    reportRepository.save(r);
                }
            }
        }
    }
}