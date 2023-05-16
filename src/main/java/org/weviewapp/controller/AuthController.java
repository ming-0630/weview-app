package org.weviewapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.JWTAuthResponse;
import org.weviewapp.dto.JWTRefreshRequest;
import org.weviewapp.dto.JWTRefreshResponse;
import org.weviewapp.dto.LoginDto;
import org.weviewapp.entity.RefreshToken;
import org.weviewapp.entity.User;
import org.weviewapp.exception.RefreshTokenException;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.security.JwtTokenProvider;
import org.weviewapp.service.AuthService;
import org.weviewapp.service.RefreshTokenService;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<JWTAuthResponse> login(@RequestBody LoginDto loginDto){
            String token = authService.login(loginDto);
            JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> loggedInUser = userRepository.findByEmail(userEmail);

            if (loggedInUser.isPresent()) {
                jwtAuthResponse.setUser(loggedInUser.get());
                String refreshToken = refreshTokenService.createRefreshToken(loggedInUser.get().getId()).getToken();
                jwtAuthResponse.setRefreshToken(refreshToken);
            }
        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody JWTRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateToken(user.getUsername());
                    return ResponseEntity.ok(new JWTRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RefreshTokenException("Refresh token is not in database!"));
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser(@RequestBody RegisterDto signUpDto){
//
//        // add check for username exists in a DB
//        if(userRepository.existsByUsername(signUpDto.getUsername())){
//            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
//        }
//
//        // add check for email exists in DB
//        if(userRepository.existsByEmail(signUpDto.getEmail())){
//            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
//        }
//
//        // create user object
//        User user = new User();
//        user.setUsername(signUpDto.getUsername());
//        user.setEmail(signUpDto.getEmail());
//        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
//
//        Role roles = roleRepository.findByName("ROLE_USER").get();
//        user.setRoles(Collections.singleton(roles));
//
//        userRepository.save(user);
//
//        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
//
//    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Logout ===> " + auth);
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        System.out.println("Logout ===> " + auth);
        return new ResponseEntity<>("Logout successfully", HttpStatus.OK);
    }
}
