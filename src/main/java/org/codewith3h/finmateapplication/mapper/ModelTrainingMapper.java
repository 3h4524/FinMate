package org.codewith3h.finmateapplication.mapper;

import ch.qos.logback.core.model.ComponentModel;
import org.codewith3h.finmateapplication.dto.response.RetrainResponse;
import org.codewith3h.finmateapplication.entity.ModelTrainingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ModelTrainingMapper {
     ModelTrainingHistory toEntity(RetrainResponse dto);

     RetrainResponse toResponseDto (ModelTrainingHistory entity);
}
