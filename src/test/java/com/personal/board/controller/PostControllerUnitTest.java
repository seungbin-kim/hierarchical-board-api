package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.query.PostQueryDto;
import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.response.post.PostResponseWithContentAndCreatedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(controllers = PostController.class)
class PostControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PostService postService;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;

  private String postTitle = "testTitle";
  private String postContent = "testContent";

  private String email = "test@test.com";
  private String name = "testName";
  private String nickname = "testNickname";
  private LocalDate birthday = LocalDate.parse("1997-05-28");
  private String password = "1234";
  private Authority authority = new Authority(Role.ROLE_USER);
  private User user = User.createUser(
      email,
      nickname,
      name,
      birthday,
      password,
      authority
  );

  @Test
  @DisplayName("게시글 등록")
  @WithMockUser("1")
  void addPost() throws Exception {
    //given
    Long boardId = 1L;
    Board board = new Board("test");
    ReflectionTestUtils.setField(board, "id", boardId);

    PostRequest postRequest = new PostRequest();
    postRequest.setTitle(postTitle);
    postRequest.setContent(postContent);

    Long postId = 1L;
    Post post = Post.createPost(
        board,
        user,
        postTitle,
        postContent,
        null
    );
    ReflectionTestUtils.setField(post, "id", postId);

    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(postRequest);

    when(postService.addPost(postRequest, boardId))
        .thenReturn(new PostResponseWithContentAndCreatedAt(post));

    //when
    ResultActions perform = mockMvc.perform(post("/api/v1/boards/{boardId}/posts", boardId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(postId))
        .andExpect(jsonPath("$.title").value(postTitle))
        .andExpect(jsonPath("$.content").value(postContent))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글페이징조회")
  @WithMockUser("1")
  void getPageablePost() throws Exception {
    //given
    int page = 0;
    int pageSize = 5;
    PageRequest pageRequest = PageRequest.of(page, pageSize);

    Long boardId = 1L;
    Board board = new Board("test");
    ReflectionTestUtils.setField(board, "id", boardId);

    int number = 5;
    List<PostQueryDto> parents = createPostQueryDto(number, false);

    PageImpl<PostQueryDto> response = new PageImpl<>(parents, pageRequest, number);
    when(postService.getPageablePosts(any(), any()))
        .thenReturn(response);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/boards/{boardId}/posts?page=0&size=5", boardId)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(pageSize)))
        .andExpect(jsonPath("$.content.[0].id").value(1))
        .andExpect(jsonPath("$.content.[1].id").value(2))
        .andExpect(jsonPath("$.totalElements").value(number))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andDo(print());
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

}