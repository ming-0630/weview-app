package org.weviewapp.service;

import org.weviewapp.entity.Product;

import java.util.UUID;

public interface WatchlistService {
    public String addToWatchlist(UUID userId, UUID productId);
    public boolean getIsWatchlisted(Product product);
}
