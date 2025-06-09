package org.codewith3h.finmateapplication.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.service.UserCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/userCategories")
@RequiredArgsConstructor
@Builder
@Slf4j
public class UserCategoryController {

    private final UserCategoryService  userCategoryService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getUserCategories(
            @PathVariable Integer userId) {
        log.info("Fetching user's categories");
        List<CategoryResponse> categories = userCategoryService.getUserCategory(userId);
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>();
        response.setResult(categories);
        response.setMessage("success");
        return ResponseEntity.ok(response);
    }
}
