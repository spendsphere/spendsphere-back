package ru.nsu.spendsphere.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.repositories.UserRepository;
import ru.nsu.spendsphere.services.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
    String email = oauthUser.getAttribute("email");

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found after OAuth login"));

    String token = jwtTokenProvider.generateToken(user.getEmail());

    String redirectUrl =
        "http://localhost:3000/oauth2/callback?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8);
    response.sendRedirect(redirectUrl);
  }
}
