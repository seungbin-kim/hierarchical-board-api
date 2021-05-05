package com.personal.board.repository;

import com.personal.board.dto.query.CommentQueryDto;
import com.personal.board.entity.*;
import com.personal.board.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureTestDatabase
@DataJpaTest
class CommentRepositoryUnitTest {

  @Autowired
  CommentRepository commentRepository;

  @Autowired
  BoardRepository boardRepository;

  @Autowired
  PostRepository postRepository;

  @Autowired
  UserRepository userRepository;


  @Autowired
  EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE comment_seq RESTART WITH 1").executeUpdate();
  }

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("원댓글페이징")
  void findAllOriginal() throws Exception {
    //given
    int number = 10;
    Board board = new Board("testBoard");
    boardRepository.save(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);

    Post post = Post.createPost(
        board,
        user,
        "title",
        "content",
        null
    );
    postRepository.save(post);

    List<Comment> commentList = createCommentList(number, post, user);
    commentRepository.saveAll(commentList);

    int numberOfReply = 3;
    List<Comment> replyList = createReplyList(numberOfReply, commentList, post, user);
    commentRepository.saveAll(replyList);

    int requestSize = 5;
    PageRequest pageRequest = PageRequest.of(0, requestSize);

    //when
    Page<CommentQueryDto> original = commentRepository.findAllOriginal(post.getId(), pageRequest);

    //then
    assertThat(original.getTotalElements()).isEqualTo(number);
    assertThat(original.getSize()).isEqualTo(requestSize);
  }

  private List<Comment> createReplyList(int number, List<Comment> parent, Post post, User user) {
    List<Comment> list = new ArrayList<>();
    for (Comment comment : parent) {
      for (int i = 0; i < number; i++) {
        Comment reply = Comment.createComment(
            post,
            user,
            "replyContent" + i,
            comment);
        list.add(reply);
      }
    }
    return list;
  }

  private List<Comment> createCommentList(int number, Post post, User user) {
    List<Comment> list = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      Comment comment = Comment.createComment(
          post,
          user,
          "content" + i,
          null);
      list.add(comment);
    }
    return list;
  }

  @Test
  void findAllChildren() throws Exception {
    //given
    int number = 10;
    Board board = new Board("testBoard");
    boardRepository.save(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);

    Post post = Post.createPost(
        board,
        user,
        "title",
        "content",
        null
    );
    postRepository.save(post);

    List<Comment> commentList = createCommentList(number, post, user);
    commentRepository.saveAll(commentList);

    int numberOfReply = 3;
    List<Comment> replyList = createReplyList(numberOfReply, commentList, post, user);
    commentRepository.saveAll(replyList);

    List<Long> parentIds = commentList.stream()
        .map(Comment::getId)
        .collect(Collectors.toList());

    //when
    List<CommentQueryDto> children = commentRepository.findAllChildren(post.getId(), parentIds);

    //then
    assertThat(children.size()).isEqualTo(number * numberOfReply);
  }

}