package org.codewith3h.finmateapplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeatureRequestDto {
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    String code;
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name;
    @Size(max = 255, message = "Description cannot exceed 255 characters" )
    String description;
    @JsonProperty("isActive")
    boolean isActive;
}
