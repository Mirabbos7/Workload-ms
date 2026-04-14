package org.example.workloadms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.entity.MonthSummary;
import org.example.workloadms.entity.Trainer;
import org.example.workloadms.entity.YearSummary;
import org.example.workloadms.enums.ActionType;
import org.example.workloadms.exceptions.TrainerNotFoundException;
import org.example.workloadms.mapper.TrainerWorkloadMapper;
import org.example.workloadms.repository.TrainerRepository;
import org.example.workloadms.service.WorkloadService;
import org.springframework.stereotype.Service;

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
    public void processWorkload(TrainerWorkloadRequest request) {
        LocalDate localDate = request.getTrainingDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int duration = calculateDurationDelta(request);

        Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .map(existing -> updateTrainerNames(existing, request))
                .orElseGet(() -> mapper.toEntity(request));

        updateTrainerRecord(trainer, year, month, duration);
        trainerRepository.save(trainer);
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

    private void updateTrainerRecord(Trainer trainer, int year, int month, int durationMinutes) {
        List<YearSummary> years = trainer.getYears();

        YearSummary yearEntry = years.stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setMonthSummary(new ArrayList<>());
                    years.add(newYear);
                    return newYear;
                });

        List<MonthSummary> months = yearEntry.getMonthSummary();
        MonthSummary monthEntry = months.stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setDurationInMinutes(0);
                    months.add(newMonth);
                    return newMonth;
                });

        int newTotal = monthEntry.getDurationInMinutes() + durationMinutes;
        monthEntry.setDurationInMinutes(Math.max(0, newTotal));
    }

    @Override
    public TrainerWorkloadResponse getTrainerWorkingHours(String username, int year, int month) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found"));

        int durationInMinutes = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .map(yearSummary -> yearSummary.getMonthSummary().stream()
                        .filter(m -> m.getMonth() == month)
                        .findFirst()
                        .map(MonthSummary::getDurationInMinutes)
                        .orElse(0))
                .orElse(0);

        return mapper.toResponse(username, year, month, durationInMinutes / 60F);
    }
}