package ru.nsu.spendsphere.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  private SecretKey key;

  @PostConstruct
  public void init() {
    log.info(
        "Initializing JwtTokenProvider with secret length: {}",
        secret != null ? secret.length() : "null");
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  private static final long EXPIRATION_MS = 86400000; // 24h

  public String generateToken(String email) {
    log.info("Generating token for email: {}", email);
    Date now = new Date();
    Date expiry = new Date(now.getTime() + EXPIRATION_MS);

    String token =
        Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS512, key)
            .compact();

    log.info(
        "Token generated (first 50 chars): {}...",
        token.substring(0, Math.min(50, token.length())));
    return token;
  }

  public String getEmailFromToken(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      String email = claims.getSubject();
      log.info("Successfully extracted email from token: {}", email);
      return email;
    } catch (Exception e) {
      log.error("Failed to extract email from token: {}", e.getMessage());
      throw e;
    }
  }

  public boolean validateToken(String token) {
    log.info("Validating token...");
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      log.info("Token validation SUCCESS");
      return true;
    } catch (ExpiredJwtException e) {
      log.error("Token EXPIRED: {}", e.getMessage());
      return false;
    } catch (MalformedJwtException e) {
      log.error("Token MALFORMED: {}", e.getMessage());
      return false;
    } catch (SignatureException e) {
      log.error("Token SIGNATURE INVALID: {}", e.getMessage());
      log.error("Expected key: {}", secret);
      return false;
    } catch (Exception e) {
      log.error("Token validation ERROR: {}", e.getMessage());
      log.error("Exception class: {}", e.getClass().getName());
      return false;
    }
  }
}
