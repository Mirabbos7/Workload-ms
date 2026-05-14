package org.example.workloadms.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.service.WorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkingHours(
            @PathVariable String username,
            @RequestParam @Min(2000) @Max(2026) int year,
            @RequestParam @Min(value = 1, message = "Month must be at least 1")
            @Max(value = 12, message = "Month must not be greater than 12") int month) {
        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours(username, year, month);
        return ResponseEntity.ok(response);
    }
}
