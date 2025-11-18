package com.karaik.gamebot.roguelike.account;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoguelikeAccountServiceImpl implements RoguelikeAccountService {

    private final Map<String, String> bindings = new ConcurrentHashMap<>();

    public void bind(String userKey, String uid) {
        bindings.put(userKey, uid);
    }

    @Override
    public String resolveUid(String userKey) {
        return bindings.getOrDefault(userKey, userKey);
    }
}
