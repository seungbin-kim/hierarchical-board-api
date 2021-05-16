package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.entity.*;
import com.personal.board.enumeration.Role;
import com.personal.board.repository.BoardRepository;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
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

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private EntityManager em;

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
        "testTitle",
        "testContent",
        null
    );
    postRepository.save(post);
    Long postId = post.getId();

    Long expectedCommentId = 1L;

    //when
    ResultActions perform = mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestContent)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(expectedCommentId))
        .andExpect(jsonPath("$.content").value(content))
        .andDo(print());
  }

  @Test
  @DisplayName("댓글페이지조회")
  void getPageableComment() throws Exception {
    //given
    int page = 0;
    int pageSize = 5;

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
        "testTitle",
        "testContent",
        null
    );
    postRepository.save(post);
    Long postId = post.getId();

    int number = 10;
    List<Comment> parents = createComment(number, post, user,false);
    commentRepository.saveAll(parents);
    List<Comment> children = createComment(number, post, user, true);
    commentRepository.saveAll(children);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
        .queryParam("page", String.valueOf(page))
        .queryParam("size", String.valueOf(pageSize))
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(pageSize)))
        .andExpect(jsonPath("$.content.[0].id").value(1L))
        .andExpect(jsonPath("$.content.[1].id").value(2L))
        .andExpect(jsonPath("$.totalElements").value(number))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andDo(print());
  }

  private List<Comment> createComment(int number, Post post, User user, boolean isChildren) {
    List<Comment> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Comment parent = null;
      if (isChildren) {
        parent = commentRepository.findById((long) i).get();
      }
      Comment comment = Comment.createComment(
          post,
          user,
          "content" + i,
          parent
      );
      list.add(comment);
    }
    return list;
  }

  @Test
  @DisplayName("댓글수정")
  @WithMockUser("1")
  void patchComment() throws Exception {
    //given
    Board board = new Board("testBoard");
    boardRepository.save(board);
    Long boardId = board.getId();

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);
    Long userId = user.getId();

    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );
    postRepository.save(post);
    Long postId = post.getId();

    Comment comment = Comment.createComment(
        post,
        user,
        "test",
        null
    );
    commentRepository.save(comment);
    Long commentId = comment.getId();

    String updateContent = "update";
    CommentUpdateRequest request = new CommentUpdateRequest();
    request.setContent(updateContent);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestContent = objectMapper.writeValueAsString(request);

    //when
    ResultActions perform = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestContent)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(commentId))
        .andExpect(jsonPath("$.content").value(updateContent))
        .andDo(print());
  }

  @Test
  @DisplayName("댓글삭제")
  @WithMockUser("1")
  void deleteComment() throws Exception {
    //given
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
    Long userId = user.getId();

    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );
    postRepository.save(post);
    Long postId = post.getId();

    Comment comment = Comment.createComment(
        post,
        user,
        "test",
        null
    );
    commentRepository.save(comment);
    Long commentId = comment.getId();

    //when
    ResultActions perform = mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId));

    //then
    perform
        .andExpect(status().isNoContent())
        .andDo(print());
  }

}