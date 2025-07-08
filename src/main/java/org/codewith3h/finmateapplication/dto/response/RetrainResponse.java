package org.codewith3h.finmateapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RetrainResponse {
    @JsonProperty("status")
    private String status;
}
