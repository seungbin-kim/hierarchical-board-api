package com.personal.board.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 인증이 되지 않았을 경우
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver handlerExceptionResolver;

  /**
   * 필터의 예외를 ControllerAdvice 가 처리할 수 있도록 HandlerExceptionResolver 에게 넘긴다.
   */
  @Override
  public void commence(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AuthenticationException authException) throws IOException, ServletException {

    handlerExceptionResolver.resolveException(request, response, null, authException);
  }

}
