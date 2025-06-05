package org.codewith3h.finmateapplication.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoalResponse {

    String name;
    String description;
    UserResponse userResponse;
}
