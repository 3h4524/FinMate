package org.codewith3h.finmateapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.codewith3h.finmateapplication.service.AIService;

import java.util.List;

@Data
public class BudgetPredictionResponse {
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("budgets")
    private List<BudgetEntry> budgets;
    @JsonProperty("timestamp")
    private String timestamp;
}
