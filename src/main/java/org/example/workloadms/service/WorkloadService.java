package org.example.workloadms.service;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;

public interface WorkloadService {

    void processWorkload(TrainerWorkloadRequest request);

    TrainerWorkloadResponse getTrainerWorkingHours(String username, int year, int month);

}
