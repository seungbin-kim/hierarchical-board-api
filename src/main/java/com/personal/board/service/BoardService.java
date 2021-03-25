package com.personal.board.service;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.entity.Board;
import com.personal.board.exception.BoardNotFoundException;
import com.personal.board.exception.NameDuplicatedException;
import com.personal.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;


  public BoardResponseWithCreatedAt addBoard(final BoardRequest request) {
    if (boardRepository.findBoardByName(request.getName()).isPresent()) {
      throw new NameDuplicatedException();
    }
    Board board = new Board(request.getName());
    Board savedBoard = boardRepository.save(board);
    return new BoardResponseWithCreatedAt(savedBoard);
  }


  @Transactional(readOnly = true)
  public List<BoardResponseWithDate> getAllBoard() {
    return boardRepository.findAllBoard()
        .stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public BoardResponseWithDate getBoard(final Long boardId) {
    Optional<Board> boardById = boardRepository.findBoardById(boardId);
    boardById.orElseThrow(BoardNotFoundException::new);

    return new BoardResponseWithDate(boardById.get());
  }


  public Board checkBoard(final Long boardId) {
    Optional<Board> boardById = boardRepository.findBoardById(boardId);
    boardById.orElseThrow(BoardNotFoundException::new);
    return boardById.get();
  }

}
