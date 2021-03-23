package com.personal.board.controller;

import com.personal.board.dto.request.SignInRequest;
import com.personal.board.dto.response.TokenResponse;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

  private final TokenProvider tokenProvider;

  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  @PostMapping("/sign-in")
  public ResponseEntity<TokenResponse> signIn(@RequestBody @Valid SignInRequest signInRequest, HttpServletResponse response , Authentication auth) {
    if (auth != null) {
      throw new BadArgumentException("이미 로그인 되어 있네요?");
    }

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword());

    try {
      Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = tokenProvider.createToken(authentication);

      Cookie token = new Cookie("token", jwt);
      token.setHttpOnly(true);
      token.setPath("/");
      response.addCookie(token);

      return ResponseEntity
          .ok()
          .body(new TokenResponse(jwt));
    } catch (BadCredentialsException exception) {
      throw new BadArgumentException("email or password is incorrect.");
    }

  }

  @PostMapping("/log-out")
  public void logOut(HttpServletRequest request, HttpServletResponse response) {
    Cookie token = new Cookie("token", null);
    token.setHttpOnly(true);
    token.setPath("/");
    response.addCookie(token);
  }

}
