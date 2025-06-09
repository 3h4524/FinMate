package org.codewith3h.finmateapplication.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GoalResponse {

    String name;
    String description;
    UserResponse userResponse;
}
