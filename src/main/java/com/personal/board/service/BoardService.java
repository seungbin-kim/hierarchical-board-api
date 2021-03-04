package com.personal.board.service;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.entity.Board;
import com.personal.board.exception.NameDuplicatedException;
import com.personal.board.exception.NotFoundException;
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
    if (boardRepository.checkBoardName(request.getName())) {
      throw new NameDuplicatedException();
    }

    Board board = new Board(
        request.getName());

    Board savedBoard = boardRepository.save(board);

    return new BoardResponseWithCreatedAt(savedBoard);
  }

  public List<BoardResponseWithDate> getAllBoard() {
    return boardRepository.findAllBoard()
        .stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());
  }

  public BoardResponseWithDate getBoard(final Long id) {
    Optional<Board> boardById = boardRepository.findBoardById(id);
    if (boardById.isEmpty()) {
      throw new NotFoundException("board id not found.");
    }
    return new BoardResponseWithDate(boardById.get());
  }

}
