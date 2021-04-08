package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.repository.query.CommentQueryDto;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

  private final UserRepository userRepository;

  private final CommentRepository commentRepository;

  private final PostService postService;


  public CommentResponseWithCreatedAt addComment(final CommentRequest request, final Long postId) {
    Post findPost = postService.checkPost(postId, null);
    Long userId = SecurityUtil.getCurrentUserId().get();
    Optional<User> userById = userRepository.findById(userId);
    userById.orElseThrow(UserNotFoundException::new);

    Long parentId = request.getParentId();
    Comment parentComment  = null;
    if (parentId != null) {
      parentComment = checkComment(parentId, postId);
    }

    Comment comment = Comment.createComment(
        findPost,
        userById.get(),
        request.getContent(),
        parentComment
    );

    Comment savedPost = commentRepository.save(comment);
    return new CommentResponseWithCreatedAt(savedPost);
  }


  @Transactional(readOnly = true)
  public Page<CommentQueryDto> getPageableComment(final Long postId, final Pageable pageable) {
    postService.checkPost(postId, null);

    Page<CommentQueryDto> originalPage = commentRepository.findAllOriginal(postId, pageable);
    List<CommentQueryDto> parentListForLoop = originalPage.getContent();
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = extractParentIds(parentListForLoop);

      List<CommentQueryDto> children = commentRepository.findAllChildren(postId, parentIds);
      Map<Long, List<CommentQueryDto>> childrenCommentMap = mapByParentId(children);

      setReply(parentListForLoop, childrenCommentMap);

      parentListForLoop = children;
    }

    return originalPage;
  }


  private void setReply(List<CommentQueryDto> parentListForLoop, Map<Long, List<CommentQueryDto>> childrenCommentMap) {
    parentListForLoop.forEach(c -> c.setReply(childrenCommentMap.get(c.getId())));
  }


  private Map<Long, List<CommentQueryDto>> mapByParentId(List<CommentQueryDto> children) {
    return children.stream()
        .collect(Collectors.groupingBy(CommentQueryDto::getParentId));
  }


  private List<Long> extractParentIds(List<CommentQueryDto> parentListForLoop) {
    return parentListForLoop.stream()
        .map(CommentQueryDto::getId)
        .collect(toList());
  }


  public CommentResponseWithModifiedAt updateComment(
      final CommentUpdateRequest request, final Long postId, final Long commentId) {

    postService.checkPost(postId, null);
    Comment findComment = checkComment(commentId, postId);
    checkWriter(findComment);

    findComment.updateComment(request.getContent());

    return new CommentResponseWithModifiedAt(findComment);
  }


  public void deleteComment(final Long postId, final Long commentId) {
    postService.checkPost(postId, null);
    Comment targetComment = checkComment(commentId, postId);
    checkWriter(targetComment);

    if (targetComment.getChildren().isEmpty()) {
      commentRepository.delete(getDeletableAncestorComment(targetComment));
    } else {
      targetComment.changeDeletionStatus();
    }
  }


  private Comment getDeletableAncestorComment(final Comment targetComment) {
    Comment parent = targetComment.getParent();
    if (parent != null && parent.getChildren().size() == 1 && parent.isDeleted()) {
      return getDeletableAncestorComment(parent);
    }
    return targetComment;
  }


  private Comment checkComment(final Long commentId, final Long postId) {
    Optional<Comment> commentById = commentRepository.findByIdAndPostId(commentId, postId);
    commentById.orElseThrow(CommentNotFoundException::new);

    Comment findComment = commentById.get();
    if (findComment.isDeleted()) {
      throw new BadArgumentException("comment has been deleted.");
    }
    return findComment;
  }


  private void checkWriter(final Comment comment) {
    Long currentUserId = SecurityUtil.getCurrentUserId().get();
    if (!SecurityUtil.isAdmin()) {
      if (!currentUserId.equals(comment.getUser().getId())) {
        throw new AccessDeniedException("작성자가 아닙니다.");
      }
    }
  }

}
