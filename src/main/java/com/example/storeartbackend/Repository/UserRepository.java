package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Integer> {
    Optional<UserEntity> findByUserEmail(String userEmail);
    // 전체 조회
    List<UserEntity> findAll();
    Optional<UserEntity> findByUserId(String userId);

}
