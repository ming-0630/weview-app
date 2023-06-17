package org.weviewapp.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.User;
import org.weviewapp.entity.Watchlist;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.repository.WatchlistRepository;
import org.weviewapp.service.WatchlistService;

import java.util.Optional;
import java.util.UUID;

@Service
public class WatchlistServiceImpl implements WatchlistService {
    @Autowired
    WatchlistRepository watchlistRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public String addToWatchlist(UUID userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        Optional<Watchlist> existingEntry = watchlistRepository.findByUser_IdAndProduct_ProductId(userId, productId);
        if (existingEntry.isPresent()) {
            watchlistRepository.delete(existingEntry.get());
            return "Deleted from watchlist!";
        } else {
            Watchlist watchlist = new Watchlist();
            watchlist.setId(UUID.randomUUID());
            watchlist.setProduct(product);
            watchlist.setUser(user);
            watchlistRepository.save(watchlist);
            return "Added to watchlist!";
        }
    }
    public boolean getIsWatchlisted(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (!user.isEmpty()) {
                Boolean isWatchlisted = watchlistRepository.existsByUser_IdAndProduct_ProductId(
                        user.get().getId(),
                        product.getProductId()
                );
                return isWatchlisted;
            }
            return false;
        }
        return false;
    }
}
