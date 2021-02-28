package com.personal.board.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

  private LocalDateTime createdAt;

  private LocalDateTime modifiedAt;

}
