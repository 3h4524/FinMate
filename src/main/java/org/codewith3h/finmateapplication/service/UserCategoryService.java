package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.UserCategoryMapper;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserCategoryService {

    private final UserCategoryRepository userCategoryRepository;
    private final UserCategoryMapper userCategoryMapper;

    public List<CategoryResponse> getUserCategory(Integer userId){
        log.info("Fetching category for user: {}", userId);
        List<UserCategory> categories = userCategoryRepository.findByUserId(userId);
        return categories.stream().map(userCategoryMapper :: toCategoryResponse).toList();
    }
}
