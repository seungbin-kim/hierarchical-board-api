package com.personal.board.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
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

  @OneToMany(mappedBy = "parent", orphanRemoval = true)
  private final List<Post> children = new ArrayList<>();

  @Column(columnDefinition = "boolean")
  private boolean deleted;


  public static Post createPost(final Board board, final User user, final String title, final String content, final Post parent) {
    Post post = new Post();
    post.setBoard(board);
    post.setUser(user);
    post.changeTitle(title);
    post.changeContent(content);
    post.changeParent(parent);
    return post;
  }


  public void updatePost(final ArrayList<String> validatedFields, final String title, final String content) {
    for (String validatedField : validatedFields) { // 입력이 확인된 필드를 변경감지로 데이터 변경
      switch (validatedField) {
        case "title":
          this.changeTitle(title);
          break;
        case "content":
          this.changeContent(content);
          break;
      }
    }
    this.setModifiedAt(LocalDateTime.now()); // 수정시간
  }


  private void setBoard(Board board) {
    this.board = board;
  }

  private void setUser(User user) {
    this.user = user;
  }

  private void changeTitle(String title) {
    this.title = title;
  }

  private void changeContent(String content) {
    this.content = content;
  }

  public void changeParent(Post parent) {
    this.parent = parent;
  }

  public void changeDeletionStatus() {
    this.deleted = true;
    this.changeTitle("지워진 게시글");
    this.changeContent("지워진 게시글");
  }

}
