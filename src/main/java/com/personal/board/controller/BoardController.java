package com.personal.board.controller;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

  private static final String BOARDS = "/api/v1/boards";

  private static final String BOARD = BOARDS + "/{boardId}";

  private final BoardService boardService;

  @PostMapping("/boards")
  public ResponseEntity<BoardResponseWithCreatedAt> addBoard(@RequestBody @Valid final BoardRequest request) {
    BoardResponseWithCreatedAt boardResponse = boardService.addBoard(request);
    return ResponseEntity
        .created(new UriTemplate(BOARD).expand(boardResponse.getId()))
        .body(boardResponse);
  }

  @GetMapping("/boards")
  public ResponseEntity<ResultResponse<List<BoardResponseWithDate>>> getAllBoard() {
    return ResponseEntity
        .ok(new ResultResponse<>(boardService.getAllBoard()));
  }

  @GetMapping("/boards/{boardId}")
  public ResponseEntity<BoardResponseWithDate> getBoard(@PathVariable final Long boardId) {
    return ResponseEntity
        .ok(boardService.getBoard(boardId));
  }

}
