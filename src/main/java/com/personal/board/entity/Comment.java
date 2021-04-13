package com.personal.board.entity;

import com.personal.board.exception.BadArgumentException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  @ManyToOne
  @JoinColumn(name = "parent_id", columnDefinition = "bigint")
  private Comment parent;

  @OneToMany(mappedBy = "parent", orphanRemoval = true)
  private final List<Comment> children = new ArrayList<>();

  @Column(columnDefinition = "boolean")
  private boolean deleted;


  /**
   * 생성 메서드
   * @param post    게시글 엔티티
   * @param user    유저 엔티티
   * @param content 내용
   * @param parent  원글
   * @return 생성된 엔티티
   */
  public static Comment createComment(final Post post,
                                      final User user,
                                      final String content,
                                      final Comment parent) {

    Comment comment = new Comment();
    comment.setPost(post);
    comment.setUser(user);
    comment.changeContent(content);
    comment.changeParent(parent);
    return comment;
  }


  /**
   * 내용수정 메서드
   * @param content 수정할 내용
   */
  public void updateComment(final String content) {

    if (content != null) {
      if (StringUtils.isBlank(content)) {
        throw new BadArgumentException("content is blank.");
      }
      this.changeContent(content);
      this.setModifiedAt(LocalDateTime.now());
    }
  }


  private void setPost(final Post post) {
    this.post = post;
  }

  private void setUser(final User user) {
    this.user = user;
  }

  private void changeContent(final String content) {
    this.content = content;
  }

  public void changeParent(final Comment parent) {
    this.parent = parent;
  }

  /**
   * 삭제시 상태변경 메서드
   */
  public void changeDeletionStatus() {

    this.deleted = true;
    this.changeContent("지워진 댓글");
  }

}
