package com.karaik.gamebot.skland.credential;

public interface SklandTokenStore {
    void saveToken(String userKey, String token);

    String getToken(String userKey);
}

