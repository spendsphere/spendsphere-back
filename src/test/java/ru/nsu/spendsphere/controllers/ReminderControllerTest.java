package ru.nsu.spendsphere.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nsu.spendsphere.configurations.security.SecurityConfig;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.ReminderCreateDTO;
import ru.nsu.spendsphere.models.dto.ReminderDTO;
import ru.nsu.spendsphere.models.dto.ReminderUpdateDTO;
import ru.nsu.spendsphere.models.entities.RecurrenceType;
import ru.nsu.spendsphere.services.ReminderService;

@WebMvcTest(ReminderController.class)
@Import(SecurityConfig.class)
class ReminderControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ReminderService reminderService;

  @Test
  void createReminderSuccess() throws Exception {
    Long userId = 1L;
    ReminderCreateDTO createDTO =
        new ReminderCreateDTO(
            "Оплата",
            null,
            new BigDecimal("100.00"),
            RecurrenceType.DAILY,
            null,
            null,
            null,
            true,
            2L);

    ReminderDTO response =
        new ReminderDTO(
            10L,
            userId,
            "Оплата",
            null,
            new BigDecimal("100.00"),
            RecurrenceType.DAILY,
            null,
            null,
            false,
            true,
            2L,
            "Основная",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(reminderService.create(eq(userId), any(ReminderCreateDTO.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/users/{userId}/reminders", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(10L))
        .andExpect(jsonPath("$.title").value("Оплата"))
        .andExpect(jsonPath("$.recurrenceType").value("DAILY"));
  }

  @Test
  void getUpcomingSuccess() throws Exception {
    Long userId = 1L;
    ReminderDTO r1 =
        new ReminderDTO(
            10L,
            userId,
            "Оплата",
            null,
            new BigDecimal("100.00"),
            RecurrenceType.WEEKLY,
            DayOfWeek.MONDAY,
            null,
            false,
            true,
            2L,
            "Основная",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(reminderService.getUpcoming(userId, 5)).thenReturn(java.util.List.of(r1));

    mockMvc
        .perform(get("/api/v1/users/{userId}/reminders/upcoming", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10L));
  }

  @Test
  void getByIdNotFound() throws Exception {
    Long userId = 1L;
    Long reminderId = 999L;
    when(reminderService.getById(userId, reminderId))
        .thenThrow(
            new ResourceNotFoundException(
                "Reminder with id " + reminderId + " not found for user " + userId));

    mockMvc
        .perform(get("/api/v1/users/{userId}/reminders/{reminderId}", userId, reminderId))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateReminderSuccess() throws Exception {
    Long userId = 1L;
    Long reminderId = 10L;
    ReminderUpdateDTO updateDTO =
        new ReminderUpdateDTO(
            "Оплата (upd)",
            null,
            new BigDecimal("120.00"),
            RecurrenceType.DAILY,
            null,
            null,
            null,
            true,
            2L);

    ReminderDTO response =
        new ReminderDTO(
            reminderId,
            userId,
            "Оплата (upd)",
            null,
            new BigDecimal("120.00"),
            RecurrenceType.DAILY,
            null,
            null,
            false,
            true,
            2L,
            "Основная",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(reminderService.update(eq(userId), eq(reminderId), any(ReminderUpdateDTO.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/v1/users/{userId}/reminders/{reminderId}", userId, reminderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Оплата (upd)"))
        .andExpect(jsonPath("$.amount").value(120.00));
  }

  @Test
  void deleteReminderSuccess() throws Exception {
    Long userId = 1L;
    Long reminderId = 10L;
    doNothing().when(reminderService).delete(userId, reminderId);

    mockMvc
        .perform(delete("/api/v1/users/{userId}/reminders/{reminderId}", userId, reminderId))
        .andExpect(status().isNoContent());
  }
}
