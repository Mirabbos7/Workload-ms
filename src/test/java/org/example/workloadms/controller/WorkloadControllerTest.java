package org.example.workloadms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.workloadms.config.service.JwtTokenService;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.dto.response.TrainerWorkloadResponse;
import org.example.workloadms.enums.ActionType;
import org.example.workloadms.exceptions.GlobalExceptionHandler;
import org.example.workloadms.exceptions.MonthNotFoundException;
import org.example.workloadms.exceptions.TrainerNotFoundException;
import org.example.workloadms.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({WorkloadController.class, GlobalExceptionHandler.class})
@WithMockUser
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkloadService workloadService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    void processWorkload_shouldReturn200_whenValidRequest() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(new Date())
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        doNothing().when(workloadService).processWorkload(any());

        mockMvc.perform(post("/api/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(workloadService).processWorkload(any());
    }

    @Test
    void getTrainerWorkingHours_shouldReturnResponse_whenFound() throws Exception {
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .year("2026")
                .month("4")
                .workingHours(1.5f)
                .build();

        when(workloadService.getTrainerWorkingHours("john.doe", 2026, 4))
                .thenReturn(response);

        mockMvc.perform(get("/api/workload/john.doe")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.year").value("2026"))
                .andExpect(jsonPath("$.month").value("4"))
                .andExpect(jsonPath("$.workingHours").value(1.5));

        verify(workloadService).getTrainerWorkingHours("john.doe", 2026, 4);
    }

    @Test
    void getTrainerWorkingHours_shouldReturn404_whenTrainerNotFound() throws Exception {
        when(workloadService.getTrainerWorkingHours(eq("unknown"), anyInt(), anyInt()))
                .thenThrow(new TrainerNotFoundException("Trainer not found"));

        mockMvc.perform(get("/api/workload/unknown")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrainerWorkingHours_shouldReturn404_whenMonthNotFound() throws Exception {
        when(workloadService.getTrainerWorkingHours(eq("john.doe"), eq(2026), eq(99)))
                .thenThrow(new MonthNotFoundException("Month not found"));

        mockMvc.perform(get("/api/workload/john.doe")
                        .param("year", "2026")
                        .param("month", "99"))
                .andExpect(status().isNotFound());
    }
}