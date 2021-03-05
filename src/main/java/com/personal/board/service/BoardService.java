package com.personal.board.service;

import com.personal.board.dto.request.BoardRequest;
import com.personal.board.dto.request.PostRequest;
import com.personal.board.dto.response.board.BoardResponseWithCreatedAt;
import com.personal.board.dto.response.board.BoardResponseWithDate;
import com.personal.board.dto.response.post.PostResponseWithCreatedAt;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.exception.NameDuplicatedException;
import com.personal.board.exception.NotFoundException;
import com.personal.board.repository.BoardRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public BoardResponseWithCreatedAt addBoard(final BoardRequest request) {
    if (boardRepository.checkBoardName(request.getName())) {
      throw new NameDuplicatedException();
    }

    Board board = new Board(
        request.getName());

    Board savedBoard = boardRepository.save(board);

    return new BoardResponseWithCreatedAt(savedBoard);
  }

  public List<BoardResponseWithDate> getAllBoard() {
    return boardRepository.findAllBoard()
        .stream()
        .map(BoardResponseWithDate::new)
        .collect(Collectors.toList());
  }

  public BoardResponseWithDate getBoard(final Long id) {
    Optional<Board> boardById = boardRepository.findBoardById(id);
    if (boardById.isEmpty()) {
      throw new NotFoundException("board id not found.");
    }
    return new BoardResponseWithDate(boardById.get());
  }

  public PostResponseWithCreatedAt addPost(final PostRequest request, final Long id) {
    Optional<Board> boardById = boardRepository.findBoardById(id);
    if (boardById.isEmpty()) {
      throw new NotFoundException("board id not found.");
    }
    Optional<User> userById = userRepository.findUserById(request.getWriterId());
    if (userById.isEmpty()) {
      throw new NotFoundException("user id not found.");
    }

    Post post = new Post(
        boardById.get(),
        userById.get(),
        request.getTitle(),
        request.getContent(),
        null,
        0,
        0
    );

    if (request.getParentId() == null) { // 원글인 경우
      post.setGroup(post);
    } else { // 답글인 경우
      // 답글인데 부모글 번호가 없는경우(잘못된 요청)
      Optional<Post> postById = postRepository.findPostById(request.getParentId());
      if (postById.isEmpty()) {
        throw new NotFoundException("parent id not found.");
      }
      // 부모글 번호가 정상적으로 있는경우
      Post parentPost = postById.get();
      Post group = parentPost.getGroup();
      post.setGroup(group);
      int groupOrder = parentPost.getGroupOrder();
      postRepository.updateGroupOrder(group, groupOrder);
      post.setGroupOrder(groupOrder + 1);
      post.setGroupDepth(parentPost.getGroupDepth() + 1);
    }
    postRepository.save(post);
    return new PostResponseWithCreatedAt(post);
  }

  public List<PostResponseWithCreatedAt> getAllPost() {
    return postRepository.findAllPost()
        .stream()
        .map(PostResponseWithCreatedAt::new)
        .collect(Collectors.toList());
  }
}
