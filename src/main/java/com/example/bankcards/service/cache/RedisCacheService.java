package com.example.bankcards.service.cache;

public interface RedisCacheService {
    void evictUserCardsCache(Long userId);
    void evictAdminCardsCache();
}
