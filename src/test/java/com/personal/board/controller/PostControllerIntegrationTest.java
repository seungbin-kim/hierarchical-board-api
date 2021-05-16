package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.entity.Authority;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.BoardRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE board_seq RESTART WITH 1").executeUpdate();
    em.createNativeQuery("ALTER SEQUENCE post_seq RESTART WITH 1").executeUpdate();
    em.createNativeQuery("ALTER SEQUENCE user_seq RESTART WITH 1").executeUpdate();
  }

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
  @DisplayName("게시글등록")
  @WithMockUser("1")
  void addPost() throws Exception {
    //given
    Board board = new Board("test");
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    userRepository.save(user);

    PostRequest postRequest = new PostRequest();
    postRequest.setTitle(postTitle);
    postRequest.setContent(postContent);
    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(postRequest);

    //when
    ResultActions perform = mockMvc.perform(post("/api/v1/boards/{boardId}/posts", boardId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
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

    userRepository.save(user);

    Board board = new Board("test");
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    int number = 10;
    List<Post> parentPostList = createPost(number, boardId, false);
    postRepository.saveAll(parentPostList);
    List<Post> childrenPostList = createPost(number, boardId, true);
    postRepository.saveAll(childrenPostList);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/boards/{boardId}/posts", boardId)
        .queryParam("page", String.valueOf(page))
        .queryParam("size", String.valueOf(pageSize))
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(pageSize)))
        .andExpect(jsonPath("$.content.[0].id").value(number))
        .andExpect(jsonPath("$.content.[1].id").value(number - 1))
        .andExpect(jsonPath("$.totalElements").value(number))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andDo(print());
  }

  private List<Post> createPost(int number, Long boardId, boolean isChildren) {
    Board board = boardRepository.findById(boardId).get();
    List<Post> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Post parent = null;
      if (isChildren) {
        parent = postRepository.findById((long)i).get();
      }
      Post post = Post.createPost(
          board,
          user,
          postTitle,
          postContent,
          parent
      );
      list.add(post);
    }
    return list;
  }

  @Test
  @DisplayName("게시글단건조회")
  void getPost() throws Exception {
    //given
    userRepository.save(user);

    Board board = new Board("test");
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    int number = 5;
    List<Post> postList = createPost(number, boardId, false);
    postRepository.saveAll(postList);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/boards/{boardId}/posts/{postId}", boardId, 1L)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value(postTitle))
        .andExpect(jsonPath("$.content").value(postContent))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글수정")
  @WithMockUser("1")
  void patchPost() throws Exception {
    //given
    userRepository.save(user);

    Board board = new Board("test");
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    int number = 5;
    List<Post> postList = createPost(number, boardId, false);
    postRepository.saveAll(postList);

    String changeTitle = "changeTitle";
    PostUpdateRequest request = new PostUpdateRequest();
    request.setTitle(changeTitle);

    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(request);

    //when
    ResultActions perform = mockMvc.perform(patch("/api/v1/boards/{boardId}/posts/{postId}", boardId, 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value(changeTitle))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글삭제")
  @WithMockUser("1")
  void deletePost() throws Exception {
    //given
    userRepository.save(user);

    Board board = new Board("test");
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    int number = 5;
    List<Post> postList = createPost(number, boardId, false);
    postRepository.saveAll(postList);

    //when
    ResultActions perform = mockMvc.perform(delete("/api/v1/boards/{boardId}/posts/{postId}", boardId, 1L));

    //then
    perform
        .andExpect(status().isNoContent())
        .andDo(print());
  }

}