package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.CommentRequest;
import com.personal.board.entity.*;
import com.personal.board.enumeration.Role;
import com.personal.board.service.CommentService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CommentService commentService;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE post_seq RESTART WITH 1").executeUpdate();
    em.createNativeQuery("ALTER SEQUENCE user_seq RESTART WITH 1").executeUpdate();
    em.createNativeQuery("ALTER SEQUENCE comment_seq RESTART WITH 1").executeUpdate();
  }

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
    em.persist(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    em.persist(user);

    Post post = Post.createPost(
        board,
        user,
        "testTitle",
        "testContent",
        null
    );

    em.persist(post);
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

}