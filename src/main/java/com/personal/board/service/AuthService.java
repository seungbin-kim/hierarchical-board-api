package com.personal.board.service;

import com.personal.board.dto.request.SignInRequest;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final TokenProvider tokenProvider;

  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  @Value("${jwt.token-validity-in-seconds}")
  private int cookieAge;


  public Cookie logIn(final SignInRequest signInRequest) {

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword());

    try {
      Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = tokenProvider.createToken(authentication);

      Cookie token = new Cookie("token", jwt);
      token.setHttpOnly(true);
      token.setMaxAge(cookieAge);
      token.setPath("/");

      return token;
    } catch (BadCredentialsException exception) {
      throw new BadArgumentException("email or password is incorrect.");
    }
  }

}
