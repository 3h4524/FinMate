package org.codewith3h.finmateapplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureResponse {
    private Integer id;
    private String featureCode;
    private String featureName;
    private String featureDescription;
    boolean isActive;
}

