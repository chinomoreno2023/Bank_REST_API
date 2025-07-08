package com.example.bankcards.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static com.example.bankcards.config.cache.RedisConfig.ADMIN_CARDS_CACHE;
import static com.example.bankcards.config.cache.RedisConfig.PREFIX_CACHE_NAME;
import static com.example.bankcards.config.cache.RedisConfig.USER_CARDS_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements RedisCacheService {
    private final RedisTemplate<String, ?> redisTemplate;

    public void evictUserCardsCache(Long userId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                StringBuilder keyBuilder = new StringBuilder();
                keyBuilder.append(PREFIX_CACHE_NAME);
                keyBuilder.append(USER_CARDS_CACHE);
                keyBuilder.append("::{").append(userId).append("}*");

                String keyPattern = keyBuilder.toString();

                redisTemplate.execute((RedisCallback<Void>) connection -> {
                    ScanOptions options = ScanOptions.scanOptions().match(keyPattern).count(1000).build();

                    try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                        while (cursor.hasNext()) {
                            connection.keyCommands().del(cursor.stream().toArray(byte[][]::new));
                        }
                    }
                    return null;
                });
            }
        });
    }

    public void evictAdminCardsCache() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                StringBuilder keyBuilder = new StringBuilder();
                keyBuilder.append(PREFIX_CACHE_NAME);
                keyBuilder.append(ADMIN_CARDS_CACHE);
                keyBuilder.append("::*");

                String keyPattern = keyBuilder.toString();

                redisTemplate.execute((RedisCallback<Void>) connection -> {
                    ScanOptions options = ScanOptions.scanOptions().match(keyPattern).count(1000).build();

                    try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                        while (cursor.hasNext()) {
                            connection.keyCommands().del(cursor.stream().toArray(byte[][]::new));
                        }
                    }
                    return null;
                });
            }
        });
    }
}
