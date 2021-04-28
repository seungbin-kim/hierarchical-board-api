package com.personal.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.entity.Board;
import com.personal.board.jwt.JwtAccessDeniedHandler;
import com.personal.board.jwt.JwtAuthenticationEntryPoint;
import com.personal.board.jwt.TokenProvider;
import com.personal.board.service.BoardService;
import com.personal.board.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

  @Test
  @WithMockUser(roles = "ADMIN")
  void addBoard() throws Exception {
    //given
    String boardName = "testBoard";
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

}