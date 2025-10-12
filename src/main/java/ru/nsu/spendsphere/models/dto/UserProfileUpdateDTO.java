package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Данные для обновления профиля пользователя")
public record UserProfileUpdateDTO(
    @Schema(description = "Фамилия", example = "Петров") String surname,
    @Schema(description = "Имя", example = "Петр") String name,
    @Schema(description = "Дата рождения", example = "1995-05-20") LocalDate birthday,
    @Schema(description = "URL фотографии профиля", example = "https://example.com/new-photo.jpg")
        String photoUrl) {}
