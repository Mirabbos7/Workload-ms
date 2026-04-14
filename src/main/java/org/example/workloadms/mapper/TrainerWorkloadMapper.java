package org.example.workloadms.mapper;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
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

    default TrainerWorkloadResponse toResponse(String username, int year, int month, float workingHours) {
        return TrainerWorkloadResponse.builder()
                .trainerUsername(username)
                .year(String.valueOf(year))
                .month(String.valueOf(month))
                .workingHours(workingHours)
                .build();
    }
}
