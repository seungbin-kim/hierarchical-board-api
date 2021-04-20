package com.personal.board.repository;

import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureTestDatabase
@DataJpaTest
class UserRepositoryUnitTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager em;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE user_seq RESTART WITH 1").executeUpdate();
    userRepository.deleteById(0L);
  }

  @Test
  @DisplayName("회원저장")
  void save() throws Exception {
    //given
    User user = User.createUser(email, nickname, name, birthday, password, authority);

    //when
    User savedUser = userRepository.save(user);

    //then
    assertThat(savedUser).isEqualTo(user);
  }

  @Test
  @DisplayName("회원삭제")
  void delete() throws Exception {
    //given
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    User savedUser = userRepository.save(user);

    //when
    userRepository.delete(savedUser);
    Optional<User> findUser = userRepository.findById(savedUser.getId());

    //then
    assertThat(findUser).isEmpty();
  }

  @Test
  @DisplayName("회원ID로찾기")
  void findUserById() throws Exception {
    //given
    User user = User.createUser(email, nickname, name, birthday, password, authority);
    User savedUser = userRepository.save(user);

    //when
    Optional<User> findUser = userRepository.findById(savedUser.getId());

    //then
    assertThat(findUser.get()).isEqualTo(savedUser);
  }

  @Test
  @DisplayName("회원페이지조회")
  void findUserPage() throws Exception {
    //given
    List<User> userList = createUserList(10);
    userRepository.saveAll(userList);
    PageRequest pageRequest = PageRequest.of(0, 5);

    //when
    Page<User> userPage = userRepository.findAll(pageRequest);

    //then
    assertThat(userPage.getTotalElements()).isEqualTo(10);
    assertThat(userPage.getTotalPages()).isEqualTo(2);
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

}