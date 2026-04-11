package org.example.workloadms.service.impl;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.entity.MonthSummary;
import org.example.workloadms.entity.Trainer;
import org.example.workloadms.entity.YearSummary;
import org.example.workloadms.enums.ActionType;
import org.example.workloadms.exceptions.MonthNotFoundException;
import org.example.workloadms.exceptions.TrainerNotFoundException;
import org.example.workloadms.exceptions.YearNotFoundException;
import org.example.workloadms.mapper.TrainerWorkloadMapper;
import org.example.workloadms.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainerWorkloadMapper mapper;

    @InjectMocks
    private WorkloadServiceImpl workloadService;

    private TrainerWorkloadRequest request;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        trainer = new Trainer();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
    }

    @Test
    void processWorkload_shouldCreateNewTrainer_whenNotExists() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(trainer);

        workloadService.processWorkload(request);

        verify(trainerRepository).save(trainer);
        assertThat(trainer.getYears()).hasSize(1);
        assertThat(trainer.getYears().get(0).getMonthSummary()).hasSize(1);
        assertThat(trainer.getYears().get(0).getMonthSummary().get(0).getDurationInMinutes())
                .isEqualTo(90);
    }

    @Test
    void processWorkload_shouldUpdateExistingTrainer_whenExists() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        workloadService.processWorkload(request);
        workloadService.processWorkload(request);

        verify(trainerRepository, times(2)).save(trainer);
        int total = trainer.getYears().get(0).getMonthSummary().get(0).getDurationInMinutes();
        assertThat(total).isEqualTo(180);
    }

    @Test
    void processWorkload_shouldSubtractDuration_whenActionIsDelete() {
        // сначала добавим 90 минут
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request);

        // теперь удаляем 30 минут
        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(30)
                .actionType(ActionType.DELETE)
                .build();

        workloadService.processWorkload(deleteRequest);

        int total = trainer.getYears().get(0).getMonthSummary().get(0).getDurationInMinutes();
        assertThat(total).isEqualTo(60);
    }

    @Test
    void processWorkload_shouldNotGoBelowZero_whenSubtractingTooMuch() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request); // +90

        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(200)
                .actionType(ActionType.DELETE)
                .build();

        workloadService.processWorkload(deleteRequest); // -200, должно стать 0

        int total = trainer.getYears().get(0).getMonthSummary().get(0).getDurationInMinutes();
        assertThat(total).isEqualTo(0);
    }

    @Test
    void processWorkload_shouldSubtractDuration_whenTrainerIsInactive() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request); // +90

        TrainerWorkloadRequest inactiveRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(false) // неактивный — вычитаем
                .trainingDate(new Date())
                .trainingDuration(30)
                .actionType(ActionType.ADD)
                .build();

        workloadService.processWorkload(inactiveRequest);

        int total = trainer.getYears().get(0).getMonthSummary().get(0).getDurationInMinutes();
        assertThat(total).isEqualTo(60);
    }

    @Test
    void getTrainerWorkingHours_shouldReturnCorrectHours() {
        YearSummary year = new YearSummary();
        year.setYear(2026);
        MonthSummary month = new MonthSummary();
        month.setMonth(4);
        month.setDurationInMinutes(90);
        year.setMonthSummary(List.of(month));
        trainer.setYears(List.of(year));

        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours("john.doe", 2026, 4);

        assertThat(response.getTrainerUsername()).isEqualTo("john.doe");
        assertThat(response.getYear()).isEqualTo("2026");
        assertThat(response.getMonth()).isEqualTo("4");
        assertThat(response.getWorkingHours()).isEqualTo(1.5f);
    }

    @Test
    void getTrainerWorkingHours_shouldThrow_whenTrainerNotFound() {
        when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workloadService.getTrainerWorkingHours("unknown", 2026, 4))
                .isInstanceOf(TrainerNotFoundException.class)
                .hasMessage("Trainer not found");
    }

    @Test
    void getTrainerWorkingHours_shouldThrow_whenYearNotFound() {
        trainer.setYears(new ArrayList<>());
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> workloadService.getTrainerWorkingHours("john.doe", 2026, 4))
                .isInstanceOf(YearNotFoundException.class)
                .hasMessage("Year not found");
    }

    @Test
    void getTrainerWorkingHours_shouldThrow_whenMonthNotFound() {
        YearSummary year = new YearSummary();
        year.setYear(2026);
        year.setMonthSummary(new ArrayList<>());
        trainer.setYears(List.of(year));

        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> workloadService.getTrainerWorkingHours("john.doe", 2026, 4))
                .isInstanceOf(MonthNotFoundException.class)
                .hasMessage("Month not found");
    }
}