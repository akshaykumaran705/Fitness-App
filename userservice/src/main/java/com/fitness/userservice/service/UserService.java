package com.fitness.userservice.service;

import com.fitness.userservice.User;
import com.fitness.userservice.UserserviceApplication;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Transactional
    public UserResponse findOrCreateUser(String fireBaseUid,String email){
        return userRepository.findByFireBaseUid(fireBaseUid)
                .map(this::mapToUserResponse)
                .orElseGet(()->{
                    User newUser = new User();
                    newUser.setFireBaseUid(fireBaseUid);
                    newUser.setEmail(email);
                    User savedUser = userRepository.save(newUser);
                    return mapToUserResponse(savedUser);
                });
    }
    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }
    private UserResponse mapToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
