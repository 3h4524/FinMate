package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.UpdateUserRequest;
import org.codewith3h.finmateapplication.dto.response.UserManagementResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserManagementMapper {

    UserManagementResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "isNewUser", ignore = true)
    @Mapping(target = "isDelete", ignore = true)
    @Mapping(target = "resendAttempts", ignore = true)
    @Mapping(target = "resendLockoutUntil", ignore = true)
    @Mapping(target = "verificationCode", ignore = true)
    @Mapping(target = "verificationCodeExpiry", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
} 