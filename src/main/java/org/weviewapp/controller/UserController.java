package org.weviewapp.controller;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.dto.ProductResponseDTO;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.User;
import org.weviewapp.entity.Watchlist;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.repository.WatchlistRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.service.UserService;
import org.weviewapp.service.WatchlistService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private WatchlistRepository watchlistRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private WatchlistService watchlistService;

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (user.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "User not found! Please login again to continue");
            }
            UserDTO userDTO = userService.mapUserToDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } else {
            throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "User not authorized! Please login again to continue");
        }
    }
    @GetMapping("/watchlist")
    public ResponseEntity<?> getWatchlist(
            @RequestParam String userId,
            @RequestParam Integer pageNum,
            @RequestParam (defaultValue = "name") String sortBy,
            @RequestParam (defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Optional<User> user = userRepository.findById(UUID.fromString(userId));

        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "User does not exist");
        }

        Page<Watchlist> watchlist;
        Pageable pageable = PageRequest.of(pageNum - 1, 5, sortDirection, sortBy);
        watchlist = watchlistRepository.findByUser(user.get(), pageable);

        List<Product> products = watchlist.stream()
                .map(Watchlist::getProduct)
                .collect(Collectors.toList());

        ProductResponseDTO dto = new ProductResponseDTO();
        List<ProductDTO> result = productService.mapToPreviewDTO(products);

        dto.setProductDTOs(result);
        dto.setTotalPages(watchlist.getTotalPages());
        dto.setCurrentPage(watchlist.getPageable().getPageNumber());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/addToWatchlist")
    public ResponseEntity<?> addToWatchlist(
            @RequestParam String userId,
            @RequestParam String productId) {
        try {
            String response = watchlistService.addToWatchlist(UUID.fromString(userId), UUID.fromString(productId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add product to watchlist.");
        }
    }
    @PostMapping("/updateProfilePicture")
    public ResponseEntity<?> updateProfilePicture(
            @ModelAttribute UserDTO userDTO) {
        if (userDTO.getUploadedImage().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No uploaded images!");
        }
        User updatedUser = userService.uploadUserImage(userDTO.getUploadedImage());
        UserDTO newUserDTO = userService.mapUserToDTO(updatedUser);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("user", newUserDTO);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getPoints")
    public ResponseEntity<?> getPoints(@RequestParam String userId) {

        Optional<User> user = userRepository.findById(UUID.fromString(userId));

        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "User does not exist");
        }

        return new ResponseEntity<>(user.get().getPoints(), HttpStatus.OK);
    }

    @GetMapping(value = "/generateOTP")
    public ResponseEntity<String> generateOTP(@RequestParam String phoneNumber){
        User user = userService.getCurrentUser();
        if (user.getIsVerified()) {
            return new ResponseEntity<>("This user is already verified!", HttpStatus.OK);
        }

        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
        Verification verification = Verification.creator(
                        "VAb24b0931494348d7e59eef1e55c03f10", // this is your verification sid
                        phoneNumber, //this is your Twilio verified recipient phone number
                        "whatsapp") // this is your channel type
                .create();

        System.out.println(verification.getStatus());

//        log.info("OTP has been successfully generated, and awaits your verification {}", LocalDateTime.now());

        return new ResponseEntity<>("Your OTP has been sent to your verified phone number", HttpStatus.OK);
    }

    @GetMapping("/verifyOTP")
    public ResponseEntity<?> verifyUserOTP(
            @RequestParam String phoneNumber,
            @RequestParam String code
    ) throws Exception {
        User user = userService.getCurrentUser();
        if (user.getIsVerified()) {
            return new ResponseEntity<>("This user is already verified!", HttpStatus.OK);
        }
        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
        try {

            VerificationCheck verificationCheck = VerificationCheck.creator(
                            "VAb24b0931494348d7e59eef1e55c03f10")
                    .setTo(phoneNumber)
                    .setCode(code)
                    .create();

            System.out.println(verificationCheck.getStatus());
            if (verificationCheck.getValid()) {
                userService.verifyUser();
                return new ResponseEntity<>("This user's verification has been completed successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Wrong OTP!", HttpStatus.OK);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Verification failed.", HttpStatus.BAD_REQUEST);
        }
    }
}
