package org.codewith3h.finmateapplication.dto.response;

import jakarta.annotation.Nullable;
import lombok.*;
import org.codewith3h.finmateapplication.enums.DurationType;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PremiumPackageResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationValue;
    private DurationType durationType;
    private Boolean isActive;
    private List<String> features;
    @Nullable
    private Integer subscribers;
    @Nullable
    private BigDecimal revenue;
}
