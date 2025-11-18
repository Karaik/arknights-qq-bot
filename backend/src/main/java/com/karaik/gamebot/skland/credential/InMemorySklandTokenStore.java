package com.karaik.gamebot.skland.credential;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySklandTokenStore implements SklandTokenStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void saveToken(String userKey, String token) {
        if (StringUtils.hasText(userKey) && StringUtils.hasText(token)) {
            store.put(userKey, token);
        }
    }

    @Override
    public String getToken(String userKey) {
        return store.get(userKey);
    }
}

