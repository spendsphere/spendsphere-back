package ru.nsu.spendsphere.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.UserProfileCreateDTO;
import ru.nsu.spendsphere.models.dto.UserProfileDTO;
import ru.nsu.spendsphere.models.dto.UserProfileUpdateDTO;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.UserMapper;
import ru.nsu.spendsphere.repositories.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder encoder;

  public UserProfileDTO getProfile(Long id) {
    Optional<User> user = userRepository.findById(id);
    if (user.isEmpty()) {
      throw new ResourceNotFoundException("User with id " + id + " not found");
    } else {
      return userMapper.toUserProfileDTO(user.get());
    }
  }

  @Transactional
  public UserProfileDTO updateUserById(Long id, UserProfileUpdateDTO userInputDTO) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isEmpty()) {
      throw new ResourceNotFoundException("User with id " + id + " not found");
    }
    User user = userOptional.get();
    user.setSurname(userInputDTO.surname());
    user.setName(userInputDTO.name());
    user.setBirthday(userInputDTO.birthday());
    user.setPhotoUrl(userInputDTO.photoUrl());
    return userMapper.toUserProfileDTO(userRepository.save(user));
  }

  @Transactional
  public UserProfileDTO createProfile(UserProfileCreateDTO userInputDTO) {
    if (userRepository.existsByEmail(userInputDTO.email())) {
      throw new BadRequestException("User with email " + userInputDTO.email() + " already exists");
    }
    User user = new User();
    user.setEmail(userInputDTO.email());
    user.setPassword(encoder.encode(userInputDTO.password()));
    user.setSurname(userInputDTO.surname());
    user.setName(userInputDTO.name());
    user.setBirthday(userInputDTO.birthday());
    user.setPhotoUrl(userInputDTO.photoUrl());
    return userMapper.toUserProfileDTO(userRepository.save(user));
  }
}
