package org.codewith3h.finmateapplication.controller;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.ApiResponse;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Slf4j
@Validated
@Builder
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(){
        log.info("Fetching system's categories");
        List<CategoryResponse>  categories = categoryService.getCategories();
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>();
        response.setMessage("Fetching all system's categories successfully.");
        response.setResult(categories);
        return  ResponseEntity.ok(response);
    }
}
