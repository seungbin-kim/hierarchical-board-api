package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 유저 엔티티
 */
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

  @ManyToMany
  @JoinTable(
      name = "user_authority",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
      inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
  private Set<Authority> authorities = new HashSet<>();


  /**
   * 생성 메서드
   * @param email     이메일
   * @param nickname  닉네임
   * @param name      이름
   * @param birthday  생일
   * @param password  비밀번호
   * @param authority 권한
   * @return 생성된 유저 엔티티
   */
  public static User createUser(final String email,
                                final String nickname,
                                final String name,
                                final LocalDate birthday,
                                final String password,
                                final Authority authority) {

    User user = new User();
    user.changeEmail(email);
    user.changeNickname(nickname);
    user.changeName(name);
    user.changeBirthday(birthday);
    user.changePassword(password);
    user.authorities.add(authority);
    return user;
  }


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
