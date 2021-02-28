package com.personal.board.entity;

import javax.persistence.*;

@Entity
@SequenceGenerator(
    name = "COMMENT_SEQ_GEN",
    sequenceName = "COMMENT_SEQ",
    initialValue = 1, allocationSize = 50
)
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "COMMENT_SEQ_GEN")
  @Column(name = "comment_id", columnDefinition = "bigint")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", columnDefinition = "bigint")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id", columnDefinition = "bigint")
  private User user;

  @Column(length = 500)
  private String content;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Comment group;

  @Column(columnDefinition = "integer")
  private int groupOrder;

  @Column(columnDefinition = "integer")
  private int groupDepth;

}
