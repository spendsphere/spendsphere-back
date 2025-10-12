package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Данные для создания нового профиля пользователя")
public record UserProfileCreateDTO(
    @NotBlank(message = "Email обязателен")
        @Email(message = "Email должен быть корректным")
        @Schema(description = "Электронная почта", example = "newuser@example.com", required = true)
        String email,
    @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        @Schema(description = "Пароль", example = "password123", required = true)
        String password,
    @NotBlank(message = "Фамилия обязательна")
        @Schema(description = "Фамилия", example = "Иванов", required = true)
        String surname,
    @NotBlank(message = "Имя обязательно")
        @Schema(description = "Имя", example = "Иван", required = true)
        String name,
    @Schema(description = "Дата рождения", example = "1990-01-15") LocalDate birthday,
    @Schema(description = "URL фотографии профиля", example = "https://example.com/photo.jpg")
        String photoUrl) {}
