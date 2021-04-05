package com.personal.board.jwt;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

  private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

  private final TokenProvider tokenProvider;

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String jwt = resolveToken(httpServletRequest);
    String requestURI = httpServletRequest.getRequestURI();

    try {
      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        Authentication authentication = tokenProvider.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
      } else {
        logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
      }
      chain.doFilter(request, response);
    } catch (Exception exception) {
      handlerExceptionResolver.resolveException(httpServletRequest, (HttpServletResponse) response, null, exception);
    }

  }

  private String resolveToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    String token = null;
    if (cookies != null) {
      token = Arrays.stream(cookies)
          .filter(cookie -> cookie.getName().equals("token"))
          .findFirst()
          .map(Cookie::getValue)
          .orElse(null);
    }
    return token;
  }

}
