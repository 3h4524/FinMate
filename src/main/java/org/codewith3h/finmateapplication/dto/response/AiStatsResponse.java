package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiStatsResponse {
    int totalTraining;
    int successRate;
    float avgDuration;
    LocalDate lastUpdated;
    float accuracy;
    Integer totalTransaction;
    Integer totalCategories;
}
