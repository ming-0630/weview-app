package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.dto.ProductResponseDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.User;
import org.weviewapp.entity.Watchlist;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.repository.WatchlistRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.service.WatchlistService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WatchlistRepository watchlistRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private WatchlistService watchlistService;
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
}
