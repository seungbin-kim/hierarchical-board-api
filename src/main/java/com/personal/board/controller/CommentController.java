package com.personal.board.controller;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.dto.response.comment.CommentListResponse;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

  private static final String COMMENTS = "/api/v1/posts/{postId}/comments";

  private static final String COMMENT = COMMENTS + "/{commentId}";

  private final CommentService commentService;

  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<CommentResponseWithCreatedAt> addComment(
      @RequestBody @Valid final CommentRequest request, @PathVariable final Long postId) {
    CommentResponseWithCreatedAt commentResponse = commentService.addComment(request, postId);
    return ResponseEntity
        .created(new UriTemplate(COMMENT)
            .expand(postId, commentResponse.getId()))
        .body(commentResponse);
  }

  @GetMapping("/posts/{postId}/comments")
  public ResponseEntity<ResultResponse<List<CommentListResponse>>> getAllComment(@PathVariable final Long postId) {
    return ResponseEntity
        .ok(new ResultResponse<>(commentService.getAllComment(postId)));
  }

  @PatchMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<CommentResponseWithModifiedAt> patchComment(
      @RequestBody final CommentUpdateRequest request, @PathVariable final Long postId, @PathVariable final Long commentId) {
    return ResponseEntity
        .ok(commentService.updateComment(request, postId, commentId));
  }

  @DeleteMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity deleteComment(@PathVariable final Long postId, @PathVariable final Long commentId) {
    commentService.deleteComment(postId, commentId);
    return ResponseEntity
        .noContent()
        .build();
  }

}
