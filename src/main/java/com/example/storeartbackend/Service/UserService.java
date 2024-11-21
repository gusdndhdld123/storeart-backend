package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.UserDTO;
import com.example.storeartbackend.Entity.UserEntity;
import com.example.storeartbackend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

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


        Optional<UserEntity> userEntity = userRepository
                .findById(userDTO.getUserIdx());

        if (userEntity.isPresent()) {

            String password = passwordEncoder.encode(userDTO.getUserPassword());
            UserEntity user = modelMapper.map(userDTO, UserEntity.class);


            user.setUserPassword(password);


            userRepository.save(user);
        }


    }

    public UserDTO read(int userIdx) {

        Optional<UserEntity> user = userRepository.findById(userIdx);


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



    public void delete(int userIdx) {
        userRepository.deleteById(userIdx);
    }

    public UserDTO loginUser(String userEmail, String password) {

        Optional<UserEntity> existingUserEntity = userRepository.findByUserEmail(userEmail);

        if (existingUserEntity != null) {
            String existingPassword = existingUserEntity.get().getUserPassword();
            if (passwordEncoder.matches(password, existingPassword)) {
                // ModelMapper나 다른 메커니즘을 사용하여 UserEntity를 UserDTO에 맵핑합니다
                UserDTO userDTO = modelMapper.map(existingUserEntity, UserDTO.class);
                return userDTO;
            }
        }

        return null;
    }
}
