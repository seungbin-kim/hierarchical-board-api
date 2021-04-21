package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class UserControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE user_seq RESTART WITH 1").executeUpdate();
    userRepository.deleteById(0L);
  }

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

    //when
    ResultActions resultActions = mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON))
        .andDo(print());

    //then
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
    List<User> userList = createUserList(3);
    userRepository.saveAll(userList);

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

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        passwordEncoder.encode(password),
        authority);
    userRepository.save(user);

    Long id = 1L;

    //when
    ResultActions resultActions = mockMvc.perform(patch("/api/v1/users/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.email").value("change@a.b"))
        .andDo(print());
  }

  @Test
  @DisplayName("유저단건조회")
  @WithMockUser(username = "1")
  void getUser() throws Exception {
    //given
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    User savedUser = userRepository.save(user);
    Long id = savedUser.getId();

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
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    User savedUser = userRepository.save(user);
    Long id = savedUser.getId();

    //when
    ResultActions resultActions = mockMvc.perform(delete("/api/v1/users/{id}", id));

    //then
    resultActions
        .andExpect(status().isNoContent())
        .andDo(print());
  }

}