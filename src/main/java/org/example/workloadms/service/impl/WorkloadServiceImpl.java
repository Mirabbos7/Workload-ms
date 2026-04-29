package org.example.workloadms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.entity.Trainer;
import org.example.workloadms.enums.ActionType;
import org.example.workloadms.exceptions.TrainerNotFoundException;
import org.example.workloadms.mapper.TrainerWorkloadMapper;
import org.example.workloadms.repository.TrainerRepository;
import org.example.workloadms.service.WorkloadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerRepository trainerRepository;
    private final TrainerWorkloadMapper mapper;

    @Override
    @Transactional
    public void processWorkload(TrainerWorkloadRequest request) {
        log.info("Received processWorkload request for trainer: {}", request.getTrainerUsername());

        LocalDate localDate = request.getTrainingDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int duration = calculateDurationDelta(request);

        Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .map(existing -> {
                    log.debug("Trainer found: {}, updating names", existing.getUsername());
                    return updateTrainerNames(existing, request);
                })
                .orElseGet(() -> {
                    log.info("Trainer not found, creating new document for: {}", request.getTrainerUsername());
                    return mapper.toEntity(request);
                });

        updateTrainerRecord(trainer, year, month, duration);
        trainerRepository.save(trainer);
        log.info("Successfully processed workload for trainer: {}", request.getTrainerUsername());
    }

    @Override
    public TrainerWorkloadResponse getTrainerWorkingHours(String username, int year, int month) {
        log.info("Fetching working hours for trainer: {}, year: {}, month: {}", username, year, month);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer not found: {}", username);
                    return new TrainerNotFoundException("Trainer not found");
                });

        int durationInMinutes = trainer.getYearList().stream()
                .filter(y -> y.getYear().equals(String.valueOf(year)))
                .findFirst()
                .flatMap(y -> y.getMonthList().stream()
                        .filter(m -> m.getMonth().equals(String.valueOf(month)))
                        .findFirst())
                .map(Trainer.Month::getTrainingSummaryDuration)
                .orElse(0);

        log.info("Returning working hours for trainer: {}, duration: {} min", username, durationInMinutes);

        return new TrainerWorkloadResponse(username, String.valueOf(year), String.valueOf(month), durationInMinutes / 60F);
    }

    private int calculateDurationDelta(TrainerWorkloadRequest request) {
        boolean shouldSubtract = request.getActionType() == ActionType.DELETE
                || !request.getIsActive();
        int durationMinutes = request.getTrainingDuration();
        return shouldSubtract ? -durationMinutes : durationMinutes;
    }

    private Trainer updateTrainerNames(Trainer trainer, TrainerWorkloadRequest request) {
        trainer.setFirstName(request.getTrainerFirstName());
        trainer.setLastName(request.getTrainerLastName());
        return trainer;
    }

    private void updateTrainerRecord(Trainer trainer, int year, int month, int durationDelta) {
        if (trainer.getYearList() == null) {
            trainer.setYearList(new ArrayList<>());
        }

        List<Trainer.Year> years = trainer.getYearList();

        Trainer.Year yearEntry = years.stream()
                .filter(y -> y.getYear().equals(String.valueOf(year)))
                .findFirst()
                .orElseGet(() -> {
                    Trainer.Year newYear = Trainer.Year.builder()
                            .year(String.valueOf(year))
                            .monthList(new ArrayList<>())
                            .build();
                    years.add(newYear);
                    return newYear;
                });

        List<Trainer.Month> months = yearEntry.getMonthList();

        Trainer.Month monthEntry = months.stream()
                .filter(m -> m.getMonth().equals(String.valueOf(month)))
                .findFirst()
                .orElseGet(() -> {
                    Trainer.Month newMonth = Trainer.Month.builder()
                            .month(String.valueOf(month))
                            .trainingSummaryDuration(0)
                            .build();
                    months.add(newMonth);
                    return newMonth;
                });
        int oldDuration = monthEntry.getTrainingSummaryDuration();
        int newDuration = Math.max(0, oldDuration + durationDelta);
        log.debug("Updating duration: year={}, month={}, {} -> {}", year, month, oldDuration, newDuration);

        monthEntry.setTrainingSummaryDuration(newDuration);
    }
}