package com.example.storeartbackend.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
@ToString
@Getter
public class BaseEntity {

    @CreatedDate //테이블에 값을 입력할 때 사용
    @Column(name = "regdate" , updatable = false) //updatable false= 업데이트시에 실행 X
    private LocalDateTime regdate;

    @LastModifiedDate //입력할 때마다 (수정할 때마다)
    @Column(name = "moddate")
    private LocalDateTime moddate;

}