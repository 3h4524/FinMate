package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
import org.codewith3h.finmateapplication.entity.Category;
import org.codewith3h.finmateapplication.entity.RecurringTransaction;
import org.codewith3h.finmateapplication.entity.UserCategory;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecurringTransactionMapper {

    @Named("mapInstantToLocalDateTime")
    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @AfterMapping
    default void resolveRelations(@MappingTarget RecurringTransaction recurringTransaction,
                                  RecurringTransactionRequest recurringTransactionRequest,
                                  @Context EntityResolver entityResolver) {
        recurringTransaction.setUser(entityResolver.resolverUser(recurringTransactionRequest.getUserId()));
        if(recurringTransactionRequest.getCategoryId() != null) {
            recurringTransaction.setCategory(entityResolver.resolverCategory(recurringTransactionRequest.getCategoryId()));
        } else if (recurringTransactionRequest.getUserCategoryId() != null) {
            recurringTransaction.setUserCategory(entityResolver.resolverUserCategory(recurringTransactionRequest.getUserCategoryId()));
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RecurringTransaction toEntity(RecurringTransactionRequest dto, @Context EntityResolver entityResolver);

    @Mapping(source = "id" , target = "recurringId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "userCategory.id", target = "userCategoryId")
    @Mapping(target = "type", expression = "java(resolveType(entity.getCategory(), entity.getUserCategory()))")
    @Mapping(source = "userCategory.name", target = "userCategoryName")
    @Mapping(target = "icon", expression = "java(entity.getCategory() != null ? entity.getCategory().getIcon() : entity.getUserCategory().getIcon())")
    RecurringTransactionResponse toResponseDto(RecurringTransaction entity);

    List<RecurringTransactionResponse> toResponseDtoList(List<RecurringTransaction> entities);

    @Named("resolveType")
    default String resolveType(Category category, UserCategory userCategory) {
        if (category != null && category.getType() != null) {
            return category.getType();
        }
        return userCategory != null ? userCategory.getType() : null;
    }


    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "userCategory.id", target = "userCategoryId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "note", target = "note")
    TransactionCreationRequest mapRecurringTransactionToTransactionRequestDto(RecurringTransaction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(RecurringTransactionRequest dto, @MappingTarget RecurringTransaction entity, @Context EntityResolver entityResolver);
}
