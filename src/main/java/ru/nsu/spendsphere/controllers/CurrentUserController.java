package ru.nsu.spendsphere.controllers;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.mappers.UserMapper;
import ru.nsu.spendsphere.repositories.UserRepository;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CurrentUserController {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @GetMapping("/me")
  public ResponseEntity<UserProfileDTO> me(Authentication authentication) {
    if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    Object principal = authentication.getPrincipal();
    AtomicReference<Long> localUserId = new AtomicReference<>();

    if (principal instanceof DefaultOAuth2User) {
      DefaultOAuth2User oauthUser = (DefaultOAuth2User) principal;
      Object idObj = oauthUser.getAttributes().get("localUserId");
      if (idObj != null) {
        localUserId.set(Long.valueOf(String.valueOf(idObj)));
      }
    } else if (principal instanceof org.springframework.security.core.userdetails.User) {
      String username =
          ((org.springframework.security.core.userdetails.User) principal).getUsername();
      userRepository.findByEmail(username).ifPresent(u -> localUserId.set(u.getId()));
    }

    Long idValue = localUserId.get();

    if (idValue == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return userRepository
        .findById(idValue)
        .map(user -> ResponseEntity.ok(userMapper.toUserProfileDTO(user)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
