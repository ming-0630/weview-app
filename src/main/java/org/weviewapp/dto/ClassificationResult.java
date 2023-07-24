package org.weviewapp.dto;

import lombok.Data;

@Data
public class ClassificationResult {
    private double score;
    private String label;
}