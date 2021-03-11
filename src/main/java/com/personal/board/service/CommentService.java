package com.personal.board.service;

import com.personal.board.dto.request.CommentRequest;
import com.personal.board.dto.response.comment.CommentResponseWithCreatedAt;
import com.personal.board.dto.response.post.PostListResponse;
import com.personal.board.entity.Comment;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.exception.NotFoundException;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
      throw new NotFoundException("post id not found.");
    }

    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    if (userById.isEmpty()) {
      throw new NotFoundException("user id not found");
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
        throw new NotFoundException("parent id not found.");
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

}