package com.personal.board.entity;

import com.personal.board.enumeration.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 권한정보
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Authority {

  @Id
  @Column(name = "authority_name", length = 50)
  @Enumerated(EnumType.STRING)
  private Role authorityName;

}
