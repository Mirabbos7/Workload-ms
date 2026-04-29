package org.example.workloadms.service.impl;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.entity.Trainer;
import org.example.workloadms.enums.ActionType;
import org.example.workloadms.exceptions.TrainerNotFoundException;
import org.example.workloadms.mapper.TrainerWorkloadMapper;
import org.example.workloadms.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        trainer.setIsActive(true);
        trainer.setYearList(new ArrayList<>());
    }

    @Test
    @DisplayName("Should create new trainer when not found in DB")
    void processWorkload_shouldCreateNewTrainer_whenNotExists() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(trainer);

        workloadService.processWorkload(request);

        verify(trainerRepository).save(trainer);
        assertThat(trainer.getYearList()).hasSize(1);
        assertThat(trainer.getYearList().get(0).getMonthList()).hasSize(1);
        assertThat(trainer.getYearList().get(0).getMonthList().get(0).getTrainingSummaryDuration())
                .isEqualTo(90);
    }

    @Test
    @DisplayName("Should update trainer names when trainer already exists")
    void processWorkload_shouldUpdateTrainerNames_whenExists() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        request.setTrainerFirstName("UpdatedFirst");
        request.setTrainerLastName("UpdatedLast");

        workloadService.processWorkload(request);

        assertThat(trainer.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(trainer.getLastName()).isEqualTo("UpdatedLast");
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("Should accumulate duration on second call")
    void processWorkload_shouldAccumulateDuration_onSecondCall() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        workloadService.processWorkload(request);
        workloadService.processWorkload(request);

        verify(trainerRepository, times(2)).save(trainer);
        int total = trainer.getYearList().get(0).getMonthList().get(0).getTrainingSummaryDuration();
        assertThat(total).isEqualTo(180);
    }

    @Test
    @DisplayName("Should subtract duration when action type is DELETE")
    void processWorkload_shouldSubtractDuration_whenActionIsDelete() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request);

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

        int total = trainer.getYearList().get(0).getMonthList().get(0).getTrainingSummaryDuration();
        assertThat(total).isEqualTo(60);
    }

    @Test
    @DisplayName("Should not go below zero when subtracting too much")
    void processWorkload_shouldNotGoBelowZero_whenSubtractingTooMuch() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request);

        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(200)
                .actionType(ActionType.DELETE)
                .build();

        workloadService.processWorkload(deleteRequest);

        int total = trainer.getYearList().get(0).getMonthList().get(0).getTrainingSummaryDuration();
        assertThat(total).isZero();
    }

    @Test
    @DisplayName("Should subtract duration when trainer isActive is false")
    void processWorkload_shouldSubtractDuration_whenTrainerIsInactive() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));
        workloadService.processWorkload(request);

        TrainerWorkloadRequest inactiveRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(false)
                .trainingDate(new Date())
                .trainingDuration(30)
                .actionType(ActionType.ADD)
                .build();

        workloadService.processWorkload(inactiveRequest);

        int total = trainer.getYearList().get(0).getMonthList().get(0).getTrainingSummaryDuration();
        assertThat(total).isEqualTo(60);
    }

    @Test
    @DisplayName("Should create new year entry when year does not exist")
    void processWorkload_shouldCreateNewYear_whenYearNotExists() {
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        workloadService.processWorkload(request);

        assertThat(trainer.getYearList()).hasSize(1);
        assertThat(trainer.getYearList().get(0).getYear()).isNotNull();
    }

    @Test
    @DisplayName("Should return correct working hours")
    void getTrainerWorkingHours_shouldReturnCorrectHours() {
        Trainer.Month month = Trainer.Month.builder()
                .month("4")
                .trainingSummaryDuration(90)
                .build();
        Trainer.Year year = Trainer.Year.builder()
                .year("2026")
                .monthList(new ArrayList<>(List.of(month)))
                .build();
        trainer.setYearList(new ArrayList<>(List.of(year)));

        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours("john.doe", 2026, 4);

        assertThat(response.getTrainerUsername()).isEqualTo("john.doe");
        assertThat(response.getYear()).isEqualTo("2026");
        assertThat(response.getMonth()).isEqualTo("4");
        assertThat(response.getWorkingHours()).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("Should throw TrainerNotFoundException when trainer not found")
    void getTrainerWorkingHours_shouldThrow_whenTrainerNotFound() {
        when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workloadService.getTrainerWorkingHours("unknown", 2026, 4))
                .isInstanceOf(TrainerNotFoundException.class)
                .hasMessage("Trainer not found");
    }

    @Test
    @DisplayName("Should return zero hours when year not found")
    void getTrainerWorkingHours_shouldReturnZero_whenYearNotFound() {
        trainer.setYearList(new ArrayList<>());
        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours("john.doe", 2026, 4);

        assertThat(response.getWorkingHours()).isZero();
    }

    @Test
    @DisplayName("Should return zero hours when month not found")
    void getTrainerWorkingHours_shouldReturnZero_whenMonthNotFound() {
        Trainer.Year year = Trainer.Year.builder()
                .year("2026")
                .monthList(new ArrayList<>())
                .build();
        trainer.setYearList(new ArrayList<>(List.of(year)));

        when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainer));

        TrainerWorkloadResponse response = workloadService.getTrainerWorkingHours("john.doe", 2026, 4);

        assertThat(response.getWorkingHours()).isZero();
    }
}