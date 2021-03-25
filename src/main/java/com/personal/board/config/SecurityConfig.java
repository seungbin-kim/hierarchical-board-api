package com.personal.board.config;

import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.JwtSecurityConfig;
import com.personal.board.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final TokenProvider tokenProvider;

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()

        .exceptionHandling()
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(jwtAccessDeniedHandler)

        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
        .antMatchers(HttpMethod.POST, "/api/v1/sign-in").permitAll()
        .antMatchers(HttpMethod.GET, "/api/v1/boards/**").permitAll()
        .antMatchers(HttpMethod.GET, "/api/v1/boards/{boardId}/posts/**").permitAll()
        .antMatchers(HttpMethod.GET, "/api/v1/posts/{postId}/comments").permitAll()
        .antMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
        .anyRequest().authenticated()

        .and()
        .apply(new JwtSecurityConfig(tokenProvider, handlerExceptionResolver));
  }

}
