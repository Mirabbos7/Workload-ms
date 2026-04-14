package org.example.workloadms.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerWorkloadResponse {
    private String trainerUsername;
    private String year;
    private String month;
    private Float workingHours;
}