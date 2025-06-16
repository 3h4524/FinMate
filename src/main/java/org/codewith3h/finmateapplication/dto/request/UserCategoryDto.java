package org.codewith3h.finmateapplication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCategoryDto {
    private Integer userId;
    private String userCategoryName;
    private String type;
    private String icon;
    private String color;
}
