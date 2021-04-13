package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 게시판 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "BOARD_SEQ_GEN",
    sequenceName = "BOARD_SEQ",
    initialValue = 1, allocationSize = 1
)
public class Board extends BaseEntity {

  public Board(final String name) {
    this.name = name;
  }

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "BOARD_SEQ_GEN")
  @Column(name = "board_id", columnDefinition = "bigint")
  private Long id;

  @Column(length = 20)
  private String name;

}