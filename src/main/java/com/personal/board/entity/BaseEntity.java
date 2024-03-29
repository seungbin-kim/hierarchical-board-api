package com.personal.board.entity;

import lombok.Getter;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 생성일, 수정일 기본 엔티티
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime modifiedAt = LocalDateTime.now();

  public void setModifiedAt(final LocalDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

}
