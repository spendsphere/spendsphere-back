package ru.nsu.spendsphere.controllers;

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

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/profile/{id}")
  public UserProfileDTO getProfile(@PathVariable Long id) {
    return userService.getProfile(id);
  }

  @PutMapping("/profile/{id}")
  public UserProfileDTO updateProfile(
      @PathVariable Long id, @RequestBody UserProfileUpdateDTO dto) {
    return userService.updateUserById(id, dto);
  }

  @PostMapping("/profile")
  public UserProfileDTO createProfile(@RequestBody UserProfileCreateDTO dto) {
    return userService.createProfile(dto);
  }

}
