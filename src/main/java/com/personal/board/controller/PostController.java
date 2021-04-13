package com.personal.board.controller;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.*;
import com.personal.board.exception.ReflectIllegalAccessException;
import com.personal.board.dto.query.PostQueryDto;
import com.personal.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

  private static final String POSTS = "/api/v1/boards/{boardId}/posts";

  private static final String POST = POSTS + "/{postId}";

  private final PostService postService;


  /**
   * 게시글 등록
   *
   * @param request 등록 요청정보
   * @param boardId 등록할 게시판 id
   * @return 등록된 게시글 정보
   */
  @PostMapping("/boards/{boardId}/posts")
  public ResponseEntity<PostResponseWithContentAndCreatedAt> addPost(@RequestBody @Valid final PostRequest request,
                                                                     @PathVariable final Long boardId) {

    PostResponseWithContentAndCreatedAt postResponse = postService.addPost(request, boardId);
    return ResponseEntity
        .created(new UriTemplate(POST)
            .expand(boardId, postResponse.getId()))
        .body(postResponse);
  }


  /**
   * 게시글 페이징 조회
   *
   * @param boardId  조회할 게시판 id
   * @param pageable 페이징 정보
   * @return 페이징된 게시글 목록
   */
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/boards/{boardId}/posts")
  public Page<PostQueryDto> getPageablePost(@PathVariable final Long boardId,
                                            @PageableDefault(size = 5) final Pageable pageable) {

    return postService.getPageablePosts(boardId, pageable);
  }


  /**
   * 게시글 단건조회
   *
   * @param boardId 게시판 id
   * @param postId  게시글 id
   * @return 게시글 정보
   */
  @GetMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndDate> getPost(@PathVariable final Long boardId,
                                                                @PathVariable final Long postId) {

    return ResponseEntity
        .ok(postService.getPost(boardId, postId));
  }


  /**
   * 게시글 수정
   *
   * @param request 수정 정보
   * @param boardId 게시판 id
   * @param postId  게시글 id
   * @return 수정된 게시글 정보
   */
  @PatchMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<PostResponseWithContentAndModifiedAt> patchPost(@RequestBody final PostUpdateRequest request,
                                                                        @PathVariable final Long boardId,
                                                                        @PathVariable final Long postId) {

    try {
      return ResponseEntity
          .ok(postService.updatePost(request, boardId, postId));
    } catch (IllegalAccessException exception) {
      throw new ReflectIllegalAccessException();
    }
  }


  /**
   * 게시글 삭제
   *
   * @param boardId 게시판 id
   * @param postId  게시글 id
   * @return 상태코드 204
   */
  @DeleteMapping("/boards/{boardId}/posts/{postId}")
  public ResponseEntity<?> deletePost(@PathVariable final Long boardId,
                                      @PathVariable final Long postId) {

    postService.deletePost(boardId, postId);
    return ResponseEntity
        .noContent()
        .build();
  }

}
