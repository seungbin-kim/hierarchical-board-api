package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.util.PatchUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

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
    Assertions.assertThat(userResponseWithCreatedAt.getId()).isEqualTo(user.getId());
  }

}