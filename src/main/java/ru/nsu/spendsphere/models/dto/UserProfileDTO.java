package ru.nsu.spendsphere.models.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserProfileDTO(
    Long id,
    String email,
    String surname,
    String name,
    LocalDate birthday,
    String photoUrl,
    LocalDateTime createdAt,
    Boolean isPremium) {}
