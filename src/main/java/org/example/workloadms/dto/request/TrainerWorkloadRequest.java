package org.example.workloadms.dto.request;

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

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private Date trainingDate;
    private Integer trainingDuration;
    private ActionType actionType;

}