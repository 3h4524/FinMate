package org.codewith3h.finmateapplication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeatureSearchRequest {
    private String featureCode;
    private Boolean isActive;
    private String featureName;
    private int page = 0;
    private int size = 10;
    private String sortBy = "code";
    private String sortDirection = "DESC";
}
