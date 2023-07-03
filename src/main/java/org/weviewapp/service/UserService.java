package org.weviewapp.service;

import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.User;

import java.util.UUID;

public interface UserService {
    User uploadUserImage(MultipartFile file);
    User modifyPoints(UUID userId, Integer points);
    User verifyUser();
    Boolean phoneNumExist(String phoneNum);
    User getCurrentUser();
    UserDTO mapUserToDTO(User user);
}
