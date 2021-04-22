package com.personal.board.service;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.entity.Board;
import com.personal.board.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceUnitTest {

  @InjectMocks
  private BoardService boardService;

  @Mock
  private BoardRepository boardRepository;

  @Test
  @DisplayName("게시판등록")
  void addBoard() throws Exception {
    //given
    String boardName = "test";
    BoardRequest boardRequest = new BoardRequest();
    boardRequest.setName(boardName);
    Board board = new Board(boardName);
    when(boardRepository.save(any(Board.class)))
        .thenReturn(board);

    //when
    BoardResponseWithCreatedAt response = boardService.addBoard(boardRequest);

    //then
    assertThat(response.getName()).isEqualTo(boardName);
  }

  @Test
  @DisplayName("게시판정보조회")
  void getBoard() throws Exception {
    //given
    String boardName = "test";
    Board board = new Board(boardName);
    when(boardRepository.findById(any()))
        .thenReturn(Optional.of(board));

    //when
    BoardResponseWithDate response = boardService.getBoard(1L);

    //then
    assertThat(response.getName()).isEqualTo(boardName);
  }

  @Test
  @DisplayName("게시판목록조회")
  void getAllBoard() throws Exception {
    //given
    List<Board> boardList = createBoardList(10);
    when(boardRepository.findAll())
        .thenReturn(boardList);

    //when
    List<BoardResponseWithDate> response = boardService.getAllBoard();

    //then
    assertThat(response.size()).isEqualTo(10);
  }

  private List<Board> createBoardList(int number) {
    List<Board> boardList = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      boardList.add(new Board("test" + i));
    }
    return boardList;
  }

}