package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@SequenceGenerator(
    name = "USER_SEQ_GEN",
    sequenceName = "USER_SEQ",
    initialValue = 1, allocationSize = 1
)
public class User extends BaseEntity {

  public User(String email, String nickname, String name, int age, String password) {
    this.email = email;
    this.nickname = nickname;
    this.name = name;
    this.age = age;
    this.password = password;
    setCreatedAt(LocalDateTime.now());
    setModifiedAt(LocalDateTime.now());
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

  @Column(columnDefinition = "integer default 0")
  private int age;

  @Column(columnDefinition = "text", nullable = false)
  private String password;

//  @ManyToMany
//  @JoinTable(
//      name = "user_authority",
//      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
//      inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")}
//  )
//  private Set<Authority> authorities;

}
