package com.personal.board.repository;

import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

  @Autowired
  UserRepository userRepository;

  @BeforeEach
  void before() {
    User user1 = User.createUser(
        "a@a.a",
        "nick",
        "name1",
        LocalDate.now(),
        "123",
        new Authority(Role.ROLE_USER));
    User user2 = User.createUser(
        "b@b.b",
        "kick",
        "name2",
        LocalDate.now(),
        "123",
        new Authority(Role.ROLE_USER));
    userRepository.save(user1);
    userRepository.save(user2);
  }

  @Test
  void save() throws Exception {
    //given
    User user = User.createUser(
        "c@c.c",
        "cccc",
        "namec",
        LocalDate.now(),
        "123",
        new Authority(Role.ROLE_USER));

    //when
    User savedUser = userRepository.save(user);

    //then
    assertThat(savedUser).isEqualTo(user);
  }

  @Test
  void existsByEmailSuccess() throws Exception {
    //given
    boolean existsByEmail = userRepository.existsByEmail("a@a.a");

    //when

    //then
    assertThat(existsByEmail).isTrue();
  }

  @Test
  void existsByEmailFail() throws Exception {
    //given
    boolean existsByEmail = userRepository.existsByEmail("b.@b.b");

    //when

    //then
    assertThat(existsByEmail).isFalse();
  }

  @Test
  void existsByNicknameSuccess() throws Exception {
    //given
    boolean existsByNickname = userRepository.existsByNickname("nick");

    //when

    //then
    assertThat(existsByNickname).isTrue();
  }

  @Test
  void existsByNicknameFail() throws Exception {
    //given
    boolean existsByNickname = userRepository.existsByNickname("kim");

    //when

    //then
    assertThat(existsByNickname).isFalse();
  }

  @Test
  void findById() throws Exception {
    //given
    Optional<User> userOptional = userRepository.findById(0L);

    //when
    User findUser = userOptional.get();

    //then
    assertThat(findUser.getId()).isEqualTo(0L);
  }

  @Test
  void delete() throws Exception {
    //given
    Optional<User> userOptional = userRepository.findById(0L);

    //when
    userRepository.delete(userOptional.get());
    Optional<User> userOptional2 = userRepository.findById(0L);

    //then
    assertThrows(NoSuchElementException.class, userOptional2::get);
  }

  @Test
  void findAll() throws Exception {
    //given
    List<User> userList = userRepository.findAll();

    //when

    //then
    assertThat(userList.size()).isEqualTo(3); // 관리자계정 생성 DDL 자동실행되므로 +1
  }

  @Test
  void findAllPage() throws Exception {
    //given
    PageRequest pageRequest = PageRequest.of(0, 1);

    //when
    Page<User> userPage = userRepository.findAll(pageRequest);
    List<User> userList = userPage.getContent();

    //then
    assertThat(userList.size()).isEqualTo(1);
  }

}