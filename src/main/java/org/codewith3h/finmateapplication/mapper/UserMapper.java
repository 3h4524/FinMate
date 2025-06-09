package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CreateUserRequest;
import org.codewith3h.finmateapplication.dto.response.UserResponse;
import org.codewith3h.finmateapplication.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser (CreateUserRequest createUserRequest);
    UserResponse toUserResponse(User user);

}
