package com.personal.board.controller;

import com.personal.board.dto.request.SignInRequest;
import com.personal.board.dto.response.TokenResponse;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.service.AuthService;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;


  @PostMapping("/sign-in")
  public ResponseEntity<TokenResponse> signIn(@RequestBody @Valid final SignInRequest signInRequest,
                                              final HttpServletResponse response) {

    if (!SecurityUtil.getAuthentication().getName().equals("anonymousUser")) {
      throw new BadArgumentException("이미 로그인 되어 있네요?");
    }

    Cookie token = authService.logIn(signInRequest);
    response.addCookie(token);

    return ResponseEntity
        .ok()
        .body(new TokenResponse(token.getValue()));
  }


  @PostMapping("/log-out")
  public void logOut(final HttpServletResponse response) {

    Cookie token = new Cookie("token", null);
    token.setHttpOnly(true);
    token.setPath("/");
    token.setMaxAge(0);
    response.addCookie(token);
  }

}