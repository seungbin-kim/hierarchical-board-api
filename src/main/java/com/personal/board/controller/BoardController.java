package com.personal.board.controller;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.ListResponse;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.service.BoardService;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

  private static final String BOARDS = "/api/v1/boards";

  private static final String BOARD = BOARDS + "/{boardId}";

  private final BoardService boardService;

  private final SecurityUtil securityUtil;


  /**
   * 게시판 등록
   *
   * @param request 등록 요청정보
   * @return 등록된 게시판 정보
   */
  @PostMapping("/boards")
  public ResponseEntity<BoardResponseWithCreatedAt> addBoard(@RequestBody @Valid final BoardRequest request) {

    if (!securityUtil.isAdmin()) {
      throw new AccessDeniedException("관리자가 아님");
    }

    BoardResponseWithCreatedAt boardResponse = boardService.addBoard(request);
    return ResponseEntity
        .created(new UriTemplate(BOARD).expand(boardResponse.getId()))
        .body(boardResponse);
  }


  /**
   * 게시판 목록 조회
   *
   * @return 등록된 게시판 목록
   */
  @GetMapping("/boards")
  public ResponseEntity<ListResponse<BoardResponseWithDate>> getAllBoard() {

    return ResponseEntity
        .ok(new ListResponse<>(boardService.getAllBoard()));
  }


  /**
   * 게시판 단건조회
   *
   * @param boardId 조회할 게시판 id
   * @return 게시판 정보
   */
  @GetMapping("/boards/{boardId}")
  public ResponseEntity<BoardResponseWithDate> getBoard(@PathVariable final Long boardId) {

    return ResponseEntity
        .ok(boardService.getBoard(boardId));
  }

}
