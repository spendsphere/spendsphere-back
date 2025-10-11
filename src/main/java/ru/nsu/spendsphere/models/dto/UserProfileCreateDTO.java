package ru.nsu.spendsphere.models.dto;

import java.time.LocalDate;

public record UserProfileCreateDTO(
    String email,
    String password,
    String surname,
    String name,
    LocalDate birthday,
    String photoUrl) {}
