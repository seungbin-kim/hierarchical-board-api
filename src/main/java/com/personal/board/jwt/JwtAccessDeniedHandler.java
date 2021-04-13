package com.personal.board.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 인증은 되었으나 권한이 없을경우
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final HandlerExceptionResolver handlerExceptionResolver;

  /**
   * 필터의 예외를 ControllerAdvice 가 처리할 수 있도록 HandlerExceptionResolver 에게 넘긴다.
   */
  @Override
  public void handle(final HttpServletRequest request,
                     final HttpServletResponse response,
                     final AccessDeniedException accessDeniedException) throws IOException, ServletException {

    handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
  }

}
