package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Integer> {
    List<CommentsEntity> findByCallcenterIdx(Long callcenterIdx);
    Optional <CommentsEntity> findByCommentIdx(Long CommentIdx);
}
