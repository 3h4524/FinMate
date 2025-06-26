package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.AdminLogCreateRequest;
import org.codewith3h.finmateapplication.dto.response.AdminLogResponse;
import org.codewith3h.finmateapplication.entity.AdminLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminLogMapper {

    @Mapping(target = "adminId", source = "admin.id")
    AdminLogResponse toAdminLogResponse(AdminLog adminLog);

    @Mapping(target = "admin.id", source = "adminId")
    AdminLog toAdminLog(AdminLogCreateRequest request);

}