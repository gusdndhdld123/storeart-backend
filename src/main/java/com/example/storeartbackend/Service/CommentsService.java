package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.CommentsDTO;
import com.example.storeartbackend.Entity.CommentsEntity;
import com.example.storeartbackend.Repository.CommentsRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final ModelMapper modelMapper;

    // 모든 댓글 조회 (DTO로 변환하여 반환)
    public List<CommentsDTO> getAllComments(Long callcenterIdx) {
        List<CommentsEntity> commentsEntities = commentsRepository.findByCallcenterIdx(callcenterIdx);

        // ModelMapper를 사용하여 엔티티 리스트를 DTO 리스트로 변환
        return commentsEntities.stream()
                .map(entity -> modelMapper.map(entity, CommentsDTO.class)) // 엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    // 댓글 추가
    public CommentsEntity addComment(CommentsDTO commentsDTO) {
        CommentsEntity commentsEntity = toEntity(commentsDTO);  // DTO -> Entity 변환
        return commentsRepository.save(commentsEntity);
    }

    // 댓글 수정
    @Transactional
    public CommentsEntity updateComment(CommentsDTO commentsDTO) {
        //CommentIdx로 검색해서 하나 불러오기
        Optional<CommentsEntity> existingComment = commentsRepository.findByCommentIdx(commentsDTO.getCommentIdx());
//        있으면
        if (existingComment.isPresent()) {
//            새로 하나 생성
            CommentsEntity commentsEntity = existingComment.get();
//            내용 업데이트
            commentsEntity.setComment(commentsDTO.getComment());  // 필요한 필드 업데이트
            commentsEntity.setWriter(commentsDTO.getWriter());
            // 추가적으로 수정해야 할 필드가 있다면 여기에 추가
            return commentsRepository.save(commentsEntity);
        } else {
            log.error("댓글을 찾을 수 없습니다. id: {}", commentsDTO.getCommentIdx());
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentIdx) {
        Optional<CommentsEntity> commentsEntity = commentsRepository.findByCommentIdx(commentIdx);
        if (commentsEntity.isPresent()) {
            commentsRepository.delete(commentsEntity.get());
        } else {
            log.error("댓글을 찾을 수 없습니다. id: {}", commentIdx);
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }
    }

    // DTO -> Entity 변환
    private CommentsEntity toEntity(CommentsDTO commentsDTO) {
        return CommentsEntity.builder()
                .callcenterIdx(commentsDTO.getCallcenterIdx())
                .comment(commentsDTO.getComment())
                .Writer(commentsDTO.getWriter())
                .build();
    }

    // Entity -> DTO 변환 (필요한 경우)
    private CommentsDTO toDTO(CommentsEntity commentsEntity) {
        return CommentsDTO.builder()
                .commentIdx(commentsEntity.getCommentIdx())
                .callcenterIdx(commentsEntity.getCallcenterIdx())
                .comment(commentsEntity.getComment())
                .writer(commentsEntity.getWriter())
                .build();
    }
}
