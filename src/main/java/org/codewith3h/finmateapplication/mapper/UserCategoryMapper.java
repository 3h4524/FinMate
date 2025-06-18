package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.UserCategoryDto;
import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserCategoryMapper {
    @Mapping(source = "name", target = "categoryName")
    @Mapping(source =  "id", target = "categoryId")
    CategoryResponse toCategoryResponse(UserCategory userCategory);

    @Mapping(target = "name", source = "userCategoryName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", source = "type")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserCategory toEntity(UserCategoryDto dto);
}
