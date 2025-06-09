package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "userCategoryId", target = "userCategory.id")
    Budget toBudget(CreateBudgetRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "userCategory.name", target = "userCategoryName")
    BudgetResponse toBudgetResponse(Budget budget);
}