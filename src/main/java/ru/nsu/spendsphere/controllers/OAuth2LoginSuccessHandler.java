package ru.nsu.spendsphere.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

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

    // Определяем окружение: production или dev
    boolean isProduction = frontendUrl.startsWith("https://");

    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from("accessToken", token)
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .httpOnly(false)
            .sameSite("Lax");

    // Для production добавляем domain и secure
    if (isProduction) {
      cookieBuilder.domain("spendsphere.ru").secure(true);
    }
    // Для localhost не устанавливаем domain и secure

    ResponseCookie cookie = cookieBuilder.build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    String redirectUrl = frontendUrl + "/oauth2/callback";
    response.sendRedirect(redirectUrl);
  }
}
