package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@SequenceGenerator(
    name = "USER_SEQ_GEN",
    sequenceName = "USER_SEQ",
    initialValue = 1, allocationSize = 1
)
public class User extends BaseEntity {

  public User(final String email, final String nickname, final String name, final LocalDate birthday, final String password) {
    this.email = email;
    this.nickname = nickname;
    this.name = name;
    this.birthday = birthday;
    this.password = password;
  }

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "USER_SEQ_GEN")
  @Column(name = "user_id", columnDefinition = "bigint")
  private Long id;

  @Column(length = 50, unique = true, nullable = false)
  private String email;

  @Column(length = 20, unique = true, nullable = false)
  private String nickname;

  @Column(length = 20)
  private String name;

  @Column(columnDefinition = "date")
  private LocalDate birthday;

  @Column(columnDefinition = "text", nullable = false)
  private String password;

  public void changeEmail(final String email) {
    this.email = email;
  }

  public void changeNickname(final String nickname) {
    this.nickname = nickname;
  }

  public void changeName(final String name) {
    this.name = name;
  }

  public void changeBirthday(final LocalDate birthday) {
    this.birthday = birthday;
  }

  public void changePassword(final String password) {
    this.password = password;
  }

}
