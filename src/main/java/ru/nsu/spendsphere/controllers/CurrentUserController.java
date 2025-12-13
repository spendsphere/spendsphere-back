package ru.nsu.spendsphere.controllers;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.UserMapper;
import ru.nsu.spendsphere.repositories.UserRepository;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class CurrentUserController {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @GetMapping("/me")
  public ResponseEntity<UserProfileDTO> me(Authentication authentication) {
    log.info("=== CurrentUserController.me() called ===");

    if (authentication == null) {
      log.error("Authentication is null");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    log.info("Principal class: {}", authentication.getPrincipal().getClass().getName());
    log.info("Principal: {}", authentication.getPrincipal());

    Long userId;

    // 1. Проверяем, если principal - ваш User entity
    if (authentication.getPrincipal() instanceof User) {
      User user = (User) authentication.getPrincipal();
      userId = user.getId();
      log.info("Found User entity with ID: {}", userId);
    }
    // 2. Проверяем, если principal - UserDetails
    else if (authentication.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String email = userDetails.getUsername();
      log.info("Found UserDetails with username: {}", email);

      // Ищем пользователя по email
      Optional<User> userOpt = userRepository.findByEmail(email);
      if (userOpt.isPresent()) {
        userId = userOpt.get().getId();
        log.info("Found user ID from email: {}", userId);
      } else {
        userId = null;
      }
    }
    // 3. Проверяем details аутентификации
    else if (authentication.getDetails() instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
      if (details.containsKey("localUserId")) {
        userId = ((Number) details.get("localUserId")).longValue();
        log.info("Found user ID from details: {}", userId);
      } else {
        userId = null;
      }
    } else {
      userId = null;
    }

    if (userId == null) {
      log.error("Could not extract user ID from authentication");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    log.info("Looking for user with ID: {}", userId);
    return userRepository
        .findById(userId)
        .map(
            user -> {
              log.info("User found: {}", user.getEmail());
              return ResponseEntity.ok(userMapper.toUserProfileDTO(user));
            })
        .orElseGet(
            () -> {
              log.error("User not found in database with ID: {}", userId);
              return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            });
  }
}
