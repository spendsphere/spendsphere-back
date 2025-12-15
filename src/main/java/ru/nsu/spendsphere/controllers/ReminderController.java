package ru.nsu.spendsphere.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.ReminderCreateDTO;
import ru.nsu.spendsphere.models.dto.ReminderDTO;
import ru.nsu.spendsphere.models.dto.ReminderUpdateDTO;
import ru.nsu.spendsphere.services.ReminderService;

@Tag(
    name = "Напоминания",
    description = "API для создания, получения, обновления и удаления напоминаний")
@RestController
@RequestMapping({"/api/v1/users/{userId}/reminders", "/v1/users/{userId}/reminders"})
@RequiredArgsConstructor
public class ReminderController {

  private final ReminderService reminderService;

  @Operation(summary = "Получить все напоминания пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ReminderDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping
  public List<ReminderDTO> getAll(@PathVariable Long userId) {
    return reminderService.getAll(userId);
  }

  @Operation(summary = "Получить напоминание по id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReminderDTO.class))),
        @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content)
      })
  @GetMapping("/{reminderId}")
  public ReminderDTO getById(@PathVariable Long userId, @PathVariable Long reminderId) {
    return reminderService.getById(userId, reminderId);
  }

  @Operation(summary = "Создать напоминание")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Создано",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReminderDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь/счет не найден",
            content = @Content),
        @ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content)
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReminderDTO create(
      @PathVariable Long userId, @Valid @RequestBody ReminderCreateDTO createDTO) {
    return reminderService.create(userId, createDTO);
  }

  @Operation(summary = "Обновить напоминание")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReminderDTO.class))),
        @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content),
        @ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content)
      })
  @PutMapping("/{reminderId}")
  public ReminderDTO update(
      @PathVariable Long userId,
      @PathVariable Long reminderId,
      @Valid @RequestBody ReminderUpdateDTO updateDTO) {
    return reminderService.update(userId, reminderId, updateDTO);
  }

  @Operation(summary = "Удалить напоминание")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Удалено"),
        @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content)
      })
  @DeleteMapping("/{reminderId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long userId, @PathVariable Long reminderId) {
    reminderService.delete(userId, reminderId);
  }

  @Operation(summary = "Напоминания в ближайшие дни")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ReminderDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/upcoming")
  public List<ReminderDTO> upcoming(
      @PathVariable Long userId, @RequestParam(name = "days", defaultValue = "5") int days) {
    return reminderService.getUpcoming(userId, days);
  }
}
