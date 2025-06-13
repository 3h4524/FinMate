package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.response.CategoryResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {
    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "name", target = "categoryName")
    CategoryResponse toCategoryResponse(Category category);
}
