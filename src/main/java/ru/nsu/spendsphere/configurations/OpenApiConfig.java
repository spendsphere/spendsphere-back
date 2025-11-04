package ru.nsu.spendsphere.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "googleOAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("SpendSphere API Documentation")
                .version("1.0")
                .description("API documentation for SpendSphere application"))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("Google OAuth2 Login")
                        .flows(
                            new OAuthFlows()
                                .authorizationCode(
                                    new OAuthFlow()
                                        .authorizationUrl(
                                            "https://accounts.google.com/o/oauth2/v2/auth")
                                        .tokenUrl("https://oauth2.googleapis.com/token")
                                        .scopes(
                                            new Scopes()
                                                .addString("openid", "OpenID Connect scope")
                                                .addString("profile", "Access profile info")
                                                .addString("email", "Access email"))))));
  }
}
