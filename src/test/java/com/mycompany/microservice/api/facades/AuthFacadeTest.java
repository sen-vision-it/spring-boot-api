package com.mycompany.microservice.api.facades;

import static com.mycompany.microservice.api.constants.JWTClaims.CLAIM_EMAIL;

import com.mycompany.microservice.api.infra.auth.providers.ApiKeyAuthentication;
import com.mycompany.microservice.api.infra.auth.providers.ApiKeyAuthentication.ApiKeyDetails;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthFacadeTest {

  public static String API_KEY = "my-apikey-test";
  public static String EMAIL = "test@gmail.com";
  public static String COMPANY_SLUG = "my-company-test";

  @Test
  void verifyGetCompanySlugIsEmptyOnEmptyAuth() {
    final var securityContext = Mockito.mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertEquals(AuthFacade.getCompanySlug(), StringUtils.EMPTY);
  }

  @Test
  void verifyGetCompanySlugIsEmptyOnInvalidAuth() {
    final var securityContext = Mockito.mock(SecurityContext.class);
    final var authentication = Mockito.mock(Authentication.class);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertTrue(AuthFacade.getCompanySlug().isEmpty());
  }

  @Test
  void verifyGetCompanySlugOnJwtAuth() {

    final var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("scope", "read")
            .claim("company_slug", COMPANY_SLUG)
            .build();

    final var securityContext = Mockito.mock(SecurityContext.class);
    final var authentication = new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertEquals(AuthFacade.getCompanySlug(), COMPANY_SLUG);
  }

  @Test
  void verifyGetUserEmailOnJwtAuth() {

    final var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("scope", "read")
            .claim(CLAIM_EMAIL, EMAIL)
            .build();

    final var securityContext = Mockito.mock(SecurityContext.class);
    final var authentication = new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertEquals(AuthFacade.getUserEmail(), EMAIL);
  }

  @Test
  void verifyGetCompanySlugOnApiAuth() {

    final var authentication =
        new ApiKeyAuthentication(
            API_KEY, true, new ApiKeyDetails(1L, StringUtils.EMPTY, COMPANY_SLUG));

    final var securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertEquals(AuthFacade.getCompanySlug(), COMPANY_SLUG);
  }

  @Test
  void verifyGetUserEmailOnApiAuth() {

    final var authentication =
        new ApiKeyAuthentication(API_KEY, true, new ApiKeyDetails(1L, EMAIL, StringUtils.EMPTY));

    final var securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Assertions.assertEquals(AuthFacade.getUserEmail(), EMAIL);
  }
}
