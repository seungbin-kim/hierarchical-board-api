package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.response.PageDto;
import com.personal.board.dto.response.comment.CommentDto;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.repository.query.CommentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

  private final UserRepository userRepository;

  private final PostRepository postRepository;

  private final CommentRepository commentRepository;

  private final CommentQueryRepository commentQueryRepository;


  public CommentResponseWithCreatedAt addComment(final CommentRequest request, final Long postId) {
    Post findPost = checkPost(postId);

    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    userById.orElseThrow(UserNotFoundException::new);

    Comment comment = Comment.createComment(
        findPost,
        userById.get(),
        request.getContent(),
        null
    );

    if (request.getParentId() != null) {
      // 답 댓글인데 부모글 번호가 없거나 지워진 경우 예외발생(잘못된 요청)
      Comment parentComment = checkComment(request.getParentId());
      // 부모글 번호가 정상적으로 있는 경우
      comment.changeParent(parentComment);
    }
    Comment savedPost = commentRepository.save(comment);
    return new CommentResponseWithCreatedAt(savedPost);
  }


  @Transactional(readOnly = true)
  public PageDto<CommentDto> getPageableComment(final Long postId, final int size, final int page) {
    checkPost(postId);

    return commentQueryRepository.findPageableCommentByDto(postId, size, page);
  }


  public CommentResponseWithModifiedAt updateComment(
      final CommentUpdateRequest request, final Long postId, final Long commentId) {

    checkPost(postId);
    Comment findComment = checkComment(commentId);

    findComment.updateComment(request.getContent());

    return new CommentResponseWithModifiedAt(findComment);
  }


  public void deleteComment(final Long postId, final Long commentsId) {
    checkPost(postId);
    Comment targetComment = checkComment(commentsId);

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


  private Comment checkComment(final Long commentsId) {
    Optional<Comment> commentById = commentRepository.findCommentById(commentsId);
    commentById.orElseThrow(CommentNotFoundException::new);

    Comment findComment = commentById.get();
    if (findComment.isDeleted()) {
      throw new BadArgumentException("comment has been deleted.");
    }
    return findComment;
  }


  private Post checkPost(final Long postId) {
    Optional<Post> postById = postRepository.findPostById(postId);
    postById.orElseThrow(PostNotFoundException::new);

    Post findPost = postById.get();
    if (findPost.isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }
    return findPost;
  }

}
