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

/**
 * Сервис для управления пользователями. Предоставляет бизнес-логику для работы с профилями
 * пользователей, включая получение, создание и обновление данных.
 */
@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder encoder;

  /**
   * Получение профиля пользователя по идентификатору.
   *
   * @param id идентификатор пользователя
   * @return DTO с данными профиля пользователя
   * @throws ResourceNotFoundException если пользователь с указанным ID не найден
   */
  public UserProfileDTO getProfile(Long id) {
    Optional<User> user = userRepository.findById(id);
    if (user.isEmpty()) {
      throw new ResourceNotFoundException("User with id " + id + " not found");
    } else {
      return userMapper.toUserProfileDTO(user.get());
    }
  }

  /**
   * Обновление данных профиля пользователя.
   *
   * @param id идентификатор пользователя
   * @param userInputDTO DTO с обновленными данными профиля
   * @return DTO с обновленными данными профиля пользователя
   * @throws ResourceNotFoundException если пользователь с указанным ID не найден
   */
  @Transactional
  public UserProfileDTO updateUserById(Long id, UserProfileUpdateDTO userInputDTO) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isEmpty()) {
      throw new ResourceNotFoundException("User with id " + id + " not found");
    }
    User user = userOptional.get();
    if (userInputDTO.surname() != null) {
      user.setSurname(userInputDTO.surname());
    }
    if (userInputDTO.name() != null) {
      user.setName(userInputDTO.name());
    }
    if (userInputDTO.birthday() != null) {
      user.setBirthday(userInputDTO.birthday());
    }
    if (userInputDTO.photoUrl() != null) {
      user.setPhotoUrl(userInputDTO.photoUrl());
    }
    return userMapper.toUserProfileDTO(userRepository.save(user));
  }

  /**
   * Создание нового профиля пользователя.
   *
   * @param userInputDTO DTO с данными для создания профиля
   * @return DTO созданного профиля пользователя
   * @throws BadRequestException если пользователь с указанным email уже существует
   */
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
