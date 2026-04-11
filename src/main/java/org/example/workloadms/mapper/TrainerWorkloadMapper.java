package org.example.workloadms.mapper;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(source = "trainerUsername", target = "username")
    @Mapping(source = "trainerFirstName", target = "firstName")
    @Mapping(source = "trainerLastName", target = "lastName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "years", ignore = true)
    Trainer toEntity(TrainerWorkloadRequest request);
}
