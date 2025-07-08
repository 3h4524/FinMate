package org.codewith3h.finmateapplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BudgetPredictionRequest {
    @JsonProperty("user_id")
    private Integer userId;
}
