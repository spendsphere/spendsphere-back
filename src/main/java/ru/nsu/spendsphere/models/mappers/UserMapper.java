package ru.nsu.spendsphere.models.mappers;

import org.springframework.stereotype.Component;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.entities.User;

@Component
public class UserMapper {

  public UserProfileDTO toUserProfileDTO(User user) {
    if (user == null) return null;
    return new UserProfileDTO(
        user.getId(),
        user.getEmail(),
        user.getSurname(),
        user.getName(),
        user.getBirthday(),
        user.getPhotoUrl(),
        user.getCreatedAt(),
        user.getIsPremium());
  }
}
