package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    // 추가로 필요한 쿼리 메서드가 있다면 정의할 수 있습니다.
}