package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.UserDTO;
import com.example.storeartbackend.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 등록
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        int id = userService.register(userDTO);
        UserDTO newUser = userService.read(id);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

//    // 전체 조회
//    @GetMapping
//    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
//        return new ResponseEntity<>(userService.list(pageable), HttpStatus.OK);
//    }

    // 개별 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") int id) {
        UserDTO user = userService.read(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable int id) {
        userDTO.setUserIdx(id);
        userService.modify(userDTO);
        UserDTO updatedUser = userService.read(id);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO request) {
        UserDTO user = userService.loginUser(request.getUserEmail(), request.getUserPassword());
        return user != null ?
                new ResponseEntity<>(user, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}