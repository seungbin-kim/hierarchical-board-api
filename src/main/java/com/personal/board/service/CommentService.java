package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.query.CommentQueryDto;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  private final PostService postService;

  private final UserService userService;

  private final SecurityUtil securityUtil;

  /**
   * 댓글 등록
   *
   * @param request 등록 댓글 정보
   * @param postId  등록할 게시글 id
   * @return 등록된 댓글
   */
  @Transactional
  public CommentResponseWithCreatedAt addComment(final CommentRequest request,
                                                 final Long postId) {

    Post post = postService.findPost(postId, null);

    Long userId = securityUtil.getCurrentUserId().get();
    User user = userService.findUser(userId);

    Comment comment = createComment(request, post, user);
    Comment savedPost = commentRepository.save(comment);
    return new CommentResponseWithCreatedAt(savedPost);
  }


  /**
   * 댓글 삭제
   *
   * @param postId    삭제할 댓글이 속한 게시글 id
   * @param commentId 삭제할 게시글 id
   */
  @Transactional
  public void deleteComment(final Long postId,
                            final Long commentId) {

    postService.findPost(postId, null);
    Comment targetComment = findComment(commentId, postId);
    securityUtil.checkAdminAndSameUser(targetComment.getUser().getId());

    checkAndDeleteComment(targetComment);
  }


  /**
   * 댓글목록 페이징 조회
   *
   * @param postId   조회할 게시글 id
   * @param pageable 페이징 정보
   * @return 페이징된 댓글목록
   */
  public Page<CommentQueryDto> getPageableComment(final Long postId,
                                                  final Pageable pageable) {

    postService.findPost(postId, null);
    return findCommentPage(postId, pageable);
  }


  /**
   * 댓글 업데이트
   *
   * @param request   업데이트 정보
   * @param postId    업데이트할 댓글이 속한 게시글 id
   * @param commentId 업데이트할 댓글 id
   * @return 업데이트된 댓글
   */
  @Transactional
  public CommentResponseWithModifiedAt updateComment(final CommentUpdateRequest request,
                                                     final Long postId,
                                                     final Long commentId) {

    postService.findPost(postId, null);

    Comment findComment = findComment(commentId, postId);
    securityUtil.checkAdminAndSameUser(findComment.getUser().getId());

    findComment.updateComment(request.getContent());

    return new CommentResponseWithModifiedAt(findComment);
  }


  /**
   * 답변형 댓글 페이징하여 찾기
   *
   * @param postId
   * @param pageable
   * @return
   */
  private Page<CommentQueryDto> findCommentPage(final Long postId,
                                                final Pageable pageable) {

    Page<CommentQueryDto> originalPage = commentRepository.findAllOriginal(postId, pageable);
    List<CommentQueryDto> parentListForLoop = originalPage.getContent();
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = extractCommentIds(parentListForLoop);

      List<CommentQueryDto> children = commentRepository.findAllChildren(postId, parentIds);
      Map<Long, List<CommentQueryDto>> childrenCommentMap = mapByParentId(children);

      setReply(parentListForLoop, childrenCommentMap);

      parentListForLoop = children;
    }
    return originalPage;
  }


  /**
   * 댓글 엔티티 생성
   *
   * @param request 생성할 댓글 정보
   * @param post    생성할 댓글이 등록될 게시글 엔티티
   * @param user    등록 유저의 엔티티
   * @return 생성된 댓글의 엔티티
   */
  private Comment createComment(final CommentRequest request,
                                final Post post,
                                final User user) {

    Comment parentComment = findParentComment(request.getParentId(), post.getId());

    return Comment.createComment(
        post,
        user,
        request.getContent(),
        parentComment
    );
  }


  /**
   * 부모댓글 찾기
   *
   * @param parentId 부모댓글의 id
   * @param postId   부모댓글이 속한 게시판 id
   * @return 부모글 엔티티 또는 null(원글)
   */
  private Comment findParentComment(final Long parentId,
                                    final Long postId) {

    Comment parentComment = null;
    if (parentId != null) {
      parentComment = findComment(parentId, postId);
    }
    return parentComment;
  }


  /**
   * 답 댓글 설정
   *
   * @param parentList         부모댓글 리스트
   * @param childrenCommentMap 부모글 id로 분류된 답 댓글들의 맵
   */
  private void setReply(final List<CommentQueryDto> parentList,
                        final Map<Long, List<CommentQueryDto>> childrenCommentMap) {

    parentList
        .forEach(c -> c.setReply(childrenCommentMap.get(c.getId())));
  }


  /**
   * 각 댓글의 부모댓글 id 를 키로하여 답글 분류
   *
   * @param children 부모댓글의 id로 분류할 답 댓글리스트
   * @return 부모글의 id로 분류된 답 댓글들의 맵
   */
  private Map<Long, List<CommentQueryDto>> mapByParentId(final List<CommentQueryDto> children) {

    return children.stream()
        .collect(Collectors.groupingBy(CommentQueryDto::getParentId));
  }


  /**
   * 댓글 id 리스트 추출
   *
   * @param commentList id를 추출할 댓글 리스트
   * @return 추출된 id 리스트
   */
  private List<Long> extractCommentIds(final List<CommentQueryDto> commentList) {

    return commentList.stream()
        .map(CommentQueryDto::getId)
        .collect(toList());
  }


  /**
   * 삭제 가능한 댓글 확인 및 삭제
   *
   * @param targetComment 삭제할 댓글
   */
  private void checkAndDeleteComment(final Comment targetComment) {

    if (targetComment.getChildren().isEmpty()) {
      commentRepository.delete(findDeletableAncestorComment(targetComment));
    } else {
      targetComment.changeDeletionStatus();
    }
  }


  /**
   * 지울 수 있는 조상댓글 찾기
   *
   * @param targetComment 지울 댓글
   * @return 지울 수 있는 조상글
   */
  private Comment findDeletableAncestorComment(final Comment targetComment) {

    Comment parent = targetComment.getParent();
    if (parent != null && parent.getChildren().size() == 1 && parent.isDeleted()) {
      return findDeletableAncestorComment(parent);
    }
    return targetComment;
  }


  /**
   * 댓글 조회
   *
   * @param commentId 조회할 댓글 id
   * @param postId    조회할 댓글이 속한 게시글 id
   * @return 조회된 댓글
   */
  private Comment findComment(final Long commentId,
                              final Long postId) {

    Optional<Comment> commentById = commentRepository.findByIdAndPostId(commentId, postId);
    commentById.orElseThrow(CommentNotFoundException::new);

    Comment findComment = commentById.get();
    if (findComment.isDeleted()) {
      throw new BadArgumentException("comment has been deleted.");
    }
    return findComment;
  }

}
