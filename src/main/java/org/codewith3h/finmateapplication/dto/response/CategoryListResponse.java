package org.codewith3h.finmateapplication.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryListResponse {
    private List<CategoryResponse> categoryList;
    private List<CategoryResponse> userCategoryList;
}
