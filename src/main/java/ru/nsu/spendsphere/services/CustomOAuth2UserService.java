package ru.nsu.spendsphere.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    log.info("=== Starting OAuth2 login process ===");

    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    String email = (String) attributes.get("email");
    String name = (String) attributes.getOrDefault("name", null);
    String picture = (String) attributes.getOrDefault("picture", null);
    String providerId = (String) attributes.getOrDefault("sub", attributes.get("id"));

    log.info("OAuth2 provider: {}", registrationId);
    log.info("Attributes from provider: {}", attributes);
    log.info("Parsed values -> email: {}, name: {}, providerId: {}", email, name, providerId);

    if (providerId == null) {
      log.error("No provider id found (sub). Attributes = {}", attributes);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_user"), "No provider id (sub) present");
    }

    try {
      Optional<User> opt = userRepository.findByProviderAndProviderId(registrationId, providerId);
      User user;

      if (opt.isPresent()) {
        user = opt.get();
        log.info(
            "Found existing user by providerId: id={}, email={}", user.getId(), user.getEmail());

        if (name != null && !name.equals(user.getName())) {
          log.debug("Updating name: {} -> {}", user.getName(), name);
          user.setName(name);
        }
        if (picture != null && !Objects.equals(picture, user.getPhotoUrl())) {
          log.debug("Updating photo: {} -> {}", user.getPhotoUrl(), picture);
          user.setPhotoUrl(picture);
        }
        userRepository.save(user);
        log.info("Updated existing user: id={}", user.getId());

      } else {
        log.info("No user found by providerId={}, provider={}", providerId, registrationId);

        if (email != null) {
          Optional<User> byEmail = userRepository.findByEmail(email);
          if (byEmail.isPresent()) {
            user = byEmail.get();
            log.info(
                "Found existing user by email={}, linking with provider {}", email, registrationId);
            user.setProvider(registrationId);
            user.setProviderId(providerId);
            if (picture != null) user.setPhotoUrl(picture);
            if (name != null) user.setName(name);
            userRepository.save(user);
            log.info("Linked OAuth provider to existing user id={}", user.getId());
          } else {
            log.info("Creating new user with email={}", email);
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : email);
            user.setProvider(registrationId);
            user.setProviderId(providerId);
            user.setPhotoUrl(picture);
            userRepository.save(user);
            log.info("Created new user id={} email={}", user.getId(), user.getEmail());
          }
        } else {
          log.warn("Provider did not return email. Creating synthetic user.");
          String syntheticEmail = registrationId + "_" + providerId + "@noemail.local";
          User u = new User();
          u.setEmail(syntheticEmail);
          u.setName(name != null ? name : syntheticEmail);
          u.setProvider(registrationId);
          u.setProviderId(providerId);
          u.setPhotoUrl(picture);
          user = userRepository.save(u);
          log.info("Created synthetic user id={} email={}", user.getId(), user.getEmail());
        }
      }

      List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
      Map<String, Object> mapped = new HashMap<>(attributes);
      mapped.put("localUserId", user.getId());

      log.info(
          "OAuth2 login completed successfully for user id={}, providerId={}",
          user.getId(),
          providerId);

      return new DefaultOAuth2User(authorities, mapped, "sub");

    } catch (Exception e) {
      log.error("Error during OAuth2 user processing", e);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("user_creation_failed"),
          "Failed to process or save OAuth2 user: " + e.getMessage());
    }
  }
}
