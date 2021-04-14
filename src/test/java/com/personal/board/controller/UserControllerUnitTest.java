package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = {UserController.class})
class UserControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Test
  void signUp() throws Exception {
    //given
    String email = "test@test.com";
    String name = "김테스트";
    String nickname = "닉네임";
    LocalDate birthday = LocalDate.parse("1997-05-28");
    String password = "1234";
    Authority authority = new Authority(Role.ROLE_USER);

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

    User user = User.createUser(email, nickname, name, birthday, password, authority);
    ReflectionTestUtils.setField(user, "id", 1L);
    when(userService.signUp(signUpRequest))
        .thenReturn(new UserResponseWithCreatedAt(user));

    //when
    ResultActions resultActions = mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON))
        .andDo(print());

    //then
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(email))
        .andDo(print());

  }

}