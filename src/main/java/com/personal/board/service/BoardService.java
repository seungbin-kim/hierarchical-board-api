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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;


  /**
   * 게시판 등록
   *
   * @param request 등록할 게시판 정보
   * @return 등록된 게시판
   */
  @Transactional
  public BoardResponseWithCreatedAt addBoard(final BoardRequest request) {

    checkDuplicate(request.getName());
    Board board = new Board(request.getName());
    Board savedBoard = boardRepository.save(board);
    return new BoardResponseWithCreatedAt(savedBoard);
  }


  /**
   * 게시판 단건조회
   *
   * @param boardId 조회할 게시판 id
   * @return 조회된 게시판
   */
  public BoardResponseWithDate getBoard(final Long boardId) {

    Board board = findBoard(boardId);
    return new BoardResponseWithDate(board);
  }


  /**
   * 게시판목록 조회
   *
   * @return 게시판목록
   */
  public List<BoardResponseWithDate> getAllBoard() {

    return boardRepository.findAll()
        .stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());
  }


  /**
   * 게시판 조회
   *
   * @param boardId 조회할 게시판 id
   * @return 조회된 게시판
   */
  public Board findBoard(final Long boardId) {

    Optional<Board> boardById = boardRepository.findById(boardId);
    boardById.orElseThrow(BoardNotFoundException::new);
    return boardById.get();
  }


  /**
   * 게시판 이름 중복검사
   *
   * @param name 이름
   */
  private void checkDuplicate(String name) {

    if (boardRepository.existsByName(name)) {
      throw new NameDuplicatedException();
    }
  }

}
