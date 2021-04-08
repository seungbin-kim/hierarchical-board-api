package com.personal.board.service;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.*;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.PostJpaRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.repository.query.CommentIdAndPostIdQueryDto;
import com.personal.board.repository.query.CommentQueryRepository;
import com.personal.board.repository.query.PostQueryDto;
import com.personal.board.util.PatchUtil;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

  private final UserRepository userRepository;

  private final PostRepository postRepository;

  private final PostJpaRepository postJpaRepository;

  private final CommentQueryRepository commentQueryRepository;

  private final BoardService boardService;


  public PostResponseWithContentAndCreatedAt addPost(final PostRequest request, final Long boardId) {
    Board findBoard = boardService.checkBoard(boardId);

    Long userId = SecurityUtil.getCurrentUserId().get();
    Optional<User> userById = userRepository.findById(userId);
    userById.orElseThrow(UserNotFoundException::new);

    Long parentId = request.getParentId();
    Post parentPost = null;
    if (parentId != null) {
      parentPost = checkPost(parentId, boardId);
    }

    Post post = Post.createPost(
        findBoard,
        userById.get(),
        request.getTitle(),
        request.getContent(),
        parentPost
    );

    Post savedPost = postRepository.save(post);
    return new PostResponseWithContentAndCreatedAt(savedPost);
  }


//  @Transactional(readOnly = true)
//  public PageQueryDto<PostQueryDto> getPageablePost(final Long boardId, final int size, final int page) {
//    boardService.checkBoard(boardId);
//
//    return postQueryRepository.findPageablePostByDto(boardId, size, page);
//  }


  @Transactional(readOnly = true)
  public Page<PostQueryDto> getPageablePost(final Long boardId, final Pageable pageable) {
    boardService.checkBoard(boardId);

    Page<PostQueryDto> originalPage = postRepository.findAllOriginal(boardId, pageable);
    List<PostQueryDto> parentListForLoop = originalPage.getContent();
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = extractParentId(parentListForLoop);

      List<PostQueryDto> children = postRepository.findAllChildren(boardId, parentIds, pageable);
      Map<Long, List<PostQueryDto>> childrenPostMap = mapByParentID(children);

      Map<Long, Set<Long>> commentIdMap = getCommentIdMapByPostId(parentIds);

      setReplyAndCommentCount(parentListForLoop, childrenPostMap, commentIdMap);

      parentListForLoop = children;
    }

    return originalPage;
  }


  private void setReplyAndCommentCount(List<PostQueryDto> parentListForLoop, Map<Long, List<PostQueryDto>> childrenPostMap, Map<Long, Set<Long>> commentIdMap) {
    parentListForLoop.forEach(p -> {
      p.setReply(childrenPostMap.get(p.getId()));
      Optional<Set<Long>> optionalCommentIdSet = Optional.ofNullable(commentIdMap.get(p.getId()));
      optionalCommentIdSet.ifPresent(s -> p.setCommentCount(s.size()));
    });
  }


  private Map<Long, Set<Long>> getCommentIdMapByPostId(List<Long> parentIds) {
    List<CommentIdAndPostIdQueryDto> commentCountByPostId = commentQueryRepository.findCommentIdByPostId(parentIds);
    return commentCountByPostId.stream()
        .collect(groupingBy(CommentIdAndPostIdQueryDto::getPostId,
            mapping(CommentIdAndPostIdQueryDto::getCommentId,
                toSet())));
  }


  private Map<Long, List<PostQueryDto>> mapByParentID(List<PostQueryDto> children) {
    return children.stream()
        .collect(Collectors.groupingBy(PostQueryDto::getParentId));
  }


  private List<Long> extractParentId(List<PostQueryDto> parentListForLoop) {
    return parentListForLoop.stream()
        .map(PostQueryDto::getId)
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public PostResponseWithContentAndDate getPost(final Long boardId, final Long postId) {
    boardService.checkBoard(boardId);
    Post findPost = checkPost(postId, boardId);
    return new PostResponseWithContentAndDate(findPost);
  }


  public PostResponseWithContentAndModifiedAt updatePost(
      final PostUpdateRequest request, final Long boardId, final Long postId) throws IllegalAccessException {

    boardService.checkBoard(boardId);
    Post findPost = checkPost(postId, boardId);
    checkWriter(findPost);

    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = PatchUtil.validateFields(request, declaredFields); // 입력된 필드 얻기

    findPost.updatePost(validatedFields, request.getTitle(), request.getContent());

    return new PostResponseWithContentAndModifiedAt(findPost);
  }


  public void deletePost(final Long boardId, final Long postId) {
    boardService.checkBoard(boardId);
    Post targetPost = checkPost(postId, boardId);
    checkWriter(targetPost);

    if (targetPost.getChildren().isEmpty()) {
      postRepository.delete(getDeletableAncestorPost(targetPost));
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


  public Post checkPost(final Long postId, final Long boardId) {
    Optional<Post> post = postJpaRepository.findPostByIdAndBoardId(postId, boardId);
    post.orElseThrow(PostNotFoundException::new);

    Post findPost = post.get();
    if (findPost.isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }
    return findPost;
  }


  private void checkWriter(final Post post) {
    Long currentUserId = SecurityUtil.getCurrentUserId().get();
    if (!SecurityUtil.isAdmin()) {
      if (!currentUserId.equals(post.getUser().getId())) {
        throw new AccessDeniedException("작성자가 아닙니다.");
      }
    }
  }

}
