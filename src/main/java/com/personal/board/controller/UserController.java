package com.personal.board.controller;

import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.UserResponseWithCreatedAt;
import com.personal.board.dto.response.UserResponseWithDate;
import com.personal.board.dto.response.UserResponseWithModifiedAt;
import com.personal.board.exception.ReflectIllegalAccessException;
import com.personal.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private static final String USERS = "/api/v1/users";
  private static final String USER = USERS + "/{id}";

  private final UserService userService;

  @PostMapping("/users")
  public ResponseEntity<UserResponseWithCreatedAt> signUp(@RequestBody @Valid final SignUpRequest signUpRequest) {
    UserResponseWithCreatedAt userResponse = userService.signUp(signUpRequest);

    return ResponseEntity
        .created(new UriTemplate(USER).expand(userResponse.getId()))
        .body(userResponse);
  }

  @GetMapping("/users")
  public ResponseEntity<ResultResponse<List<UserResponseWithDate>>> getAllUsers() {

    return ResponseEntity
        .ok(new ResultResponse<>(userService.getAllUsers()));
  }

  @PatchMapping("/users/{id}")
  public ResponseEntity<UserResponseWithModifiedAt> updateUser(
      @RequestBody @Valid final UserUpdateRequest request, @PathVariable final Long id) {

    try {
      return ResponseEntity
          .ok(userService.updateUser(request, id));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }

}