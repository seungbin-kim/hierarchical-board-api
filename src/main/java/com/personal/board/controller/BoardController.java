package com.personal.board.controller;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.dto.response.post.PostListResponse;
import com.personal.board.dto.response.post.PostResponseWithContentAndCreatedAt;
import com.personal.board.dto.response.post.PostResponseWithContentAndDate;
import com.personal.board.dto.response.post.PostResponseWithContentAndModifiedAt;
import com.personal.board.exception.ReflectIllegalAccessException;
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

  private static final String POSTS = BOARD + "/posts";
  private static final String POST = POSTS + "/{postId}";

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

  @PostMapping("/boards/{boardId}/posts")
  public ResponseEntity<PostResponseWithContentAndCreatedAt> uploadPost(
      @RequestBody @Valid final PostRequest request, @PathVariable final Long boardId) {
    PostResponseWithContentAndCreatedAt postResponse = boardService.addPost(request, boardId);
    return ResponseEntity
        .created(new UriTemplate(POST)
            .expand(boardId, postResponse.getId()))
        .body(postResponse);
  }

  @GetMapping("/boards/{boardId}/posts")
  public ResponseEntity<ResultResponse<List<PostListResponse>>> getAllPost(@PathVariable final Long boardId) {
    return ResponseEntity
        .ok(new ResultResponse<>(boardService.getAllPost(boardId)));
  }

  @GetMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndDate> getPost(
      @PathVariable final Long boardId, @PathVariable final Long postId) {
    return ResponseEntity
        .ok(boardService.getPost(boardId, postId));
  }

  @PatchMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndModifiedAt> patchPost(
      @RequestBody final PostUpdateRequest request, @PathVariable final Long boardId, @PathVariable final Long postId) {
    try {
      return ResponseEntity
          .ok(boardService.updatePost(request, boardId, postId));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }

  @DeleteMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity deletePost(@PathVariable final Long boardId, @PathVariable final Long postId) {
    boardService.deletePost(boardId, postId);
    return ResponseEntity
        .noContent()
        .build();
  }
}
