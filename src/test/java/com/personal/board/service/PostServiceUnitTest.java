package com.personal.board.service;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.response.post.PostResponseWithContentAndCreatedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.PostRepository;
import com.personal.board.util.PatchUtil;
import com.personal.board.util.SecurityUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceUnitTest {

  @InjectMocks
  private PostService postService;

  @Mock
  private PostRepository postRepository;

  @Mock
  private CommentService commentService;

  @Mock
  private BoardService boardService;

  @Mock
  private UserService userService;

  @Mock
  private PatchUtil patchUtil;

  @Mock
  private SecurityUtil securityUtil;

  @Test
  @DisplayName("게시글등록")
  void addPost() throws Exception {
    //given
    PostRequest postRequest = new PostRequest();
    postRequest.setTitle("testTitle");
    postRequest.setContent("testContent");

    Board board = new Board("testBoard");
    ReflectionTestUtils.setField(board, "id", 1L);
    when(boardService.findBoard(1L))
        .thenReturn(board);
    User user = User.createUser(
        "email",
        "nickname",
        "name",
        LocalDate.now(),
        "password",
        new Authority(Role.ROLE_USER));
    when(securityUtil.getCurrentUserId())
        .thenReturn(Optional.of(1L));
    when(userService.findUser(1L))
        .thenReturn(user);
    when(postRepository.save(any(Post.class)))
        .thenReturn(
            Post.createPost(
                board,
                user,
                "testTitle",
                "testContent",
                null
            )
        );

    //when
    PostResponseWithContentAndCreatedAt response = postService.addPost(postRequest, board.getId());

    //then
    assertThat(response.getTitle()).isEqualTo("testTitle");
    assertThat(response.getContent()).isEqualTo("testContent");
  }

  @Test
  @DisplayName("게시글삭제")
  void deletePost() throws Exception {
    //given
    Board board = new Board("testBoard");
    User user = User.createUser(
        "email",
        "nickname",
        "name",
        LocalDate.now(),
        "password",
        new Authority(Role.ROLE_USER));
    Long postId = 1L;
    Long boardId = 1L;
    when(postRepository.findPostByIdAndBoardId(postId, boardId))
        .thenReturn(Optional.of(
            Post.createPost(
                board,
                user,
                "testTitle",
                "testContent",
                null
            )));

    //when
    boolean bool = postService.deletePost(postId, boardId);

    //then
    verify(boardService, times(1)).findBoard(boardId);
    verify(securityUtil, times(1)).checkAdminAndSameUser(any());
    assertThat(bool).isTrue();
  }

}