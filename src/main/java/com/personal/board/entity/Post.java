package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "POST_SEQ_GEN",
    sequenceName = "POST_SEQ",
    initialValue = 1, allocationSize = 1
)
public class Post extends BaseEntity {

  public Post(Board board, User user, String title, String content, Post group, int groupOrder, int groupDepth) {
    this.board = board;
    this.user = user;
    this.title = title;
    this.content = content;
    this.group = group;
    this.groupOrder = groupOrder;
    this.groupDepth = groupDepth;
  }

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "POST_SEQ_GEN")
  @Column(name = "post_id", columnDefinition = "bigint")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", columnDefinition = "bigint")
  private Board board;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id", columnDefinition = "bigint")
  private User user;

  @Column(length = 100, nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String content;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", columnDefinition = "bigint")
  private Post group;

  @Column(columnDefinition = "integer")
  private int groupOrder;

  @Column(columnDefinition = "integer")
  private int groupDepth;

  @Column(columnDefinition = "boolean")
  private boolean isDeleted;

  public void setGroup(Post group) {
    this.group = group;
  }

  public void setGroupOrder(int groupOrder) {
    this.groupOrder = groupOrder;
  }

  public void setGroupDepth(int groupDepth) {
    this.groupDepth = groupDepth;
  }

}
