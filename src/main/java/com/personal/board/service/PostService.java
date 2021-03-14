package com.personal.board.service;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.PostListResponse;
import com.personal.board.dto.response.post.PostResponseWithContentAndCreatedAt;
import com.personal.board.dto.response.post.PostResponseWithContentAndDate;
import com.personal.board.dto.response.post.PostResponseWithContentAndModifiedAt;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.BoardRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

  private final BoardRepository boardRepository;

  private final PostRepository postRepository;

  private final UserRepository userRepository;


  public PostResponseWithContentAndCreatedAt addPost(final PostRequest request, final Long boardId) {
    Optional<Board> boardById = boardRepository.findBoardById(boardId);
    boardById.orElseThrow(BoardNotFoundException::new);

    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    userById.orElseThrow(UserNotFoundException::new);

    Post post = new Post(
        boardById.get(),
        userById.get(),
        request.getTitle(),
        request.getContent(),
        null
    );

    if (request.getParentId() != null) {
      // 답글인 경우
      // 답글인데 부모글 번호가 없는경우(잘못된 요청)
      Optional<Post> postById = postRepository.findPostById(request.getParentId());
      postById.orElseThrow(ParentNotFoundException::new);

      Post parentPost = postById.get();

      // 답글인데 부모글이 지워진 경우(답글 불가)
      if (parentPost.isDeleted()) {
        throw new BadArgumentException("parent post has been deleted.");
      }

      // 부모글 번호가 정상적으로 있는경우
      post.setParent(parentPost);
    }
    Post savedPost = postRepository.save(post);
    return new PostResponseWithContentAndCreatedAt(savedPost);
  }


  @Transactional(readOnly = true)
  public List<PostListResponse> getAllPost(final Long boardId) {
    // 게시판을 찾지 못할시
    boardRepository.findBoardById(boardId).orElseThrow(BoardNotFoundException::new);

    // 답변형 출력을위해 부모글이 null인것만 골라서 DTO로 변환
    return postRepository.findAllPost(boardId)
        .stream()
        .map(PostListResponse::new)
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public PostResponseWithContentAndDate getPost(final Long boardId, final Long postId) {
    Post findPost = checkBoardAndPost(boardId, postId);
    return new PostResponseWithContentAndDate(findPost);
  }


  public PostResponseWithContentAndModifiedAt updatePost(
      final PostUpdateRequest request, final Long boardId, final Long postId) throws IllegalAccessException {
    Post findPost = checkBoardAndPost(boardId, postId);

    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = PatchUtil.validateFields(request, declaredFields); // PATCH를 위한 입력필드얻기

    for (String validatedField : validatedFields) { // 입력이 확인된 필드를 변경감지로 데이터 변경
      switch (validatedField) {
        case "title":
          findPost.changeTitle(request.getTitle());
          break;
        case "content":
          findPost.changeContent(request.getContent());
          break;
      }
    }
    findPost.setModifiedAt(LocalDateTime.now()); // 수정시간
    return new PostResponseWithContentAndModifiedAt(findPost);
  }


  public void deletePost(final Long boardId, final Long postId) {
    Post findPost = checkBoardAndPost(boardId, postId);
    postRepository.deletePost(findPost);
  }


  private Post checkBoardAndPost(final Long boardId, final Long postId) {
    boardRepository.findBoardById(boardId).orElseThrow(BoardNotFoundException::new);

    Optional<Post> postById = postRepository.findPostById(postId);
    postById.orElseThrow(PostNotFoundException::new);

    Post findPost = postById.get();
    if (findPost.isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }
    return findPost;
  }

}
