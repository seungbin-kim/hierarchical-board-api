package com.personal.board.controller;

import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.exception.ReflectIllegalAccessException;
import com.personal.board.service.UserService;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

  private static final String USERS = "/api/v1/users";

  private static final String USER = USERS + "/{id}";

  private final UserService userService;


  /**
   * 회원가입
   * @param signUpRequest 회원가입 정보
   * @return 등록한 정보
   */
  @PostMapping("/users")
  public ResponseEntity<UserResponseWithCreatedAt> signUp(@RequestBody @Valid final SignUpRequest signUpRequest) {

    if (!SecurityUtil.getAuthentication().getName().equals("anonymousUser")) {
      throw new BadArgumentException("이미 로그인 되어 있네요?");
    }

    UserResponseWithCreatedAt userResponse = userService.signUp(signUpRequest);
    return ResponseEntity
        .created(new UriTemplate(USER).expand(userResponse.getId()))
        .body(userResponse);
  }


  /**
   * 유저목록 페이징 조회
   * @param pageable 페이징 정보
   * @return 페이징된 유저목록
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/users")
  public Page<UserResponseWithDate> getPageableUsers(@PageableDefault(size = 5, sort = "id") final Pageable pageable) {

    return userService.getPageableUsers(pageable);
  }


  /**
   * 유저정보 수정
   * @param request 수정정보
   * @param id      유저 id
   * @return 수정된 유저정보
   */
  @PatchMapping("/users/{id}")
  public ResponseEntity<UserResponseWithModifiedAt> patchUser(@RequestBody @Valid final UserUpdateRequest request,
                                                              @PathVariable final Long id) {

    SecurityUtil.checkAdminAndSameUser(id);

    try {
      return ResponseEntity
          .ok(userService.updateUser(request, id));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }


  /**
   * 유저 단건조회
   * @param id 유저 id
   * @return 유저정보
   */
  @GetMapping("/users/{id}")
  public ResponseEntity<UserResponseWithDate> getUser(@PathVariable final Long id) {

    SecurityUtil.checkAdminAndSameUser(id);

    return ResponseEntity
        .ok(userService.getUser(id));
  }


  /**
   * 유저 삭제
   * @param id       유저 id
   * @param response 응답
   * @return 상태코드 204
   */
  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable final Long id,
                                      final HttpServletResponse response) {

    SecurityUtil.checkAdminAndSameUser(id);

    if (SecurityUtil.isAdmin()) {
      Cookie token = new Cookie("token", null);
      token.setHttpOnly(true);
      token.setPath("/");
      response.addCookie(token);
    }

    userService.deleteUser(id);

    return ResponseEntity.noContent().build();
  }

}