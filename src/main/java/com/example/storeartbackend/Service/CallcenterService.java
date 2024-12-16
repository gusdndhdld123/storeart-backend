package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.CallcenterDTO;
import com.example.storeartbackend.Entity.CallcenterEntity;
import com.example.storeartbackend.Entity.UserEntity;
import com.example.storeartbackend.Repository.CallcenterRepository;
import com.example.storeartbackend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallcenterService {
    private final CallcenterRepository callcenterRepository;
    private final ModelMapper modelMapper;

    private final UserRepository userRepository;

    //등록
    public CallcenterDTO register(CallcenterDTO callcenterDTO) {
        CallcenterEntity callcenterEntity = modelMapper.map(callcenterDTO, CallcenterEntity.class);
        callcenterEntity.setComplete("N");
        CallcenterEntity saveEntity = callcenterRepository.save(callcenterEntity);

        return modelMapper.map(saveEntity, CallcenterDTO.class);
    }
    //전체조회
    public List<CallcenterDTO> list() {
        List<CallcenterEntity> callcenterEntityList = callcenterRepository.findAll();

        return callcenterEntityList.stream()
                .map(callcenterEntity -> {
                    // CallcenterEntity -> CallcenterDTO 변환
                    CallcenterDTO callcenterDTO = modelMapper.map(callcenterEntity, CallcenterDTO.class);
                    System.out.println(callcenterDTO.toString());
                    System.out.println(callcenterEntity.getUserIdx());
                    // userIdx로 작성자 정보 조회
                    Optional<UserEntity> userEntityOptional = userRepository.findByUserIdx(callcenterEntity.getUserIdx());

                    // 작성자 정보가 존재하면, 이름을 가져와서 writer 필드에 설정
                    String writer = userEntityOptional.map(UserEntity::getUserName)
                            .orElse("Unknown"); // 작성자가 없을 경우 "Unknown"으로 설정

                    callcenterDTO.setWriter(writer); // 작성자 정보 설정

                    return callcenterDTO;
                })
                .collect(Collectors.toList());
    }
    //내것만 조회
    public List<CallcenterDTO> list1(int userIdx) {
        List<CallcenterEntity> callcenterEntityList = callcenterRepository.findByUserIdx(userIdx);

        return callcenterEntityList.stream()
                .map(callcenterEntity -> {
                    // CallcenterEntity -> CallcenterDTO 변환
                    CallcenterDTO callcenterDTO = modelMapper.map(callcenterEntity, CallcenterDTO.class);
                    System.out.println(callcenterDTO.toString());
                    System.out.println(callcenterEntity.getUserIdx());
                    // userIdx로 작성자 정보 조회
                    Optional<UserEntity> userEntityOptional = userRepository.findByUserIdx(callcenterEntity.getUserIdx());

                    // 작성자 정보가 존재하면, 이름을 가져와서 writer 필드에 설정
                    String writer = userEntityOptional.map(UserEntity::getUserName)
                            .orElse("Unknown"); // 작성자가 없을 경우 "Unknown"으로 설정

                    callcenterDTO.setWriter(writer); // 작성자 정보 설정

                    return callcenterDTO;
                })
                .collect(Collectors.toList());
    }

    //개별조회
    public CallcenterDTO read(Long idx) {
        System.out.println("받은 Idx = " + idx);
        Optional<CallcenterEntity> optionalBoard = callcenterRepository.findById(idx);

        CallcenterDTO callcenterDTO = modelMapper.map(optionalBoard, CallcenterDTO.class);

        // userIdx로 작성자 정보 조회
        Optional<UserEntity> userEntityOptional = userRepository.findByUserIdx(callcenterDTO.getUserIdx());

        // 작성자 정보가 존재하면, 이름을 가져와서 writer 필드에 설정
        String writer = userEntityOptional.map(UserEntity::getUserName)
                .orElse("Unknown"); // 작성자가 없을 경우 "Unknown"으로 설정

        callcenterDTO.setWriter(writer); // 작성자 정보 설정

        return callcenterDTO;
    }

    //수정
    public CallcenterDTO update(CallcenterDTO callcenterDTO) {
        Optional<CallcenterEntity> optionalBoard =
                callcenterRepository.findById(callcenterDTO.getCallcenterIdx());

        if(optionalBoard.isPresent()) {
            CallcenterEntity callcenterEntity = modelMapper.map(callcenterDTO, CallcenterEntity.class);
            callcenterRepository.save(callcenterEntity);

            return modelMapper.map(callcenterEntity, CallcenterDTO.class);
        }

        return null;
    }

    //삭제
    public void delete(Long boardIdx) {
        callcenterRepository.deleteById(boardIdx);
    }
}
