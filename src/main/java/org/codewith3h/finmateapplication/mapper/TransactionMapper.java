    package org.codewith3h.finmateapplication.mapper;

    import org.codewith3h.finmateapplication.dto.request.TransactionCreationRequest;
    import org.codewith3h.finmateapplication.dto.request.TransactionUpdateRequest;
    import org.codewith3h.finmateapplication.dto.response.TransactionResponse;
    import org.codewith3h.finmateapplication.entity.Category;
    import org.codewith3h.finmateapplication.entity.Transaction;
    import org.codewith3h.finmateapplication.entity.UserCategory;
    import org.codewith3h.finmateapplication.exception.AppException;
    import org.codewith3h.finmateapplication.exception.ErrorCode;
    import org.codewith3h.finmateapplication.repository.CategoryRepository;
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
    public interface TransactionMapper {


        @Named("mapInstantToLocalDateTime")
        default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
            if (instant == null) return null;
            return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Ho_Chi_Minh"));
        }

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        Transaction toEntity(TransactionCreationRequest dto);

        @Mapping(source = "id" , target = "transactionId")
        @Mapping(source = "user.id", target = "userId")
        @Mapping(source = "category.id", target = "categoryId")
        @Mapping(source = "userCategory.id", target = "userCategoryId")
        @Mapping(source = "category.name", target = "categoryName")
        @Mapping(source = "userCategory.name", target = "userCategoryName")
        @Mapping(source = "category.type", target = "type")
        @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapInstantToLocalDateTime")
        @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "mapInstantToLocalDateTime")
        @Mapping(target = "type", expression = "java(resolveType(entity.getCategory(), entity.getUserCategory()))")
        TransactionResponse toResponseDto(Transaction entity);

        @Named("resolveType")
        default String resolveType(Category category, UserCategory userCategory) {
            if (category != null && category.getType() != null) {
                return category.getType();
            }
            return userCategory != null ? userCategory.getType() : null;
        }

        List<TransactionResponse> toResponseDtoList (List<Transaction> entities);

        @Named("mapCategoryIdToCategory")
        default Category mapCategoryIdToCategory(Integer categoryId, @Context CategoryRepository categoryRepository){
            if(categoryId == null) return null;
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND_EXCEPTION));
        }

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "user", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategoryIdToCategory")
        void updateEntityFromDto(TransactionUpdateRequest dto, @MappingTarget Transaction entity, @Context CategoryRepository categoryRepository);

    }
