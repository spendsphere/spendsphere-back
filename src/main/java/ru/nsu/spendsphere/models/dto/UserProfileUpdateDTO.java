package ru.nsu.spendsphere.models.dto;

import java.time.LocalDate;

public record UserProfileUpdateDTO(
    String surname, String name, LocalDate birthday, String photoUrl) {}
