package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.BoardRequest;
import com.personal.board.entity.Board;
import com.personal.board.repository.BoardRepository;
import com.personal.board.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE board_seq RESTART WITH 1").executeUpdate();
  }

  String boardName = "testBoard";

  @Test
  @DisplayName("게시판추가")
  @WithMockUser(roles = "ADMIN")
  void addBoard() throws Exception {
    //given
    BoardRequest boardRequest = new BoardRequest();
    boardRequest.setName(boardName);
    String content = new ObjectMapper()
        .writeValueAsString(boardRequest);

    Board board = new Board(boardName);
    ReflectionTestUtils.setField(board, "id", 1L);

    //when
    ResultActions perform = mockMvc.perform(post("/api/v1/boards")
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value(boardName))
        .andDo(print());
  }

  @Test
  @DisplayName("게시판목록조회")
  void getAllBoard() throws Exception {
    //given
    List<Board> boardList = createBoard(10);
    boardRepository.saveAll(boardList);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/boards")
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(10)))
        .andDo(print());
  }

  private List<Board> createBoard(int number) {
    List<Board> list = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Board board = new Board("testBoard" + i);
      ReflectionTestUtils.setField(board, "id", (long) i);
      list.add(board);
    }
    return list;
  }

  @Test
  @DisplayName("게시판단건조회")
  @WithMockUser(roles = "ADMIN")
  void getBoard() throws Exception {
    //given
    Long id = 1L;
    Board board = new Board(boardName);
    boardRepository.save(board);

    //when
    ResultActions perform = mockMvc.perform(get("/api/v1/boards/{id}", id)
        .accept(MediaType.APPLICATION_JSON));

    //then
    perform
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andDo(print());
  }

}