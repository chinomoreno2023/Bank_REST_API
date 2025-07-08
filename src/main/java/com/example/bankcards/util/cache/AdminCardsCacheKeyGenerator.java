package com.example.bankcards.util.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
public class AdminCardsCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Pageable pageable = (Pageable) params[0];

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(pageable.getPageNumber());
        keyBuilder.append("-");
        keyBuilder.append(pageable.getPageSize());

        return keyBuilder.toString();
    }
}
