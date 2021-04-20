package com.personal.board.repository;

import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

@AutoConfigureTestDatabase
@DataJpaTest
class UserRepositoryUnitTest {

  @Autowired
  private UserRepository userRepository;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  void save() throws Exception {
    //given
    User user = User.createUser(email, nickname, name, birthday, password, authority);

    //when
    User savedUser = userRepository.save(user);

    //then
    Assertions.assertThat(savedUser).isEqualTo(user);
  }

}