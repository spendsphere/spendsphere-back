package ru.nsu.spendsphere.configurations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.repositories.UserRepository;
import ru.nsu.spendsphere.services.JwtTokenProvider;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    log.info("=== JWT FILTER START ===");
    log.info("Request path: {}", path);

    // В проде nginx может отдавать бэкенду путь без префикса /api,
    // поэтому проверяем оба варианта.
    boolean isAuthPath =
        path.startsWith("/api/v1/auth/") || path.startsWith("/v1/auth/") || path.equals("/v1/auth");
    boolean isApiPath = path.startsWith("/api/") || path.startsWith("/v1/");

    boolean shouldNotFilter = !isApiPath || isAuthPath;
    log.info("Should NOT filter this request? {}", shouldNotFilter);

    return shouldNotFilter;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    log.info("=== PROCESSING REQUEST: {} {} ===", request.getMethod(), request.getRequestURI());

    String header = request.getHeader("Authorization");
    log.info("Authorization header: {}", header);

    // Берём токен либо из Authorization, либо из cookie accessToken
    String token = null;
    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);
      log.info("Token extracted from Authorization header");
    } else {
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if ("accessToken".equals(cookie.getName())) {
            token = cookie.getValue();
            log.info("Token extracted from accessToken cookie");
            break;
          }
        }
      }
    }

    if (token != null) {
      log.info(
          "JWT Token extracted (first 50 chars): {}...",
          token.length() > 50 ? token.substring(0, 50) : token);

      log.info("Token length: {} characters", token.length());

      // Важная отладочная информация
      try {
        boolean isValid = jwtTokenProvider.validateToken(token);
        log.info("Token validation result: {}", isValid);

        if (isValid) {
          String email = jwtTokenProvider.getEmailFromToken(token);
          log.info("Email extracted from token: {}", email);

          Optional<User> userOpt = userRepository.findByEmail(email);
          log.info("User found in DB? {}", userOpt.isPresent());

          if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info(
                "User details - ID: {}, Email: {}, Name: {}",
                user.getId(),
                user.getEmail(),
                user.getName());

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Authentication set in SecurityContext");
          } else {
            log.warn("User not found for email: {}", email);
          }
        } else {
          log.warn("Token is INVALID according to JwtTokenProvider");
        }
      } catch (Exception e) {
        log.error("Error during token processing: {}", e.getMessage(), e);
      }
    } else {
      log.warn("No token found in Authorization header or accessToken cookie");
      log.warn("Full headers:");
      Collections.list(request.getHeaderNames())
          .forEach(headerName -> log.warn("  {}: {}", headerName, request.getHeader(headerName)));
    }

    log.info("=== CONTINUING FILTER CHAIN ===");
    filterChain.doFilter(request, response);
    log.info("=== FILTER CHAIN COMPLETED ===");
  }
}
