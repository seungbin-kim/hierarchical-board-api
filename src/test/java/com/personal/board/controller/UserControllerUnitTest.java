package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.UserService;
import com.personal.board.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = UserController.class)
class UserControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private SecurityUtil securityUtil;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("회원가입")
  void signUp() throws Exception {
    //given
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail(email);
    signUpRequest.setName(name);
    signUpRequest.setNickname(nickname);
    signUpRequest.setBirthday(birthday);
    signUpRequest.setPassword(password);

    String content = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .writeValueAsString(signUpRequest);

    Long id = 1L;
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    ReflectionTestUtils.setField(user, "id", id);

    UserResponseWithCreatedAt userResponseWithCreatedAt = new UserResponseWithCreatedAt(user);
    when(userService.signUp(signUpRequest))
        .thenReturn(userResponseWithCreatedAt);
    when(securityUtil.getAuthentication())
        .thenReturn(new Authentication() {
          @Override
          public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
          }

          @Override
          public Object getCredentials() {
            return null;
          }

          @Override
          public Object getDetails() {
            return null;
          }

          @Override
          public Object getPrincipal() {
            return null;
          }

          @Override
          public boolean isAuthenticated() {
            return false;
          }

          @Override
          public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

          }

          @Override
          public String getName() {
            return "anonymousUser";
          }
        });
    doNothing().when(securityUtil).checkAdminAndSameUser(any());

    //when
    ResultActions resultActions = mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    // then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.email").value(email))
        .andDo(print());
  }

  @Test
  @DisplayName("유저목록조회")
  @WithMockUser(roles = "ADMIN")
  void getPageableUsers() throws Exception {
    //given
    PageRequest pageRequest = PageRequest.of(0, 5);

    List<User> userList = createUserList(3);
    List<UserResponseWithDate> dtos = userList.stream()
        .map(UserResponseWithDate::new)
        .collect(Collectors.toList());

    PageImpl<UserResponseWithDate> page = new PageImpl<>(dtos, pageRequest, dtos.size());
    when(userService.getPageableUsers(any(Pageable.class)))
        .thenReturn(page);

    //when
    ResultActions resultActions = mockMvc.perform(get("/api/v1/users?page=0&size=5")
        .accept(MediaType.APPLICATION_JSON));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(3)))
        .andExpect(jsonPath("$.content.[0].id").value(1))
        .andExpect(jsonPath("$.content.[1].id").value(2))
        .andExpect(jsonPath("$.content.[2].id").value(3))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.size").value(5))
        .andDo(print());
  }

  private List<User> createUserList(int number) {
    List<User> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      User user = User.createUser(i + "a@a.a", i + "a", i + "aa",
          LocalDate.now(), "123", new Authority(Role.ROLE_USER));
      ReflectionTestUtils.setField(user, "id", (long) i);
      list.add(user);
    }
    return list;
  }

  @Test
  @DisplayName("유저정보수정")
  @WithMockUser(username = "1")
  void patchUser() throws Exception {
    //given
    UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
    userUpdateRequest.setEmail("change@a.b");
    userUpdateRequest.setName("changeName");
    userUpdateRequest.setNickname("changeNickname");
    userUpdateRequest.setPassword(password);

    String content = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .writeValueAsString(userUpdateRequest);

    Long id = 1L;

    User user = User.createUser("change@a.b", "changeNickname", "changeName", birthday, password, authority);
    ReflectionTestUtils.setField(user, "id", id);

    UserResponseWithModifiedAt userResponseWithModifiedAt = new UserResponseWithModifiedAt(user);
    when(userService.updateUser(userUpdateRequest, id))
        .thenReturn(userResponseWithModifiedAt);

    //when
    ResultActions resultActions = mockMvc.perform(patch("/api/v1/users/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("change@a.b"))
        .andDo(print());
  }

  @Test
  @DisplayName("유저단건조회")
  @WithMockUser(username = "1")
  void getUser() throws Exception {
    //given
    Long id = 1L;
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    ReflectionTestUtils.setField(user, "id", id);

    UserResponseWithDate userResponseWithDate = new UserResponseWithDate(user);
    when(userService.getUser(id))
        .thenReturn(userResponseWithDate);

    //when
    ResultActions resultActions = mockMvc.perform(get("/api/v1/users/{id}", id)
        .accept(MediaType.APPLICATION_JSON));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.email").value(email))
        .andDo(print());
  }

  @Test
  @DisplayName("유저삭제")
  @WithMockUser(username = "1")
  void deleteUser() throws Exception {
    //given
    Long id = 1L;

    //when
    ResultActions resultActions = mockMvc.perform(delete("/api/v1/users/{id}", id));

    //then
    resultActions
        .andExpect(status().isNoContent())
        .andDo(print());
  }

}