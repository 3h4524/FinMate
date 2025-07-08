package org.codewith3h.finmateapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BudgetEntry {
    @JsonProperty("categoryName")
    private String categoryName;
    @JsonProperty("budget")
    private Double budget;
    @JsonProperty("savings")
    private Double savings;
}
