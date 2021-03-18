package com.personal.board.service;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.*;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.BoardRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.repository.query.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

  private final BoardRepository boardRepository;

  private final UserRepository userRepository;

  private final PostRepository postRepository;

  private final PostQueryRepository postQueryRepository;


  public PostResponseWithContentAndCreatedAt addPost(final PostRequest request, final Long boardId) {
    Board findBoard = checkBoard(boardId);

    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    userById.orElseThrow(UserNotFoundException::new);

    Post post = Post.createPost(
        findBoard,
        userById.get(),
        request.getTitle(),
        request.getContent(),
        null
    );

    if (request.getParentId() != null) {
      // 답글인데 부모글 번호가 없거나 지워진 경우 예외발생(잘못된 요청)
      Post parentPost = checkPost(request.getParentId());
      // 부모글 번호가 정상적으로 있는경우 부모글 세팅
      post.changeParent(parentPost);
    }
    Post savedPost = postRepository.save(post);
    return new PostResponseWithContentAndCreatedAt(savedPost);
  }


  @Transactional(readOnly = true)
  public List<PostDto> getAllPost(final Long boardId) {
    // 게시판을 찾지 못할시 예외발생
    checkBoard(boardId);

    // 답변형 출력
    return postQueryRepository.findAllPostByDto(boardId);
  }


  @Transactional(readOnly = true)
  public PostResponseWithContentAndDate getPost(final Long boardId, final Long postId) {
    checkBoard(boardId);
    Post findPost = checkPost(postId);
    return new PostResponseWithContentAndDate(findPost);
  }


  public PostResponseWithContentAndModifiedAt updatePost(
      final PostUpdateRequest request, final Long boardId, final Long postId) throws IllegalAccessException {

    checkBoard(boardId);
    Post findPost = checkPost(postId);

    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = PatchUtil.validateFields(request, declaredFields); // 입력된 필드 얻기

    findPost.updatePost(validatedFields, request.getTitle(), request.getContent());

    return new PostResponseWithContentAndModifiedAt(findPost);
  }


  public void deletePost(final Long boardId, final Long postId) {
    checkBoard(boardId);
    Post targetPost = checkPost(postId);

    if (targetPost.getChildren().isEmpty()) {
      postRepository.deletePost(getDeletableAncestorPost(targetPost));
    } else {
      targetPost.changeDeletionStatus();
    }
  }

  private Post getDeletableAncestorPost(final Post targetPost) {
    Post parent = targetPost.getParent();
    if (parent != null && parent.getChildren().size() == 1 && parent.isDeleted()) {
      return getDeletableAncestorPost(parent);
    }
    return targetPost;
  }


  private Board checkBoard(final Long boardId) {
    Optional<Board> boardById = boardRepository.findBoardById(boardId);
    boardById.orElseThrow(BoardNotFoundException::new);
    return boardById.get();
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
