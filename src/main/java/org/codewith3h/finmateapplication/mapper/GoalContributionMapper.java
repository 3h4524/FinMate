package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.request.CreateGoalContributionRequest;
import org.codewith3h.finmateapplication.dto.response.GoalContributionResponse;
import org.codewith3h.finmateapplication.entity.GoalContribution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GoalContributionMapper {
    @Mapping(target = "goalId", source = "goal.id")
    GoalContributionResponse toGoalContributionResponse(GoalContribution goalContribution);

    @Mapping(source = "goalId", target = "goal.id")
    GoalContribution toGoalContribution(CreateGoalContributionRequest createGoalContributionRequest);
}
