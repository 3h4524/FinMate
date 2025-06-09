package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CreateGoalRequest;
import org.codewith3h.finmateapplication.dto.response.GoalResponse;
import org.codewith3h.finmateapplication.entity.Goal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface GoalMapper {

    @Mapping(source = "userId", target = "user.id")
    Goal toGoal(CreateGoalRequest request);

    GoalResponse toGoalResponse(Goal goal);
}