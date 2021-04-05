package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.response.PageQueryDto;
import com.personal.board.repository.query.CommentQueryDto;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.repository.query.CommentQueryRepository;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

  private final UserRepository userRepository;

  private final CommentRepository commentRepository;

  private final CommentQueryRepository commentQueryRepository;

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
  public PageQueryDto<CommentQueryDto> getPageableComment(final Long postId, final int size, final int page) {
    postService.checkPost(postId, null);

    return commentQueryRepository.findPageableCommentByDto(postId, size, page);
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
      commentRepository.deleteComment(getDeletableAncestorComment(targetComment));
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
    Optional<Comment> commentById = commentRepository.findCommentByIdAndPostId(commentId, postId);
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
