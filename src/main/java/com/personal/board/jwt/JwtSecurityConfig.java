package com.personal.board.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

  private final TokenProvider tokenProvider;

  private final HandlerExceptionResolver handlerExceptionResolver;


  /**
   * JWT 필터 등록
   */
  @Override
  public void configure(final HttpSecurity http) throws Exception {

    JwtFilter customFilter = new JwtFilter(tokenProvider, handlerExceptionResolver);
    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
  }

}
