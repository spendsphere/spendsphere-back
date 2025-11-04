package ru.nsu.spendsphere.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.UserMapper;
import ru.nsu.spendsphere.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");
    if (email == null) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_user"), "Email not found from provider");
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  User u = new User();
                  u.setEmail(email);
                  u.setName((String) attributes.getOrDefault("name", email));
                  u.setProvider("google");
                  return userRepository.save(u);
                });

    user.setName((String) attributes.getOrDefault("name", user.getName()));
    user.setPhotoUrl((String) attributes.getOrDefault("picture", user.getPhotoUrl()));
    userRepository.save(user);

    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    Map<String, Object> mappedAttrs = new HashMap<>(attributes);
    mappedAttrs.put("localUserId", user.getId());

    return new DefaultOAuth2User(authorities, mappedAttrs, "sub");
  }
}
