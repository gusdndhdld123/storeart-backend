package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.UserDTO;
import com.example.storeartbackend.Entity.SearchCountEntity;
import com.example.storeartbackend.Entity.UserEntity;
import com.example.storeartbackend.Repository.UserRepository;
import com.example.storeartbackend.Util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor

public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey; // application.properties에서 설정한 카카오 REST API 키



    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

     // 리디렉션 URI
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Autowired
    private SearchcountService searchcountService;
    @Autowired
    private NaverSearchCountService naverSearchCountService;


    public String getKakaoAccessToken(String code) {
        // 카카오에 토큰 요청을 위한 요청 파라미터 설정
        String url = KAKAO_TOKEN_URL;
        RestTemplate restTemplate = new RestTemplate();

        // 요청 본문 구성
        String body = "grant_type=authorization_code" +
                "&client_id=" + kakaoApiKey +  // 카카오 REST API 키
                "&redirect_uri=" + "https://위아더스.shop/oauth/kakao" +
                "&code=" + code;

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 객체 생성
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // 카카오 API에 POST 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // 응답 결과
        return response.getBody();  // 응답 본문은 JSON 형태로 액세스 토큰을 포함
    }


    public String getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 헤더에 액세스 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 카카오 사용자 정보 요청
        ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity, String.class);

        return response.getBody();  // 사용자 정보 JSON 반환
    }

    public int register(UserDTO userDTO) {


        Optional<UserEntity> userEntity = userRepository
                .findByUserEmail(userDTO.getUserEmail());

        if (userEntity.isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        String password = passwordEncoder.encode(userDTO.getUserPassword());

        UserEntity user = modelMapper.map(userDTO, UserEntity.class);

        user.setUserPassword(password);

        userRepository.save(user);

        return userRepository.save(user).getUserIdx();
    }

    public void modify(UserDTO userDTO) {

        Optional<UserEntity> userEntityOptional = userRepository.findById(userDTO.getUserIdx());

        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();

            // 비밀번호가 null인 경우 기존 비밀번호를 유지
            String password = (userDTO.getUserPassword() != null)
                    ? passwordEncoder.encode(userDTO.getUserPassword())
                    : userEntity.getUserPassword();

            // UserDTO를 UserEntity로 매핑
            UserEntity updatedUserEntity = modelMapper.map(userDTO, UserEntity.class);

            // 기존 비밀번호 또는 새로 암호화된 비밀번호 설정
            updatedUserEntity.setUserPassword(password);
            int userIdx = updatedUserEntity.getUserIdx();
            LocalDate date = LocalDate.now();
            if ("1".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade1(userIdx, date);
                naverSearchCountService.handlegrade1(userIdx, date);
            }
            if ("2".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade2(userIdx, date);
                naverSearchCountService.handlegrade2(userIdx, date);
            }
            if ("3".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade3(userIdx, date);
                naverSearchCountService.handlegrade3(userIdx, date);
            }
            if ("4".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade4(userIdx, date);
                naverSearchCountService.handlegrade4(userIdx, date);
            }
            if ("5".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade5(userIdx, date);
                naverSearchCountService.handlegrade5(userIdx, date);
            }
            if ("6".equals(updatedUserEntity.getGrade())) {
                searchcountService.handlegrade6(userIdx, date);
                naverSearchCountService.handlegrade6(userIdx, date);
            }
            // 저장
            userRepository.save(updatedUserEntity);
        }
    }


    public UserDTO read(int userIdx) {

        Optional<UserEntity> user = userRepository.findById(userIdx);


        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO readUserId(String userId) {
        Optional<UserEntity> user = userRepository.findByUserId(userId);
        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> userDTOList() {

        List<UserEntity> users = userRepository.findAll();

        // Stream API를 사용해 Entity 리스트를 DTO 리스트로 변환
        List<UserDTO> userDTOList = users.stream()
                .map(data -> modelMapper.map(data, UserDTO.class))
                .toList(); // Java 16 이상에서는 .toList() 사용 가능

        return userDTOList;
    }
    public UserDTO getUserDTO(String userEmail) {
        Optional<UserEntity> userEntityOptional = userRepository.findByUserEmail(userEmail);

        if (userEntityOptional.isPresent()) {
            return modelMapper.map(userEntityOptional.get(), UserDTO.class);
        } else {
            // 사용자 정보를 찾지 못한 경우 처리 (예: null 반환 또는 예외 처리)
            return null;
        }
    }



    public void delete(int userIdx) {
        userRepository.deleteById(userIdx);
    }

    public UserDTO loginUser(String userId, String password) {
        Optional<UserEntity> existingUserEntity = userRepository.findByUserId(userId);
        if (existingUserEntity.isPresent()) {
            String existingPassword = existingUserEntity.get().getUserPassword();
            if (passwordEncoder.matches(password, existingPassword)) {
                UserDTO userDTO = new UserDTO();
                // UserDTO에 데이터를 설정
                userDTO.setUserIdx(existingUserEntity.get().getUserIdx());
                userDTO.setUserId(existingUserEntity.get().getUserId());
                userDTO.setUserEmail(existingUserEntity.get().getUserEmail());
                userDTO.setUserName(existingUserEntity.get().getUserName());
                userDTO.setUserPhone(existingUserEntity.get().getUserPhone());
                userDTO.setOrganization(existingUserEntity.get().getOrganization());
                userDTO.setUseYn(existingUserEntity.get().getUseYn());
                userDTO.setMktEmail(existingUserEntity.get().getMktEmail());
                userDTO.setMktSms(existingUserEntity.get().getMktSms());
                userDTO.setMktAdr(existingUserEntity.get().getMktAdr());

                // JWT 토큰 생성
                String token = jwtTokenProvider.generateToken(userDTO);
                return userDTO;
            }
        }
        return null;
    }

    public List<UserDTO> getUserDTOList() {
        List<UserEntity> users = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (UserEntity userEntity : users) {
            userDTOList.add(modelMapper.map(userEntity, UserDTO.class));
        }
        return userDTOList;
    }
}
