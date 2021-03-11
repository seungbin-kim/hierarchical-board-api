package com.personal.board.service;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.entity.Board;
import com.personal.board.exception.BoardNotFoundException;
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
    if (boardRepository.findBoardByName(request.getName()).isEmpty()) { // 게시판이름 중복체크
      throw new NameDuplicatedException(); // 중복발생시 예외발생
    }
    Board board = new Board(request.getName());
    Board savedBoard = boardRepository.save(board);
    return new BoardResponseWithCreatedAt(savedBoard);
  }


  @Transactional(readOnly = true)
  public List<BoardResponseWithDate> getAllBoard() { // 모든게시판을 조회하여 응답 Dto로 바꾸어 반환
    return boardRepository.findAllBoard()
        .stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public BoardResponseWithDate getBoard(final Long boardId) { // 요청 id에 대한 게시판을 조회
    Optional<Board> boardById = boardRepository.findBoardById(boardId);
    if (boardById.isEmpty()) { // 조회 안될 시 예외발생
      throw new BoardNotFoundException();
    }
    return new BoardResponseWithDate(boardById.get()); // 응답 Dto를 만들어서 반환
  }

}
