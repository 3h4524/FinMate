package org.codewith3h.finmateapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.dto.request.UserCategoryDto;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.codewith3h.finmateapplication.enums.FeatureCode;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.UserCategoryMapper;
import org.codewith3h.finmateapplication.repository.SubscriptionRepository;
import org.codewith3h.finmateapplication.repository.UserCategoryRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final UserRepository userRepository;
    private final FeatureService featureService;

    @PreAuthorize("hasRole('USER')")
    public List<CategoryResponse> getUserCategory(Integer userId){
        log.info("Fetching category for user: {}", userId);
        List<UserCategory> categories = userCategoryRepository.findByUserId(userId);
        return categories.stream().map(userCategoryMapper :: toCategoryResponse).toList();
    }

        @PreAuthorize("hasRole('USER')")
        public CategoryResponse createUserCategory(UserCategoryDto request){
            log.info("Creating user's category for user: {}", request.getUserId());
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            boolean hasUnlimited = featureService
                    .userHasFeature(user.getId(), FeatureCode.UNLIMITED_CUSTOM_CATEGORY.name());

            if(totalUserCategoryByUserId(request.getUserId()) > 3 && !hasUnlimited){
                throw new AppException(ErrorCode.EXCEED_FREE_CREATE_CUSTOM_CATEGORY);
            }

            UserCategory userCategory = userCategoryMapper.toEntity(request);
            userCategory.setUser(user);

            userCategoryRepository.save(userCategory);
            return userCategoryMapper.toCategoryResponse(userCategory);
        }


    @PreAuthorize("hasRole('USER')")
    public Integer totalUserCategoryByUserId(Integer userId){
        log.info("Counting user category for user {}", userId);
        return userCategoryRepository.countByUserId(userId);
    }
}
