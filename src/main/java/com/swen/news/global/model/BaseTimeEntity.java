package com.swen.news.global.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 엔티티의 생성 시간과 수정 시간을 자동으로 관리하는 추상 기본 클래스입니다.
 *
 * <p>JPA Auditing 기능을 활용하여, 엔티티가 생성될 때 createdDate를 자동으로 설정하고, 엔티티가 수정될 때 updatedDate를 자동으로 갱신합니다.
 *
 * <p>이 클래스를 상속받는 엔티티들은 생성 및 수정 시간을 자동으로 기록합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;
}
