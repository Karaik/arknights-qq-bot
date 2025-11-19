package com.karaik.gamebot.roguelike.account;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单内存实现，开发阶段用于维护 userKey 与 UID 的映射关系。
 * 生产环境可替换为数据库或缓存实现。
 */
@Service
public class RoguelikeAccountServiceImpl implements RoguelikeAccountService {

    private final Map<String, String> bindings = new ConcurrentHashMap<>();

    @Override
    public void bind(String userKey, String uid) {
        bindings.put(userKey, uid);
    }

    @Override
    public String resolveUid(String userKey) {
        return bindings.getOrDefault(userKey, userKey);
    }
}
