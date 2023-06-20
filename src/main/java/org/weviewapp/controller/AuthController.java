package org.weviewapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.*;
import org.weviewapp.dto.JWTRefreshRequest;
import org.weviewapp.dto.LoginDTO;
import org.weviewapp.dto.RegisterDTO;
import org.weviewapp.entity.RefreshToken;
import org.weviewapp.entity.Role;
import org.weviewapp.entity.User;
import org.weviewapp.exception.RefreshTokenException;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.RoleRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.security.JwtTokenProvider;
import org.weviewapp.service.AuthService;
import org.weviewapp.service.RefreshTokenService;
import org.weviewapp.utils.ImageUtil;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<JWTAuthResponse> login(@RequestBody LoginDTO loginDto){
            String token = authService.login(loginDto);
            JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> loggedInUser = userRepository.findByEmail(userEmail);

            if (loggedInUser.isPresent()) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(loggedInUser.get().getId());
                userDTO.setUsername(loggedInUser.get().getUsername());

                if(!loggedInUser.get().getProfileImageDirectory().equals("")) {
                    try{
                        byte[] userImage = ImageUtil.loadImage(loggedInUser.get().getProfileImageDirectory());
                        userDTO.setUserImage(userImage);
                    } catch (Exception e) {
                        throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }
                }

                jwtAuthResponse.setUser(userDTO);
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerDto){
        System.out.println(registerDto);

        // add check for username exists in a DB
        if(userRepository.existsByUsername(registerDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // add check for email exists in DB
        if(userRepository.existsByEmail(registerDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role roles = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Collections.singleton(roles));

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }
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
