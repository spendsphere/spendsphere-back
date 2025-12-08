package ru.nsu.spendsphere.configurations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    // Пропускаем все, что не начинается с /api/
    // ИЛИ пропускаем конкретные публичные эндпоинты внутри /api/
    if (!path.startsWith("/api/")) {
      return true;
    }

    // Для /api/ проверяем, является ли это публичным эндпоинтом
    return path.startsWith("/api/v1/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);

      if (jwtTokenProvider.validateToken(token)) {
        String email = jwtTokenProvider.getEmailFromToken(token);

        userRepository.findByEmail(email).ifPresent(user -> {
          UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                          user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
          SecurityContextHolder.getContext().setAuthentication(auth);
        });
      }
    }

    filterChain.doFilter(request, response);
  }
}