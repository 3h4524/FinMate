package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.mapper.CategoryMapper;
import org.codewith3h.finmateapplication.repository.CategoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAllByNameNot("Adjust balance");
        return categories.stream().map(categoryMapper :: toCategoryResponse).toList();
    }


    public List<CategoryResponse> getAllCategories(){
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream().map(categoryMapper :: toCategoryResponse).collect(Collectors.toList());
    }

}
