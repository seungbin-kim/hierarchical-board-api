package com.personal.board.controller;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.ResultResponse;
import com.personal.board.dto.response.post.*;
import com.personal.board.exception.ReflectIllegalAccessException;
import com.personal.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

  private static final String POSTS = "/api/v1/boards/{boardId}/posts";

  private static final String POST = POSTS + "/{postId}";

  private final PostService postService;

  @PostMapping("/boards/{boardId}/posts")
  public ResponseEntity<PostResponseWithContentAndCreatedAt> addPost(
      @RequestBody @Valid final PostRequest request,
      @PathVariable final Long boardId) {

    PostResponseWithContentAndCreatedAt postResponse = postService.addPost(request, boardId);
    return ResponseEntity
        .created(new UriTemplate(POST)
            .expand(boardId, postResponse.getId()))
        .body(postResponse);
  }

  @GetMapping("/boards/{boardId}/posts")
  public ResponseEntity<ResultResponse<List<PostDto>>> getAllPost(
      @PathVariable final Long boardId) {

    return ResponseEntity
        .ok(new ResultResponse<>(postService.getAllPost(boardId)));
  }

  @GetMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndDate> getPost(
      @PathVariable final Long boardId,
      @PathVariable final Long postId) {

    return ResponseEntity
        .ok(postService.getPost(boardId, postId));
  }

  @PatchMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndModifiedAt> patchPost(
      @RequestBody final PostUpdateRequest request,
      @PathVariable final Long boardId,
      @PathVariable final Long postId) {

    try {
      return ResponseEntity
          .ok(postService.updatePost(request, boardId, postId));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }

  @DeleteMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<?> deletePost(
      @PathVariable final Long boardId,
      @PathVariable final Long postId) {

    postService.deletePost(boardId, postId);
    return ResponseEntity
        .noContent()
        .build();
  }

}
