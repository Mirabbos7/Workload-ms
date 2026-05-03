package org.example.workloadms.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.workloadms.enums.ActionType;

import java.util.Date;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerWorkloadRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean isActive;

    @NotNull
    private Date trainingDate;

    @NotNull
    @Positive
    @Max(44640)
    private Integer trainingDuration;

    @NotNull
    private ActionType actionType;
}