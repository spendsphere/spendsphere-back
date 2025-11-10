package ru.nsu.spendsphere.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.UserProfileCreateDTO;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.dto.UserProfileUpdateDTO;
import ru.nsu.spendsphere.services.UserService;

/** Юнит-тесты для {@link UserController}. */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * Тест успешного получения профиля пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getProfileSuccess() throws Exception {
    Long userId = 1L;
    UserProfileDTO expectedDto =
        new UserProfileDTO(
            userId,
            "test@example.com",
            "Ivanov",
            "Ivan",
            LocalDate.of(1990, 1, 1),
            "http://example.com/photo.jpg",
            LocalDateTime.now(),
            false);

    when(userService.getProfile(userId)).thenReturn(expectedDto);

    mockMvc
        .perform(get("/api/v1/user/profile/{id}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.surname").value("Ivanov"))
        .andExpect(jsonPath("$.name").value("Ivan"))
        .andExpect(jsonPath("$.isPremium").value(false));
  }

  /**
   * Тест получения профиля несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getProfileUserNotFound() throws Exception {
    Long userId = 999L;
    when(userService.getProfile(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc.perform(get("/api/v1/user/profile/{id}", userId)).andExpect(status().isNotFound());
  }

  /**
   * Тест успешного обновления профиля пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateProfileSuccess() throws Exception {
    Long userId = 1L;
    UserProfileUpdateDTO updateDto =
        new UserProfileUpdateDTO(
            "Petrov", "Petr", LocalDate.of(1995, 5, 15), "http://example.com/new-photo.jpg");

    UserProfileDTO expectedDto =
        new UserProfileDTO(
            userId,
            "test@example.com",
            "Petrov",
            "Petr",
            LocalDate.of(1995, 5, 15),
            "http://example.com/new-photo.jpg",
            LocalDateTime.now(),
            false);

    when(userService.updateUserById(eq(userId), any(UserProfileUpdateDTO.class)))
        .thenReturn(expectedDto);

    mockMvc
        .perform(
            put("/api/v1/user/profile/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.surname").value("Petrov"))
        .andExpect(jsonPath("$.name").value("Petr"));
  }

  /**
   * Тест обновления профиля несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateProfileUserNotFound() throws Exception {
    Long userId = 999L;
    UserProfileUpdateDTO updateDto =
        new UserProfileUpdateDTO("Petrov", "Petr", LocalDate.of(1995, 5, 15), null);

    when(userService.updateUserById(eq(userId), any(UserProfileUpdateDTO.class)))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(
            put("/api/v1/user/profile/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного создания нового профиля пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createProfileSuccess() throws Exception {
    UserProfileCreateDTO createDto =
        new UserProfileCreateDTO(
            "newuser@example.com",
            "password123",
            "Sidorov",
            "Sidr",
            LocalDate.of(2000, 12, 25),
            "http://example.com/avatar.jpg");

    UserProfileDTO expectedDto =
        new UserProfileDTO(
            1L,
            "newuser@example.com",
            "Sidorov",
            "Sidr",
            LocalDate.of(2000, 12, 25),
            "http://example.com/avatar.jpg",
            LocalDateTime.now(),
            false);

    when(userService.createProfile(any(UserProfileCreateDTO.class))).thenReturn(expectedDto);

    mockMvc
        .perform(
            post("/api/v1/user/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.surname").value("Sidorov"))
        .andExpect(jsonPath("$.name").value("Sidr"));
  }

  /**
   * Тест создания профиля с существующим email.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createProfileEmailAlreadyExists() throws Exception {
    UserProfileCreateDTO createDto =
        new UserProfileCreateDTO(
            "existing@example.com", "password123", "Test", "User", LocalDate.of(1990, 1, 1), null);

    when(userService.createProfile(any(UserProfileCreateDTO.class)))
        .thenThrow(new BadRequestException("User with email existing@example.com already exists"));

    mockMvc
        .perform(
            post("/api/v1/user/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isBadRequest());
  }
}
