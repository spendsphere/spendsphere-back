package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Информация о профиле пользователя")
public record UserProfileDTO(
    @Schema(description = "Идентификатор пользователя", example = "1") Long id,
    @Schema(description = "Электронная почта", example = "user@example.com") String email,
    @Schema(description = "Фамилия", example = "Иванов") String surname,
    @Schema(description = "Имя", example = "Иван") String name,
    @Schema(description = "Дата рождения", example = "1990-01-15") LocalDate birthday,
    @Schema(description = "URL фотографии профиля", example = "https://example.com/photo.jpg")
        String photoUrl,
    @Schema(description = "Дата и время создания профиля") LocalDateTime createdAt,
    @Schema(description = "Флаг премиум аккаунта", example = "false") Boolean isPremium) {}
