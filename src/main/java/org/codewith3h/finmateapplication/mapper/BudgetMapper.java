package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.CreateBudgetRequest;
import org.codewith3h.finmateapplication.dto.response.BudgetResponse;
import org.codewith3h.finmateapplication.entity.Budget;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @AfterMapping
    default void resolveRelations(@MappingTarget Budget budget,
                                  CreateBudgetRequest createBudgetRequest,
                                  @Context EntityResolver entityResolver) {
        budget.setUser(entityResolver.resolverUser(createBudgetRequest.getUserId()));
        if(createBudgetRequest.getCategoryId() != null) {
            budget.setCategory(entityResolver.resolverCategory(createBudgetRequest.getCategoryId()));
        } else if (createBudgetRequest.getUserCategoryId() != null) {
            budget.setUserCategory(entityResolver.resolverUserCategory(createBudgetRequest.getUserCategoryId()));
        }
    }
    Budget toBudget(CreateBudgetRequest request, @Context EntityResolver entityResolver);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "userCategoryId", source = "userCategory.id")
    @Mapping(target = "userCategoryName", source = "userCategory.name")
    BudgetResponse toBudgetResponse(Budget budget);
}