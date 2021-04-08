package com.personal.board.controller;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.repository.query.CommentQueryDto;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

  private static final String COMMENTS = "/api/v1/posts/{postId}/comments";

  private static final String COMMENT = COMMENTS + "/{commentId}";

  private final CommentService commentService;

  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<CommentResponseWithCreatedAt> addComment(
      @RequestBody @Valid final CommentRequest request,
      @PathVariable final Long postId) {

    CommentResponseWithCreatedAt commentResponse = commentService.addComment(request, postId);
    return ResponseEntity
        .created(new UriTemplate(COMMENT)
            .expand(postId, commentResponse.getId()))
        .body(commentResponse);
  }

//  @GetMapping("/posts/{postId}/comments")
//  public ResponseEntity<PageQueryDto<CommentQueryDto>> getPageableComment(
//      @PathVariable final Long postId,
//      @RequestParam(defaultValue = "5") @Min(value = 1, message = "size must be at least 1.") final int size,
//      @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be at least 0.") final int page) {
//
//    return ResponseEntity
//        .ok(commentService.getPageableComment(postId, size, page));
//  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/posts/{postId}/comments")
  public Page<CommentQueryDto> getPageableComment(
      @PathVariable final Long postId,
      @PageableDefault(size = 5) Pageable pageable) {

    return commentService.getPageableComment(postId, pageable);
  }

  @PatchMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<CommentResponseWithModifiedAt> patchComment(
      @RequestBody final CommentUpdateRequest request,
      @PathVariable final Long postId,
      @PathVariable final Long commentId) {

    return ResponseEntity
        .ok(commentService.updateComment(request, postId, commentId));
  }

  @DeleteMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<?> deleteComment(
      @PathVariable final Long postId,
      @PathVariable final Long commentId) {
    
    commentService.deleteComment(postId, commentId);
    return ResponseEntity
        .noContent()
        .build();
  }

}
