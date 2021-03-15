package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
    name = "Post.user",
    attributeNodes = @NamedAttributeNode("user"))

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "POST_SEQ_GEN",
    sequenceName = "POST_SEQ",
    initialValue = 1, allocationSize = 1
)
public class Post extends BaseEntity {

  public Post(Board board, User user, String title, String content, Post parent) {
    this.board = board;
    this.user = user;
    this.title = title;
    this.content = content;
    this.parent = parent;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", columnDefinition = "bigint")
  private Post parent;

  @OneToMany(mappedBy = "parent")
  private final List<Post> children = new ArrayList<>();

  @Column(columnDefinition = "boolean")
  private boolean deleted;

  public void removeChildPost(Post child) {

  }

  public void setParent(Post parent) {
    this.parent = parent;
  }

  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeContent(String content) {
    this.content = content;
  }

  public void changeDeletionStatus() {
    this.deleted = true;
    this.changeTitle("지워진 게시글");
    this.changeContent("지워진 게시글");
  }

}
