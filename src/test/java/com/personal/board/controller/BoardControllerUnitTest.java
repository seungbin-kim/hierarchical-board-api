package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.entity.Board;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.BoardService;
import com.personal.board.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BoardController.class)
class BoardControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BoardService boardService;

  @MockBean
  private SecurityUtil securityUtil;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;

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

    when(securityUtil.isAdmin())
        .thenReturn(true);
    when(boardService.addBoard(boardRequest))
        .thenReturn(new BoardResponseWithCreatedAt(board));

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
    List<BoardResponseWithDate> responseWithDates = boardList.stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());

    when(boardService.getAllBoard())
        .thenReturn(responseWithDates);

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
    ReflectionTestUtils.setField(board, "id", id);

    when(boardService.getBoard(id))
        .thenReturn(new BoardResponseWithDate(board));
    
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