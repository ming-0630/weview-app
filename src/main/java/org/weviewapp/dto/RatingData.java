package org.weviewapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RatingData {
    private LocalDateTime date;
    private double rating;
}
