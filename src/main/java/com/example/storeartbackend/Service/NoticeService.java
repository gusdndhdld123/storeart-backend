package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.NoticeDTO;
import com.example.storeartbackend.Entity.NoticeEntity;
import com.example.storeartbackend.Repository.NoticeRepository;
import com.example.storeartbackend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ModelMapper modelMapper;

    //등록
    public NoticeDTO register(NoticeDTO noticeDTO) {
        NoticeEntity noticeEntity = modelMapper.map(noticeDTO, NoticeEntity.class);
        NoticeEntity saveEntity = noticeRepository.save(noticeEntity);

        return modelMapper.map(saveEntity, NoticeDTO.class);
    }

    //전체조회
    public List<NoticeDTO> list() {
        List<NoticeEntity> noticeEntityList = noticeRepository.findAll();

        return Arrays.asList(modelMapper.map(noticeEntityList,
                NoticeDTO[].class));
    }

    //개별조회
    public NoticeDTO read(Long noticeIdx) {
        Optional<NoticeEntity> optionalNotice = noticeRepository.findById(noticeIdx);

        return modelMapper.map(optionalNotice, NoticeDTO.class);

    }

    //수정
    public NoticeDTO update(NoticeDTO noticeDTO) {
        Optional<NoticeEntity> optionalNotice =
                noticeRepository.findById(noticeDTO.getNoticeIdx());

        if(optionalNotice.isPresent()) {
            NoticeEntity noticeEntity = modelMapper.map(noticeDTO, NoticeEntity.class);
            noticeRepository.save(noticeEntity);

            return modelMapper.map(noticeEntity, NoticeDTO.class);
        }

        return null;
    }

    //삭제
    public void delete(Long noticeIdx) {
        noticeRepository.deleteById(noticeIdx);
    }
}
