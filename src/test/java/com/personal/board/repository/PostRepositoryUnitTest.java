package com.personal.board.repository;

import com.personal.board.dto.query.PostQueryDto;
import com.personal.board.entity.Authority;
import com.personal.board.entity.Board;
import com.personal.board.entity.Post;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class PostRepositoryUnitTest {

  @Autowired
  PostRepository postRepository;

  @Autowired
  BoardRepository boardRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  EntityManager em;

  @BeforeEach
  public void init() {
    em.createNativeQuery("ALTER SEQUENCE user_seq RESTART WITH 1").executeUpdate();
  }

  String email = "test@test.com";
  String name = "testName";
  String nickname = "testNickname";
  LocalDate birthday = LocalDate.parse("1997-05-28");
  String password = "1234";
  Authority authority = new Authority(Role.ROLE_USER);

  @Test
  @DisplayName("원글페이징")
  void findAllOriginal() throws Exception {
    //given
    int number = 10;
    Board board = new Board("testBoard");
    boardRepository.save(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);

    List<Post> postList = createPostList(number, user, board);
    postRepository.saveAll(postList);

    int numberOfReply = 3;
    List<Post> replyList = createReplyList(numberOfReply, postList, user, board);
    postRepository.saveAll(replyList);

    List<Post> replyListDeep = createReplyList(numberOfReply, replyList, user, board);
    postRepository.saveAll(replyListDeep);

    int requestSize = 5;
    PageRequest pageRequest = PageRequest.of(0, requestSize);

    //when
    Page<PostQueryDto> original = postRepository.findAllOriginal(board.getId(), pageRequest);

    //then
    assertThat(original.getTotalElements()).isEqualTo(number);
    assertThat(original.getSize()).isEqualTo(requestSize);
  }

  @Test
  @DisplayName("부모글들의답글조회")
  void findAllChildren() throws Exception {
    //given
    int number = 10;
    Board board = new Board("testBoard");
    boardRepository.save(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);

    List<Post> postList = createPostList(number, user, board);
    postRepository.saveAll(postList);

    int numberOfReply = 3;
    List<Post> replyList = createReplyList(numberOfReply, postList, user, board);
    postRepository.saveAll(replyList);

    List<Long> parentIds = postList.stream()
        .map(Post::getId)
        .collect(Collectors.toList());

    //when
    List<PostQueryDto> children = postRepository.findAllChildren(board.getId(), parentIds);

    //then
    assertThat(children.size()).isEqualTo(number * numberOfReply);
  }

  private List<Post> createReplyList(int number, List<Post> parent, User user, Board board) {
    List<Post> list = new ArrayList<>();
    for (Post post : parent) {
      for (int i = 0; i < number; i++) {
        Post reply = Post.createPost(
            board,
            user,
            post.getId() + "testReply" + i,
            post.getId() + "testReplyContent" + i,
            post);
        list.add(reply);
      }
    }
    return list;
  }
  
  private List<Post> createPostList(int number, User user, Board board) {
    List<Post> list = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      Post post = Post.createPost(
          board,
          user,
          "testTitle" + i,
          "testContent" + i,
          null
      );
      list.add(post);
    }
    return list;
  }
  
  @Test
  @DisplayName("게시글찾기")
  void findPostByIdAndBoardId() throws Exception {
    //given
    int number = 10;
    Board board = new Board("testBoard");
    boardRepository.save(board);

    User user = User.createUser(
        email,
        nickname,
        name,
        birthday,
        password,
        authority
    );
    userRepository.save(user);

    List<Post> postList = createPostList(number, user, board);
    postRepository.saveAll(postList);

    for (Post post : postList) {
      System.out.println(post.getId());
    }

    //when
    Optional<Post> post1 = postRepository.findPostByIdAndBoardId(1L, board.getId());
    Optional<Post> post2 = postRepository.findPostByIdAndBoardId(1L, null);

    //then
//    assertThat(post1.get()).isEqualTo(post2.get());
  }

}