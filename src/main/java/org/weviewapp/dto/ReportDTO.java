package org.weviewapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.weviewapp.entity.Review;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ReportAction;

import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDTO {
    private UUID reportId;
    private ReportAction action;
    private List<String> reportReasons;
    private String description;

    private Review review;
    private User user;

    private String reviewId;
}
