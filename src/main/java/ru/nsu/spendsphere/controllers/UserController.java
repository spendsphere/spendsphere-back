package ru.nsu.spendsphere.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.UserProfileCreateDTO;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.dto.UserProfileUpdateDTO;
import ru.nsu.spendsphere.services.UserService;

@Tag(
    name = "Управление пользователями",
    description = "API для создания, получения и обновления профилей пользователей")
@RestController
@RequestMapping({"/api/v1/user", "/v1/user"})
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @Operation(
      summary = "Получение профиля пользователя",
      description = "Возвращает информацию о профиле пользователя по его идентификатору")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Профиль пользователя успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/profile/{id}")
  public UserProfileDTO getProfile(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long id) {
    return userService.getProfile(id);
  }

  @Operation(
      summary = "Обновление профиля пользователя",
      description = "Обновляет данные профиля пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Профиль пользователя успешно обновлен",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @PutMapping("/profile/{id}")
  public UserProfileDTO updateProfile(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable Long id,
      @Parameter(description = "Данные для обновления профиля", required = true) @RequestBody
          UserProfileUpdateDTO dto) {
    return userService.updateUserById(id, dto);
  }

  @Operation(
      summary = "Создание нового профиля пользователя",
      description = "Создает новый профиль пользователя с указанными данными")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Профиль пользователя успешно создан",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Пользователь с таким email уже существует",
            content = @Content)
      })
  @PostMapping("/profile")
  public UserProfileDTO createProfile(
      @Parameter(description = "Данные для создания нового профиля", required = true) @RequestBody
          UserProfileCreateDTO dto) {
    return userService.createProfile(dto);
  }
}
