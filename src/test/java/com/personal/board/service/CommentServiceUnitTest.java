package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.entity.*;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.CommentRepository;
import com.personal.board.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

  @InjectMocks
  private CommentService commentService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private PostService postService;

  @Mock
  private UserService userService;

  @Mock
  private SecurityUtil securityUtil;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("댓글등록")
  void addComment() throws Exception {
    //given
    Long postId = 1L;
    Long userId = 1L;
    Long boardId = 1L;
    Long commentId = 1L;

    String content = "test";
    CommentRequest commentRequest = new CommentRequest();
    commentRequest.setContent(content);
    commentRequest.setParentId(null);

    Board board = new Board("testBoard");
    ReflectionTestUtils.setField(board, "id", boardId);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    ReflectionTestUtils.setField(user, "id", userId);

    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );
    ReflectionTestUtils.setField(post, "id", postId);

    Comment comment = Comment.createComment(
        post,
        user,
        content,
        null
    );
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(postService.findPost(postId, null))
        .thenReturn(post);
    when(securityUtil.getCurrentUserId())
        .thenReturn(Optional.of(userId));
    when(userService.findUser(userId))
        .thenReturn(user);
    when(commentRepository.save(any()))
        .thenReturn(comment);

    //when
    CommentResponseWithCreatedAt commentResponseWithCreatedAt = commentService.addComment(commentRequest, postId);

    //then
    assertThat(commentResponseWithCreatedAt.getId()).isEqualTo(commentId);
    assertThat(commentResponseWithCreatedAt.getUserNickname()).isEqualTo(nickname);
    assertThat(commentResponseWithCreatedAt.getContent()).isEqualTo(content);
  }

}