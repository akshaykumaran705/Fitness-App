package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.TestUserRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile(@AuthenticationPrincipal Jwt jwt){
        String fireBaseUid = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        UserResponse userProfile = userService.findOrCreateUser(fireBaseUid,email);
        return ResponseEntity.ok(userProfile);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
//    @PostMapping("/register")
//    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request ){
//       return ResponseEntity.ok(userService.register(request));
//    }
@PostMapping("/me/test")
public ResponseEntity<UserResponse> testFindOrCreateUser(@RequestBody TestUserRequest request) {
    UserResponse userProfile = userService.findOrCreateUser(request.getFirebaseUid(), request.getEmail());
    return ResponseEntity.ok(userProfile);
}
}
