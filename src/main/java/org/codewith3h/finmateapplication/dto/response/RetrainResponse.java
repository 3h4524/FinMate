package org.codewith3h.finmateapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RetrainResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("mse")
    private Double mse;

    @JsonProperty("training_duration_seconds")
    private Double trainingDurationSeconds;

    @JsonProperty("num_transactions")
    private Integer numTransactions;

    @JsonProperty("feature_columns")
    private List<String> featureColumns;

    @JsonProperty("top_categories")
    private List<String> topCategories;

    @JsonProperty("training_timestamp")
    private LocalDateTime trainingTimestamp;
}
