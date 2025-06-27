package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CouponRequest;
import org.codewith3h.finmateapplication.dto.response.CouponResponse;
import org.codewith3h.finmateapplication.entity.Coupon;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CouponMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expiryDate", expression = "java(toEndOfDay(dto.getExpiryDate()))")
    Coupon toEntity(CouponRequest dto);

    CouponResponse toResponseDto(Coupon entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CouponRequest dto, @MappingTarget Coupon entity);


    default LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(23, 59, 59, 997_000_000);
    }

}
