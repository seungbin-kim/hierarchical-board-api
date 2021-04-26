package com.personal.board.service;

import com.personal.board.dto.query.PostQueryDto;
import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.PostResponseWithContentAndCreatedAt;
import com.personal.board.dto.response.post.PostResponseWithContentAndDate;
import com.personal.board.dto.response.post.PostResponseWithContentAndModifiedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.util.PatchUtil;
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
import java.util.Arrays;
import java.util.List;
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
  private CommentRepository commentRepository;

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
  
  @Test
  @DisplayName("게시글단건조회")
  void getPost() throws Exception {
    //given
    Long postId = 1L;
    Long boardId = 1L;
    Board board = new Board("testBoard");
    User user = User.createUser(
        "email",
        "nickname",
        "name",
        LocalDate.now(),
        "password",
        new Authority(Role.ROLE_USER));
    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );
    ReflectionTestUtils.setField(post, "id", postId);
    when(postRepository.findPostByIdAndBoardId(postId, boardId))
        .thenReturn(Optional.of(post));
    
    //when
    PostResponseWithContentAndDate response = postService.getPost(postId, boardId);

    //then
    assertThat(response.getId()).isEqualTo(postId);
    assertThat(response.getTitle()).isEqualTo("testTitle");
    assertThat(response.getContent()).isEqualTo("testContent");
  }

  @Test
  @DisplayName("게시글목록페이지조회")
  void getPageablePosts() throws Exception {
    //given
    Long boardId = 1L;
    int page = 0;
    int pageSize = 10;
    int number = 5;
    PageRequest pageRequest = PageRequest.of(page, pageSize);

    List<PostQueryDto> parents = createPostQueryDto(number, false);
    List<PostQueryDto> children = createPostQueryDto(number, true);
    Page<PostQueryDto> pageImpl = new PageImpl<>(parents, pageRequest, number);
    when(postRepository.findAllOriginal(any(), any()))
        .thenReturn(pageImpl);
    when(postRepository.findAllChildren(any(), any()))
        .thenReturn(children)
        .thenReturn(new ArrayList<>());

    //when
    Page<PostQueryDto> dtoPage = postService.getPageablePosts(boardId, pageRequest);

    //then
    assertThat(dtoPage.getTotalElements()).isEqualTo(number);
    assertThat(dtoPage.getContent().size()).isEqualTo(number);
  }

  private List<PostQueryDto> createPostQueryDto(int number, boolean isChildren) {
    List<PostQueryDto> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Long parentId = null;
      long id = i;
      if (isChildren) {
        parentId = (long) i;
        id = (i + number);
      }
      PostQueryDto dto = new PostQueryDto(
          parentId,
          id,
          "title" + i,
          "nickname" + i,
          LocalDateTime.now(),
          false);
      list.add(dto);
    }
    return list;
  }

  @Test
  @DisplayName("게시글업데이트")
  void updatePost() throws Exception {
    //given
    String updateTitle = "abc";
    PostUpdateRequest postUpdateRequest = new PostUpdateRequest();
    postUpdateRequest.setTitle(updateTitle);

    Long postId = 1L;
    Long boardId = 1L;
    Board board = new Board("testBoard");
    User user = User.createUser(
        "email",
        "nickname",
        "name",
        LocalDate.now(),
        "password",
        new Authority(Role.ROLE_USER));
    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );
    ReflectionTestUtils.setField(post, "id", postId);
    when(postRepository.findPostByIdAndBoardId(postId, boardId))
        .thenReturn(Optional.of(post));
    when(patchUtil.getValidatedFields(postUpdateRequest))
        .thenReturn(new ArrayList<>(Arrays.asList("title")));

    //when
    PostResponseWithContentAndModifiedAt response = postService.updatePost(postUpdateRequest, boardId, postId);

    //then
    assertThat(response.getId()).isEqualTo(postId);
    assertThat(response.getTitle()).isEqualTo(updateTitle);
  }

}