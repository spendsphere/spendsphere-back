package ru.nsu.spendsphere.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userReq) throws OAuth2AuthenticationException {
    log.info("=== Starting OAuth2 login process ===");

    OAuth2User oAuth2User = loadOAuth2User(userReq);
    Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
    String registrationId = userReq.getClientRegistration().getRegistrationId();

    OAuthUserData userData = extractUserData(attributes, registrationId);
    validateProviderId(userData.getProviderId(), attributes);

    try {
      User user = processUser(registrationId, userData);
      return createOAuth2User(attributes, user);
    } catch (Exception e) {
      log.error("Error during OAuth2 user processing", e);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("user_creation_failed"),
          "Failed to process or save OAuth2 user: " + e.getMessage());
    }
  }

  private OAuth2User loadOAuth2User(OAuth2UserRequest userReq) {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    return delegate.loadUser(userReq);
  }

  private OAuthUserData extractUserData(Map<String, Object> attributes, String registrationId) {
    String email = (String) attributes.get("email");
    String name = (String) attributes.getOrDefault("name", null);
    String picture = (String) attributes.getOrDefault("picture", null);
    String providerId = (String) attributes.getOrDefault("sub", attributes.get("id"));

    log.info("OAuth2 provider: {}", registrationId);
    log.info("Attributes from provider: {}", attributes);
    log.info("Parsed values -> email: {}, name: {}, providerId: {}", email, name, providerId);

    return OAuthUserData.builder()
        .email(email)
        .name(name)
        .picture(picture)
        .providerId(providerId)
        .build();
  }

  private void validateProviderId(String providerId, Map<String, Object> attributes) {
    if (providerId == null) {
      log.error("No provider id found (sub). Attributes = {}", attributes);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_user"), "No provider id (sub) present");
    }
  }

  private User processUser(String registrationId, OAuthUserData userData) {
    Optional<User> existingUser =
        userRepository.findByProviderAndProviderId(registrationId, userData.getProviderId());

    if (existingUser.isPresent()) {
      return updateExistingUser(existingUser.get(), userData);
    } else {
      return createOrLinkUser(registrationId, userData);
    }
  }

  private User updateExistingUser(User user, OAuthUserData userData) {
    log.info("Found existing user by providerId: id={}, email={}", user.getId(), user.getEmail());

    boolean updated = false;

    if (userData.getName() != null && !userData.getName().equals(user.getName())) {
      log.debug("Updating name: {} -> {}", user.getName(), userData.getName());
      user.setName(userData.getName());
      updated = true;
    }

    if (userData.getPicture() != null
        && !Objects.equals(userData.getPicture(), user.getPhotoUrl())) {
      log.debug("Updating photo: {} -> {}", user.getPhotoUrl(), userData.getPicture());
      user.setPhotoUrl(userData.getPicture());
      updated = true;
    }

    if (updated) {
      userRepository.save(user);
      log.info("Updated existing user: id={}", user.getId());
    }

    return user;
  }

  private User createOrLinkUser(String registrationId, OAuthUserData userData) {
    log.info(
        "No user found by providerId={}, provider={}", userData.getProviderId(), registrationId);

    if (userData.getEmail() != null) {
      Optional<User> byEmail = userRepository.findByEmail(userData.getEmail());
      if (byEmail.isPresent()) {
        return linkExistingUserWithProvider(byEmail.get(), registrationId, userData);
      } else {
        return createNewUser(registrationId, userData);
      }
    } else {
      return createSyntheticUser(registrationId, userData);
    }
  }

  private User linkExistingUserWithProvider(
      User user, String registrationId, OAuthUserData userData) {
    log.info(
        "Found existing user by email={}, linking with provider {}",
        userData.getEmail(),
        registrationId);

    user.setProvider(registrationId);
    user.setProviderId(userData.getProviderId());
    if (userData.getPicture() != null) user.setPhotoUrl(userData.getPicture());
    if (userData.getName() != null) user.setName(userData.getName());

    userRepository.save(user);
    log.info("Linked OAuth provider to existing user id={}", user.getId());
    return user;
  }

  private User createNewUser(String registrationId, OAuthUserData userData) {
    log.info("Creating new user with email={}", userData.getEmail());

    User user =
        User.builder()
            .email(userData.getEmail())
            .name(userData.getName() != null ? userData.getName() : userData.getEmail())
            .provider(registrationId)
            .providerId(userData.getProviderId())
            .photoUrl(userData.getPicture())
            .build();

    user = userRepository.save(user);
    log.info("Created new user id={} email={}", user.getId(), user.getEmail());
    return user;
  }

  private User createSyntheticUser(String registrationId, OAuthUserData userData) {
    log.warn("Provider did not return email. Creating synthetic user.");

    String syntheticEmail = registrationId + "_" + userData.getProviderId() + "@noemail.local";
    User user =
        User.builder()
            .email(syntheticEmail)
            .name(userData.getName() != null ? userData.getName() : syntheticEmail)
            .provider(registrationId)
            .providerId(userData.getProviderId())
            .photoUrl(userData.getPicture())
            .build();

    user = userRepository.save(user);
    log.info("Created synthetic user id={} email={}", user.getId(), user.getEmail());
    return user;
  }

  private OAuth2User createOAuth2User(Map<String, Object> attributes, User user) {
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    Map<String, Object> mappedAttributes = new HashMap<>(attributes);
    mappedAttributes.put("localUserId", user.getId());

    log.info(
        "OAuth2 login completed successfully for user id={}, providerId={}",
        user.getId(),
        user.getProviderId());

    return new DefaultOAuth2User(authorities, mappedAttributes, "sub");
  }

  @Builder
  @Getter
  private static class OAuthUserData {
    private final String email;
    private final String name;
    private final String picture;
    private final String providerId;
  }
}
