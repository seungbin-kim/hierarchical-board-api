package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.query.CommentQueryDto;
import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.entity.*;
import com.personal.board.enumeration.Role;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.CommentService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CommentService commentService;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("댓글등록")
  @WithMockUser("1")
  void addComment() throws Exception {
    //given
    String content = "test";
    CommentRequest request = new CommentRequest();
    request.setParentId(null);
    request.setContent(content);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestContent = objectMapper.writeValueAsString(request);

    Long postId = 1L;
    Long userId = 1L;
    Long boardId = 1L;
    Long commentId = 1L;

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

    CommentResponseWithCreatedAt response = new CommentResponseWithCreatedAt(comment);
    when(commentService.addComment(request, postId))
        .thenReturn(response);

    //when
    ResultActions perform = mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestContent)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(commentId))
        .andExpect(jsonPath("$.content").value(content))
        .andDo(print());
  }

  @Test
  @DisplayName("댓글페이지조회")
  void getPageableComment() throws Exception {
    //given
    int page = 0;
    int pageSize = 5;
    PageRequest pageRequest = PageRequest.of(page, pageSize);

    Long postId = 1L;
    Long userId = 1L;
    Long boardId = 1L;
    Long commentId = 1L;

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

    int number = 5;
    List<CommentQueryDto> parents = createCommentQueryDto(number, false);
    PageImpl<CommentQueryDto> response = new PageImpl<>(parents, pageRequest, number);
    when(commentService.getPageableComment(postId, pageRequest))
        .thenReturn(response);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
        .queryParam("page", String.valueOf(page))
        .queryParam("size", String.valueOf(pageSize))
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(number)))
        .andExpect(jsonPath("$.content.[0].id").value(number))
        .andExpect(jsonPath("$.content.[1].id").value(number - 1))
        .andExpect(jsonPath("$.totalElements").value(number))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andDo(print());
  }

  private List<CommentQueryDto> createCommentQueryDto(int number, boolean isChildren) {
    List<CommentQueryDto> list = new ArrayList<>();
    for (int i = number; i >= 1; i--) {
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