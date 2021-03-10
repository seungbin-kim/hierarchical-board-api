package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "COMMENT_SEQ_GEN",
    sequenceName = "COMMENT_SEQ",
    initialValue = 1, allocationSize = 50
)
public class Comment extends BaseEntity {

  public Comment(Post post, User user, String content, Comment parent) {
    this.post = post;
    this.user = user;
    this.content = content;
    this.parent = parent;
  }

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

  @ManyToOne
  @JoinColumn(name = "parent_id", columnDefinition = "bigint")
  private Comment parent;

  @OneToMany(mappedBy = "parent")
  private final List<Comment> children = new ArrayList<>();

  @Column(columnDefinition = "boolean")
  private boolean deleted;

  public void setParent(Comment parent) {
    this.parent = parent;
  }

  public void changeContent(String content) {
    this.content = content;
  }

  public void changeDeletionStatus() {
    this.deleted = true;
    this.changeContent("지워진 답글");
  }

}
