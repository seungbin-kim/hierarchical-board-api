package com.personal.board.controller;

import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.PageDto;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.exception.ReflectIllegalAccessException;
import com.personal.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private static final String USERS = "/api/v1/users";
  private static final String USER = USERS + "/{id}";

  private final UserService userService;

  @PostMapping("/users")
  public ResponseEntity<UserResponseWithCreatedAt> signUp(
      @RequestBody @Valid final SignUpRequest signUpRequest,
      final Authentication authentication) {
    if (authentication != null) {
      throw new BadArgumentException("이미 로그인 되어 있네요?");
    }

    UserResponseWithCreatedAt userResponse = userService.signUp(signUpRequest);
    return ResponseEntity
        .created(new UriTemplate(USER).expand(userResponse.getId()))
        .body(userResponse);
  }

  @GetMapping("/users")
  public ResponseEntity<PageDto<UserResponseWithDate>> getPageableUsers(
      @RequestParam(required = false, defaultValue = "5") @Min(value = 1, message = "size must be at least 1.") final int size,
      @RequestParam(required = false, defaultValue = "0") @Min(value = 0, message = "page must be at least 0.") final int page) {

    return ResponseEntity
        .ok(userService.getPageableUsers(size, page));
  }

  @PatchMapping("/users/{id}")
  public ResponseEntity<UserResponseWithModifiedAt> patchUser(
      @RequestBody @Valid final UserUpdateRequest request,
      @PathVariable final Long id,
      final Authentication authentication) {

    try {
      return ResponseEntity
          .ok(userService.updateUser(request, id, authentication));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<UserResponseWithDate> getUser(
      @PathVariable final Long id,
      final Authentication authentication) {

    return ResponseEntity
        .ok(userService.getUser(id));
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(
      @PathVariable final Long id,
      final HttpServletResponse response,
      final Authentication authentication) {

    Cookie token = new Cookie("token", null);
    token.setHttpOnly(true);
    token.setPath("/");
    response.addCookie(token);

    userService.deleteUser(id);

    return ResponseEntity
        .noContent()
        .build();
  }

}