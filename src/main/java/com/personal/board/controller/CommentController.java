package com.personal.board.controller;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.dto.response.comment.CommentListResponse;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
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

}
