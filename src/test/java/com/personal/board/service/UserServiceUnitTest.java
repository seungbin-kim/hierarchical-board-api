package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.util.PatchUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PatchUtil patchUtil;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("회원저장하기")
  void signUp() throws Exception {
    //given
    SignUpRequest signUpRequest = new SignUpRequest();
    signUpRequest.setEmail(email);
    signUpRequest.setName(name);
    signUpRequest.setNickname(nickname);
    signUpRequest.setBirthday(birthday);
    signUpRequest.setPassword(password);

    User user = User.createUser(
        signUpRequest.getEmail(),
        signUpRequest.getNickname(),
        signUpRequest.getName(),
        signUpRequest.getBirthday(),
        signUpRequest.getPassword(),
        authority);
    Long id = 1L;
    ReflectionTestUtils.setField(user, "id", id);

    when(userRepository.save(any(User.class)))
        .thenReturn(user);

    //when
    UserResponseWithCreatedAt userResponseWithCreatedAt = userService.signUp(signUpRequest);

    //then
    assertThat(userResponseWithCreatedAt.getId()).isEqualTo(user.getId());
  }

  @Test
  @DisplayName("회원탈퇴")
  void deleteUser() throws Exception {
    //given
    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority);
    Long id = 1L;
    ReflectionTestUtils.setField(user, "id", id);
    when(userRepository.findById(id))
        .thenReturn(Optional.of(user));
    doNothing()
        .when(commentRepository).updateWriterIdToNull(id);
    doNothing()
        .when(userRepository).updateWriterIdToNull(id);
    doNothing()
        .when(userRepository).delete(user);

    //when
    userService.deleteUser(id);

    //then
    verify(commentRepository, times(1)).updateWriterIdToNull(id);
    verify(userRepository, times(1)).updateWriterIdToNull(id);
    verify(userRepository, times(1)).delete(user);
  }

  @Test
  @DisplayName("회원단건조회")
  void getUser() throws Exception {
    //given
    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority);
    Long id = 1L;
    ReflectionTestUtils.setField(user, "id", id);
    when(userRepository.findById(id))
        .thenReturn(Optional.of(user));

    //when
    UserResponseWithDate findUser = userService.getUser(id);

    //then
    assertThat(findUser.getId()).isEqualTo(user.getId());
  }

  @Test
  @DisplayName("회원페이지조회")
  void getPageableUsers() throws Exception {
    //given
    List<User> userList = createUserList(10);
    PageRequest pageRequest = PageRequest.of(0, 5);
    when(userRepository.findAll(pageRequest))
        .thenReturn(new PageImpl<>(
            userList,
            pageRequest,
            userList.size()));

    //when
    Page<UserResponseWithDate> pageableUsers = userService.getPageableUsers(pageRequest);

    //then
    assertThat(pageableUsers.getTotalElements()).isEqualTo(10);
    assertThat(pageableUsers.getTotalPages()).isEqualTo(2);
    assertThat(pageableUsers.getSize()).isEqualTo(5);
    assertThat(pageableUsers.isFirst()).isTrue();
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
  @DisplayName("회원정보수정")
  void updateUser() throws Exception {
    //given
    String updateEmail = "update@a.b";

    UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
    userUpdateRequest.setPassword(password);
    userUpdateRequest.setEmail(updateEmail);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority);
    Long id = 1L;
    ReflectionTestUtils.setField(user, "id", id);

    when(userRepository.findById(id))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any()))
        .thenReturn(true);
    when(patchUtil.getValidatedFields(userUpdateRequest))
        .thenReturn(new ArrayList<>(Arrays.asList("email")));

    //when
    UserResponseWithModifiedAt response = userService.updateUser(userUpdateRequest, id);

    //then
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getEmail()).isEqualTo(updateEmail);
  }

}