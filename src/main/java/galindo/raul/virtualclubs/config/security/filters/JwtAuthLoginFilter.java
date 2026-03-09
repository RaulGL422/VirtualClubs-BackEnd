package galindo.raul.virtualclubs.config.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.dtos.request.AuthRequest;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.ErrorType;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import galindo.raul.virtualclubs.utils.CommonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom filter for handling JWT-based authentication during the login process.
 * Extends {@link UsernamePasswordAuthenticationFilter} to intercept login requests,
 * authenticate credentials, and issue JWT tokens upon success.
 */
@Slf4j
public class JwtAuthLoginFilter extends UsernamePasswordAuthenticationFilter {
  private final TokensService tokensService;
  private final UserEntityServiceImpl userService;
  
  public JwtAuthLoginFilter(AuthenticationManager authenticationManager, UserEntityServiceImpl userService, TokensService tokensService) {
    this.tokensService = tokensService;
    this.userService = userService;
    setAuthenticationManager(authenticationManager);
  }

  /**
   * Attempts to authenticate the user by parsing the login credentials from the request body.
   *
   * @param request  the HTTP request containing the user credentials in JSON format.
   * @param response the HTTP response.
   * @return the {@link Authentication} object if successful.
   * @throws AuthenticationException if authentication fails or the request body cannot be parsed.
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response) throws AuthenticationException {
    AuthRequest user;
    String email = null;
    String password;
    try {
      user = new ObjectMapper().readValue(request.getInputStream(), AuthRequest.class);
      email = user.email();
      password = user.password();
      log.info("Authentication attempt for user: {}", email);
      
      UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);
      return getAuthenticationManager().authenticate(authRequest);
    } catch (Exception e) {
      log.error("Error during authentication: {}", email);
      throw new AuthenticationServiceException(e.getMessage());
    }
  }
  
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
    Map<String, Object> httpResponse =  new HashMap<>();
    httpResponse.put("success", false);
    httpResponse.put("message", ErrorType.INVALID_CREDENTIALS.getCode());
    
    response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().flush();
  }
  
  /**
   * Handles successful authentication by generating access and refresh tokens.
   *
   * @param request    the HTTP request.
   * @param response   the HTTP response where tokens will be written.
   * @param chain      the filter chain.
   * @param authResult the result of the successful authentication.
   * @throws IOException, ServletException if an error occurs during response writing.
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authResult) throws IOException, ServletException {
    User user = (User) authResult.getPrincipal();
    UserEntity userEntity = userService.getUserFromEmail(user.getUsername());
    log.info("Authentication successful for user: {}", user.getUsername());
    
    // Get Tokens
    Tokens tokens = tokensService.getNewTokens(userEntity, CommonUtils.getDispositiveInfo(request));
    String newAccessToken = tokens.accessToken();
    String newRefreshToken = tokens.refreshToken();

    response.addHeader("Authorization", newAccessToken);

    Map<String, Object> httpResponse =  new HashMap<>();
    httpResponse.put("accessToken", newAccessToken);
    httpResponse.put("refreshToken", newRefreshToken);

    response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().flush();

    super.successfulAuthentication(request, response, chain, authResult);
  }
}
