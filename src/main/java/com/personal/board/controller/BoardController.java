package com.personal.board.controller;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.ListResponse;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.service.BoardService;
import com.personal.board.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

  @PostMapping("/boards")
  public ResponseEntity<BoardResponseWithCreatedAt> addBoard(
      @RequestBody @Valid final BoardRequest request,
      final Authentication authentication) {

    AuthenticationUtil.checkAdmin(authentication);

    BoardResponseWithCreatedAt boardResponse = boardService.addBoard(request);
    return ResponseEntity
        .created(new UriTemplate(BOARD).expand(boardResponse.getId()))
        .body(boardResponse);
  }

  @GetMapping("/boards")
  public ResponseEntity<ListResponse<BoardResponseWithDate>> getAllBoard() {
    return ResponseEntity
        .ok(new ListResponse<>(boardService.getAllBoard()));
  }

  @GetMapping("/boards/{boardId}")
  public ResponseEntity<BoardResponseWithDate> getBoard(
      @PathVariable final Long boardId) {

    return ResponseEntity
        .ok(boardService.getBoard(boardId));
  }

}
