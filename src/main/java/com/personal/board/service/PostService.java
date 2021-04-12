package com.personal.board.service;

import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.request.PostUpdateRequest;
import com.personal.board.dto.response.post.*;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.dto.query.CommentIdAndPostIdQueryDto;
import com.personal.board.dto.query.PostQueryDto;
import com.personal.board.util.PatchUtil;
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  private final CommentRepository commentRepository;

  private final BoardService boardService;

  private final UserService userService;

  private final PatchUtil patchUtil;


  /**
   * 게시글 등록
   *
   * @param request 등록 게시글 정보
   * @param boardId 등록할 게시판 id
   * @return 등록된 게시글
   */
  @Transactional
  public PostResponseWithContentAndCreatedAt addPost(final PostRequest request,
                                                     final Long boardId) {

    Board board = boardService.findBoard(boardId);

    Long userId = SecurityUtil.getCurrentUserId().get();
    User user = userService.findUser(userId);

    Post post = createPost(request, board, user);
    Post savedPost = postRepository.save(post);
    return new PostResponseWithContentAndCreatedAt(savedPost);
  }


  /**
   * 게시글 삭제
   *
   * @param boardId 삭제할 게시글이 속한 게시판 id
   * @param postId  삭제할 게시글 id
   */
  @Transactional
  public void deletePost(final Long boardId,
                         final Long postId) {

    boardService.findBoard(boardId);

    Post targetPost = findPost(postId, boardId);
    SecurityUtil.checkAdminAndSameUser(targetPost.getUser().getId());

    checkAndDeletePost(targetPost);
  }


  /**
   * 게시글 단건조회
   *
   * @param boardId 조회할 게시글이 속한 게시판 id
   * @param postId  게시글 id
   * @return 조회된 게시글
   */
  public PostResponseWithContentAndDate getPost(final Long boardId,
                                                final Long postId) {

    boardService.findBoard(boardId);
    Post findPost = findPost(postId, boardId);
    return new PostResponseWithContentAndDate(findPost);
  }


  /**
   * 게시글목록 페이징 조회
   *
   * @param boardId  조회할 게시판 id
   * @param pageable 페이징 정보
   * @return 페이징된 게시글목록
   */
  public Page<PostQueryDto> getPageablePosts(final Long boardId,
                                             final Pageable pageable) {

    boardService.findBoard(boardId);
    return findPostPage(boardId, pageable);
  }


  /**
   * 게시글 업데이트
   *
   * @param request 업데이트 정보
   * @param boardId 업데이틀할 게시글이 속한 게시판 id
   * @param postId  업데이트할 게시글 id
   * @return 업데이트된 게시글
   * @throws IllegalAccessException 필드 접근 불가시 발생
   */
  @Transactional
  public PostResponseWithContentAndModifiedAt updatePost(final PostUpdateRequest request,
                                                         final Long boardId,
                                                         final Long postId) throws IllegalAccessException {

    boardService.findBoard(boardId);

    Post findPost = findPost(postId, boardId);
    SecurityUtil.checkAdminAndSameUser(findPost.getUser().getId());

    ArrayList<String> validatedFields = patchUtil.getValidatedFields(request);
    findPost.updatePost(validatedFields, request.getTitle(), request.getContent());

    return new PostResponseWithContentAndModifiedAt(findPost);
  }


  /**
   * 게시글 조회
   *
   * @param postId  조회할 게시글 id
   * @param boardId 조회할 게시글이 속한 게시판 id
   * @return 조회된 게시글
   */
  Post findPost(final Long postId,
                final Long boardId) {

    Optional<Post> post = postRepository.findPostByIdAndBoardId(postId, boardId);
    post.orElseThrow(PostNotFoundException::new);

    Post findPost = post.get();
    if (findPost.isDeleted()) {
      throw new BadArgumentException("post has been deleted.");
    }
    return findPost;
  }


  /**
   * 삭제 가능한 게시글 확인 및 삭제
   *
   * @param targetPost 삭제할 게시글
   */
  private void checkAndDeletePost(final Post targetPost) {

    if (targetPost.getChildren().isEmpty()) {
      postRepository.delete(findDeletableAncestorPost(targetPost));
    } else {
      targetPost.changeDeletionStatus();
    }
  }


  /**
   * 지울 수 있는 조상글 찾기
   *
   * @param targetPost 지울 게시글
   * @return 지울 수 있는 조상글
   */
  private Post findDeletableAncestorPost(final Post targetPost) {

    Post parent = targetPost.getParent();
    if (parent != null && parent.getChildren().size() == 1 && parent.isDeleted()) {
      return findDeletableAncestorPost(parent);
    }
    return targetPost;
  }


  /**
   * 게시글 엔티티 생성
   *
   * @param request 생성할 게시글 정보
   * @param board   생성할 게시글의 게시판 엔티티
   * @param user    등록 유저의 엔티티
   * @return 생성된 게시글 엔티티
   */
  private Post createPost(final PostRequest request,
                          final Board board,
                          final User user) {

    Post parentPost = findParentPost(request.getParentId(), board.getId());

    return Post.createPost(
        board,
        user,
        request.getTitle(),
        request.getContent(),
        parentPost
    );
  }


  /**
   * 부모글 찾기
   *
   * @param parentId 부모글의 id
   * @param boardId  부모글이 속한 게시판 id
   * @return 부모글 엔티티 또는 null(원글)
   */
  private Post findParentPost(final Long parentId,
                              final Long boardId) {

    Post parentPost = null;
    if (parentId != null) {
      parentPost = findPost(parentId, boardId);
    }
    return parentPost;
  }


  /**
   * 답변형 게시글 페이징하여 찾기
   *
   * @param boardId  조회할 게시판 id
   * @param pageable 페이징 정보
   * @return 페이징된 게시글 목록
   */
  private Page<PostQueryDto> findPostPage(final Long boardId,
                                          final Pageable pageable) {

    Page<PostQueryDto> originalPage = postRepository.findAllOriginal(boardId, pageable);
    List<PostQueryDto> parentListForLoop = originalPage.getContent();
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = extractPostIds(parentListForLoop);

      List<PostQueryDto> children = postRepository.findAllChildren(boardId, parentIds);
      Map<Long, List<PostQueryDto>> childrenPostMap = mapByParentId(children);

      Map<Long, Set<Long>> commentIdMap = findCommentIdMappedByPostId(parentIds);

      setReplyAndCommentCount(parentListForLoop, childrenPostMap, commentIdMap);

      parentListForLoop = children;
    }
    return originalPage;
  }


  /**
   * 답글 설정 및 댓글 수 설정
   *
   * @param parentList      부모글 리스트
   * @param childrenPostMap 부모글 id로 분류된 답글들의 맵
   * @param commentIdMap    부모글 id에 달린 댓글들의 id 맵
   */
  private void setReplyAndCommentCount(final List<PostQueryDto> parentList,
                                       final Map<Long, List<PostQueryDto>> childrenPostMap,
                                       final Map<Long, Set<Long>> commentIdMap) {

    parentList
        .forEach(p -> {
          p.setReply(childrenPostMap.get(p.getId()));
          Optional<Set<Long>> optionalCommentIdSet = Optional.ofNullable(commentIdMap.get(p.getId()));
          optionalCommentIdSet.ifPresent(s -> p.setCommentCount(s.size()));
        });
  }


  /**
   * 각 게시글마다 달린 댓글들의 id 찾기
   *
   * @param postIds 게시글 id 리스트
   * @return 게시글 id에 달린 댓글들의 id 맵
   */
  private Map<Long, Set<Long>> findCommentIdMappedByPostId(final List<Long> postIds) {

    List<CommentIdAndPostIdQueryDto> commentCountByPostId = commentRepository.findCommentIdByPostId(postIds);
    return commentCountByPostId.stream()
        .collect(groupingBy(CommentIdAndPostIdQueryDto::getPostId,
            mapping(CommentIdAndPostIdQueryDto::getCommentId,
                toSet())));
  }


  /**
   * 각 게시글의 부모글 id 를 키로하여 답글 분류
   *
   * @param children 부모글의 id로 분류할 답글리스트
   * @return 부모글의 id로 분류된 답글들의 맵
   */
  private Map<Long, List<PostQueryDto>> mapByParentId(final List<PostQueryDto> children) {

    return children.stream()
        .collect(groupingBy(PostQueryDto::getParentId));
  }


  /**
   * 게시글 id 리스트 추출
   *
   * @param postList id를 추출할 게시글 리스트
   * @return 추출된 id 리스트
   */
  private List<Long> extractPostIds(final List<PostQueryDto> postList) {

    return postList.stream()
        .map(PostQueryDto::getId)
        .collect(toList());
  }

}
