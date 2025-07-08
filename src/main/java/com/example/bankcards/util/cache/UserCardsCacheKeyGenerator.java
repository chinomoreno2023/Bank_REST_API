package com.example.bankcards.util.cache;

import com.example.bankcards.util.auth.ContextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class UserCardsCacheKeyGenerator implements KeyGenerator {
    private final ContextExtractor contextExtractor;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String numberFilter = (String) params[0];
        Pageable pageable = (Pageable) params[1];
        Long userId = contextExtractor.getUserFromContext().getId();

        StringBuilder keyBuilder = new StringBuilder();

        keyBuilder.append("{").append(userId).append("}");
        keyBuilder.append("::");

        if (numberFilter != null && !numberFilter.isBlank()) {
            keyBuilder.append(numberFilter);
        } else {
            keyBuilder.append("all");
        }
        keyBuilder.append("::");
        keyBuilder.append(pageable.getPageNumber());
        keyBuilder.append("-");
        keyBuilder.append(pageable.getPageSize());

        return keyBuilder.toString();
    }
}