package com.example.storeartbackend.Controller;


import com.example.storeartbackend.DTO.UserDTO;
import com.example.storeartbackend.DTO.UserInfo;
import com.example.storeartbackend.DTO.UserLoginDTO;
import com.example.storeartbackend.Entity.UserEntity;
import com.example.storeartbackend.Repository.UserRepository;
import com.example.storeartbackend.Service.NaverSearchCountService;
import com.example.storeartbackend.Service.SearchcountService;
import com.example.storeartbackend.Service.UserService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.sound.midi.Soundbank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final JwtTokenProvider jwtTokenProvider;
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private final SearchcountService searchcountService;
    @Autowired
    private final NaverSearchCountService naverSearchCountService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;



    private final UserService userService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // Constructor Injection
    public UserController(UserService userService, UserRepository userRepository, ModelMapper modelMapper, JwtTokenProvider jwtTokenProvider, SearchcountService searchcountService, NaverSearchCountService searchCountService, NaverSearchCountService naverSearchCountService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.searchcountService = searchcountService;
        this.naverSearchCountService = naverSearchCountService;

    }

    // 등록
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {

        int id = userService.register(userDTO);
        UserDTO newUser = userService.read(id);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }


    @GetMapping("/list")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = true) String token) {
        // Authorization 헤더가 없거나 잘못된 경우 처리
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class); // userIdx 추출

            // userIdx로 사용자 조회
            UserDTO user = userService.read(userIdx);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }

            // 사용자 등급 확인
            if (!"6".equals(user.getGrade())) { // 관리자가 아닌 경우
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
            }


            // 관리자인 경우 전체 사용자 리스트 반환
            List<UserDTO> userDTOList = userService.userDTOList();
            return ResponseEntity.ok(userDTOList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }



    @GetMapping("/getuser")
    public ResponseEntity<UserDTO> getUserByToken(@RequestHeader(value = "Authorization", required = true) String token) {


        if (token == null || !token.startsWith("Bearer ")) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증 (JwtTokenProvider 사용)
            if (!jwtTokenProvider.validateToken(jwtToken)) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // 토큰이 유효하면, 유저 정보를 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class);  // userIdx를 Integer로 추출



            // userIdx를 사용하여 사용자 정보 조회
            UserDTO user = userService.read(userIdx);

            if (user == null) {
                // 유저를 찾을 수 없는 경우
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // 사용자 정보를 정상적으로 반환
            return ResponseEntity.status(HttpStatus.OK).body(user);

        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 에러 메시지 포함하여 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUserList(
            @RequestHeader("Authorization") String token,
            @RequestBody List<UserDTO> userDTOList) {
        System.out.println(userDTOList + "등급수정할 리스트 옴");
        try {
            // 1. JWT에서 사용자 정보 추출
            String jwt = token.replace("Bearer ", ""); // "Bearer " 부분 제거
            String adminGrade = jwtTokenProvider.extractGrade(jwt); // grade 추출
            System.out.println(adminGrade);
            // 2. grade가 6인지 확인
            if (!"6".equals(adminGrade)) {
                return new ResponseEntity<>("관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
            }

            // 3. 리스트로 유저 정보 수정
            for (UserDTO userDTO : userDTOList) {
                userService.modify(userDTO); // 각 사용자 정보 수정
            }

            return new ResponseEntity<>("사용자 정보가 성공적으로 수정되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            // 예외 처리
            return new ResponseEntity<>("수정 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO loginRequest) {


        // 사용자 정보 조회 후 JWT 토큰 생성
        UserDTO user = userService.loginUser(loginRequest.getUserId(), loginRequest.getUserPw());

        if (user != null) {
            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(user); // userDTO를 기반으로 토큰 생성

            // JWT 토큰을 Map 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("userName", user.getUserName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }







    @PostMapping("/idsearch")
    public ResponseEntity<String> idSearch(@RequestBody String id) {

        // ID를 이용하여 사용자 검색
        UserDTO user = userService.readUserId(id); // userService에서 ID로 사용자 검색하는 로직 작성

        if (user != null) {
            // 기존 ID가 존재하는 경우

            return ResponseEntity.ok("1"); // ID가 존재하면 1을 응답
        } else {
            // ID가 없으면 사용 가능 ID

            return ResponseEntity.ok("2"); // 사용 가능한 ID는 2를 응답
        }
    }
    @PostMapping("/getuserinfo")
    public ResponseEntity<?> getUserInfo(@RequestParam("code") String code) {
        try {

            // 1. 카카오 API와 통신하여 액세스 토큰 요청
            String accessToken = getAccessTokenFromKakao2(code);

            // 2. 액세스 토큰을 사용해 사용자 정보 요청
            Map<String, Object> userInfo = getUserInfoFromKakao(accessToken);
            System.out.println(userInfo);
            System.out.println("accessToken 발급 완료 : " + accessToken);
            // 3. 카카오 이메일과 닉네임 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "카카오 이메일 정보를 가져오지 못했습니다."));
            }
            String kakaoEmail = (String) kakaoAccount.get("email");

            System.out.println("kakaoEmail : " + kakaoEmail);
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String nickname = profile != null ? (String) profile.get("nickname") : "사용자";
            
            // 4. User 테이블에서 이메일로 사용자 검색
            UserDTO existingUser = userService.getUserDTO(kakaoEmail);
            
            if (existingUser != null) {
                System.out.println("이미회원가입해서 취소");
                // 5. 기존 사용자가 있다면 에러 응답 반환
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "이미 회원가입 하셨습니다."));

            }

            // 6. 기존 사용자가 없다면 유저 정보 반환
            Map<String, Object> response = new HashMap<>();
            response.put("result", Map.of(
                    "email", kakaoEmail,
                    "nickname", nickname,
                    "user_info", userInfo
            ));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 예외 발생 시 에러 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다.", "details", e.getMessage()));
        }
    }




    @PostMapping("/signup")
    public ResponseEntity<?> signupOrLogin(@RequestParam("code") String code) {
        System.out.println("받은 code : "+code);
        try {
            // 1. 카카오 API와 통신하여 액세스 토큰 요청
            System.out.println("엑세스 토큰 발급 시도");
            String accessToken = getAccessTokenFromKakao(code);
            System.out.println("엑세스 토큰 발급 성공" + accessToken);

            // 2. 액세스 토큰을 사용해 사용자 정보 요청
            Map<String, Object> userInfo = getUserInfoFromKakao(accessToken);
            System.out.println("사용자 정보 요청 성공" + userInfo.toString());

            // 3. 카카오 이메일 추출
            String kakaoEmail = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
            String name = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("name");
            String phonenumber = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("phone_number");
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String nickname = (String) profile.get("nickname");


            if (kakaoEmail == null || kakaoEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카카오 이메일 정보를 가져오지 못했습니다.");
            }

            // 4. User 테이블에서 이메일로 사용자 검색
            UserDTO existingUser = userService.getUserDTO(kakaoEmail);

            String jwtToken;

            if (existingUser != null) {
                // 5. 기존 사용자가 있다면 JWT 토큰 발급
                jwtToken = jwtTokenProvider.generateToken(existingUser);

            } else {
                // 6. 기존 사용자가 없다면 회원가입 처리
                UserDTO newUserDTO = new UserDTO();
                newUserDTO.setUserEmail(kakaoEmail);
                newUserDTO.setUserName(name);
                newUserDTO.setUserPhone(phonenumber);
                newUserDTO.setGrade("1");
                // UserDTO를 UserEntity로 변환
                UserEntity newUserEntity = modelMapper.map(newUserDTO, UserEntity.class);
                userRepository.save(newUserEntity);

                // 저장된 UserEntity를 다시 조회
                Optional<UserEntity> savedUserEntity = userRepository.findByUserEmail(kakaoEmail);

                if (savedUserEntity.isPresent()) {
                    UserDTO savedDTO = modelMapper.map(savedUserEntity.get(), UserDTO.class);

                    // **Default SearchCount 초기화 호출**
                    int userIdx = savedUserEntity.get().getUserIdx(); // 저장된 UserEntity의 userIdx
                    int grade = 1; // 기본 등급 (예: 1)
                    Integer maxSearch = 3; // 기본 최대 검색 횟수
                    String currentDate = LocalDate.now().toString(); // 오늘 날짜

                    searchcountService.insertDefaultSearchCount(userIdx, grade, maxSearch, currentDate);
                    naverSearchCountService.insertDefaultSearchCount(userIdx, grade, maxSearch, currentDate);
                    // 7. 새 사용자에 대해 JWT 토큰 발급
                    jwtToken = jwtTokenProvider.generateToken(savedDTO);

                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자를 찾을 수 없습니다.");
                }
            }

            // 8. 사용자 정보와 JWT 토큰 반환
            Map<String, Object> response = new HashMap<>();
            UserDTO userDTO1 = userService.getUserDTO(kakaoEmail);

            response.put("result", Map.of(
                    "user_info", userInfo,
                    "jwt", jwtToken,
                    "userDTO",userDTO1
            ));
            System.out.println("jwt랑 userDTO 반환" + jwtToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }




    // 카카오 API와 통신하여 액세스 토큰 요청
    public String getAccessTokenFromKakao(String code) {
        System.out.println("{getAccessTokenFromKakao}get code : " + code);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey); // @Value로 가져온 값을 사용
        System.out.println("client_id : " + kakaoRestApiKey);
        params.add("redirect_uri", "https://위아더스.shop/oauth/kakao"); // @Value로 가져온 값을 사용
//        params.add("redirect_uri", "https://www.위아더스.shop/oauth/userinfo"); // 다른 리다이렉트
        System.out.println("params : " + params);
        System.out.println("redirect_uri : " + "https://위아더스.shop/oauth/kakao");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            System.out.println("access_token");
            System.out.println("responseBody : " + responseBody);
            return (String) responseBody.get("access_token");
        } else {
            System.out.println("Failed to fetch access token from Kakao");

            throw new RuntimeException("Failed to fetch access token from Kakao");

        }
    }

    // 카카오 API와 통신하여 액세스 토큰 요청
    public String getAccessTokenFromKakao2(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey); // @Value로 가져온 값을 사용
        System.out.println("client_id" + kakaoRestApiKey);
        params.add("redirect_uri", "https://위아더스.shop/oauth/userinfo"); // 다른 리다이렉트
        params.add("code", code);
        ;
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();

            return (String) responseBody.get("access_token");
        } else {

            throw new RuntimeException("Failed to fetch access token from Kakao");

        }
    }


    // 액세스 토큰으로 사용자 정보 요청
    private Map<String, Object> getUserInfoFromKakao(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch user info from Kakao");
        }
    }



}