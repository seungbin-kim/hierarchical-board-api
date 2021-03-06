package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.request.CommentUpdateRequest;
import com.personal.board.dto.response.comment.CommentListResponse;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.comment.CommentResponseWithModifiedAt;
import com.personal.board.dto.response.post.PostListResponse;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  private final UserRepository userRepository;

  private final PostRepository postRepository;


  public CommentResponseWithCreatedAt addComment(final CommentRequest request, final Long postId) {
    Optional<Post> postById = postRepository.findPostById(postId);
    if (postById.isEmpty()) {
      throw new PostNotFoundException();
    } else if (postById.get().isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }

    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    if (userById.isEmpty()) {
      throw new UserNotFoundException();
    }

    Comment comment = new Comment(
        postById.get(),
        userById.get(),
        request.getContent(),
        null
    );

    if (request.getParentId() != null) {
      // 답 댓글인 경우
      // 답 댓글인데 부모글 번호가 없는경우(잘못된 요청)
      Optional<Comment> commentById = commentRepository.findCommentById(request.getParentId());
      if (commentById.isEmpty()) {
        throw new ParentNotFoundException();
      }

      Comment parentComment = commentById.get();

      // 답 댓글인데 부모글이 지워진 경우(답글 불가)
      if (parentComment.isDeleted()) {
        throw new BadArgumentException("parent comment has been deleted.");
      }
      
      // 부모글 번호가 정상적으로 있는 경우
      comment.setParent(parentComment);
    }
    Comment savedPost = commentRepository.save(comment);
    return new CommentResponseWithCreatedAt(savedPost);
  }

  public List<CommentListResponse> getAllComment(final Long postId) {
    Optional<Post> postById = postRepository.findPostById(postId);
    if (postById.isEmpty()) {
      throw new PostNotFoundException();
    } else if (postById.get().isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }
    // 답변형 출력을 위한 DTO변환
    return commentRepository.findAllComment(postId)
        .stream()
        .filter(comment -> comment.getParent() == null)
        .map(CommentListResponse::new)
        .collect(Collectors.toList());
  }

  public CommentResponseWithModifiedAt updateComment(
      final CommentUpdateRequest request, final Long postId, final Long commentId) {
    Comment findComment = checkPostAndComment(postId, commentId);

    if (request.getContent() != null) {
      if (StringUtils.isBlank(request.getContent())) {
        throw new BadArgumentException("content is blank.");
      }
      findComment.changeContent(request.getContent());
      findComment.setModifiedAt(LocalDateTime.now());
    }

    return new CommentResponseWithModifiedAt(findComment);
  }

  public void deleteComment(final Long postId, final Long commentsId) {
    Comment findComment = checkPostAndComment(postId, commentsId);
    commentRepository.deleteComment(findComment);
  }

  private Comment checkPostAndComment(final Long postId, final Long commentsId) {
    Optional<Post> postById = postRepository.findPostById(postId);
    if (postById.isEmpty()) {
      throw new PostNotFoundException();
    } else if (postById.get().isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }

    Optional<Comment> commentById = commentRepository.findCommentById(commentsId);
    if (commentById.isEmpty()) {
      throw new CommentNotFoundException();
    }

    Comment findComment = commentById.get();
    if (findComment.isDeleted()) {
      throw new BadArgumentException("comment has been deleted.");
    }
    return findComment;
  }

}
