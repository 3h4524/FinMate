//package org.codewith3h.finmateapplication.mapper;
//
//import org.codewith3h.finmateapplication.dto.request.RecurringTransactionRequest;
//import org.codewith3h.finmateapplication.dto.response.RecurringTransactionResponse;
//import org.codewith3h.finmateapplication.entity.RecurringTransaction;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.NullValuePropertyMappingStrategy;
//import org.mapstruct.ReportingPolicy;
//
//import java.util.List;
//
//@Mapper(
//        componentModel = "spring",
//        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
//        unmappedTargetPolicy = ReportingPolicy.IGNORE
//)
//public interface RecurringTransactionMapper {
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    RecurringTransaction toEntity(RecurringTransactionRequest dto);
//
//    @Mapping(source = "id" , target = "recurringId")
//    @Mapping(source = "user.id", target = "userId")
//    @Mapping(source = "category.id", target = "categoryId")
//    @Mapping(source = "userCategory.id", target = "userCategoryId")
//    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapInstantToLocalDateTime")
//    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "mapInstantToLocalDateTime")
//
//    RecurringTransactionResponse toResponseDto(RecurringTransaction entity);
//
//    List<RecurringTransactionResponse> toResponseDtoList(List<RecurringTransaction> entities);
//
//}
