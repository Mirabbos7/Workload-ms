package org.example.workloadms.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.service.WorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam int year,
            @RequestParam int month) {
        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours(username, year, month);
        return ResponseEntity.ok(response);
    }
}
