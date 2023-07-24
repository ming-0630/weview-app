package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.UserService;
import org.weviewapp.utils.ImageUtil;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Override
    public User uploadUserImage(MultipartFile file) {
        User user = getCurrentUser();
        String newImgDir = ImageUtil.uploadImage(file, ImageCategory.PROFILE_IMG);
        ImageUtil.deleteImage(user.getProfileImageDirectory());
        user.setProfileImageDirectory(newImgDir);
        return userRepository.save(user);
    }

    @Override
    public User modifyPoints(UUID userId, Integer points) {
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "User not found! Please login again to continue");
            }
            user.get().setPoints(user.get().getPoints() + points);
            return userRepository.save(user.get());
    }

    @Override
    public UserDTO mapUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setIsVerified(user.getIsVerified());
        userDTO.setPoints(user.getPoints());
        userDTO.setRole(user.getRoles().stream().map(x -> x.getName()).toList());

        if(!user.getProfileImageDirectory().equals("")) {
            try{
                byte[] userImage = ImageUtil.loadImage(user.getProfileImageDirectory());
                userDTO.setUserImage(userImage);
            } catch (Exception e) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
        return userDTO;
    }

    @Override
    public User verifyUser(String phoneNum) {
        User user = getCurrentUser();
        user.setIsVerified(true);
        user.setPhoneNumber(phoneNum);
        return userRepository.save(user);
    }
    @Override
    public Boolean phoneNumExist(String phoneNum) {
        return userRepository.existsByPhoneNumber(phoneNum);
    }
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (user.isEmpty()) {
                return null;
            }
            return user.get();
        } else {
            throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "User not authorized! Please login again to continue");
        }
    }

    @Override
    public User getMLUser() {
        Optional<User> user = userRepository.findByEmail("ML");

        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "Unable to find ML User!");
        } else {
            return user.get();
        }
    }
}
