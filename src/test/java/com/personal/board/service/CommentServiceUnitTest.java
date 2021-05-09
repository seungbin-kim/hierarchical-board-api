package com.personal.board.service;

import com.personal.board.dto.query.CommentQueryDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @Test
  @DisplayName("댓글삭제")
  void deleteComment() throws Exception {
    //given
    Long postId = 1L;
    Long commentId = 1L;
    Long boardId = 1L;
    Long userId = 1L;

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

    String content = "test";
    Comment comment = Comment.createComment(
        post,
        user,
        content,
        null
    );
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(commentRepository.findByIdAndPostId(commentId, postId))
        .thenReturn(Optional.of(comment));

    //when
    commentService.deleteComment(postId, commentId);

    //then
    verify(postService, times(1)).findPost(postId, null);
    verify(securityUtil, times(1)).checkAdminAndSameUser(any());
  }

  @Test
  @DisplayName("댓글페이지조회")
  void getPageableComment() throws Exception {
    //given
    int page = 0;
    int pageSize = 10;
    int number = 5;
    Long postId = 1L;
    PageRequest pageRequest = PageRequest.of(page, pageSize);

    List<CommentQueryDto> parents = createCommentQueryDto(number, false);
    List<CommentQueryDto> children = createCommentQueryDto(number, true);
    PageImpl<CommentQueryDto> pageImpl = new PageImpl<>(parents, pageRequest, number);
    when(commentRepository.findAllOriginal(postId, pageRequest))
        .thenReturn(pageImpl);
    when(commentRepository.findAllChildren(any(), any()))
        .thenReturn(children)
        .thenReturn(new ArrayList<>());

    //when
    Page<CommentQueryDto> dtoPage = commentService.getPageableComment(postId, pageRequest);

    //then
    assertThat(dtoPage.getTotalElements()).isEqualTo(number);
    assertThat(dtoPage.getContent().size()).isEqualTo(number);
  }

  private List<CommentQueryDto> createCommentQueryDto(int number, boolean isChildren) {
    List<CommentQueryDto> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Long parentId = null;
      long id = i;
      if (isChildren) {
        parentId = (long) i;
        id = (i + number);
      }
      CommentQueryDto dto = new CommentQueryDto(
          parentId,
          id,
          "nickname" + i,
          "content" + i,
          LocalDateTime.now(),
          false);
      list.add(dto);
    }
    return list;
  }

}