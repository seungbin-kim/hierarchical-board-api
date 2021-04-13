package com.personal.board.controller;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.query.CommentQueryDto;
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


  /**
   * 댓글 추가
   * @param request 댓글 정보
   * @param postId  등록할 게시글 id
   * @return 등록한 댓글정보
   */
  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<CommentResponseWithCreatedAt> addComment(@RequestBody @Valid final CommentRequest request,
                                                                 @PathVariable final Long postId) {

    CommentResponseWithCreatedAt commentResponse = commentService.addComment(request, postId);
    return ResponseEntity
        .created(new UriTemplate(COMMENT)
            .expand(postId, commentResponse.getId()))
        .body(commentResponse);
  }


  /**
   * 댓글 페이징 조회
   * @param postId   댓글 조회할 게시글 id
   * @param pageable 페이징 정보
   * @return 페이징된 댓글 목록
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/posts/{postId}/comments")
  public Page<CommentQueryDto> getPageableComment(@PathVariable final Long postId,
                                                  @PageableDefault(size = 5) final Pageable pageable) {

    return commentService.getPageableComment(postId, pageable);
  }


  /**
   * 댓글 수정
   * @param request   수정 정보
   * @param postId    게시글 id
   * @param commentId 댓글 id
   * @return 수정된 댓글 정보
   */
  @PatchMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<CommentResponseWithModifiedAt> patchComment(@RequestBody final CommentUpdateRequest request,
                                                                    @PathVariable final Long postId,
                                                                    @PathVariable final Long commentId) {

    return ResponseEntity
        .ok(commentService.updateComment(request, postId, commentId));
  }


  /**
   * 댓글 삭제
   * @param postId    댓글이 등록된 게시글 id
   * @param commentId 댓글 id
   * @return 상태코드 204
   */
  @DeleteMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<?> deleteComment(@PathVariable final Long postId,
                                         @PathVariable final Long commentId) {

    commentService.deleteComment(postId, commentId);
    return ResponseEntity
        .noContent()
        .build();
  }

}
