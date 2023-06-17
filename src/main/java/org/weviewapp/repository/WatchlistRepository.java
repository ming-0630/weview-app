package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.User;
import org.weviewapp.entity.Watchlist;

import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    Page<Watchlist> findByUser(User user, Pageable pageable);
    public Optional<Watchlist> findByUser_IdAndProduct_ProductId(UUID userId, UUID productId);
    public boolean existsByUser_IdAndProduct_ProductId (UUID userId, UUID productId);
}
