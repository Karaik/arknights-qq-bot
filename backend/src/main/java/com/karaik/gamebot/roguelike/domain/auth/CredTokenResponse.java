package com.karaik.gamebot.roguelike.domain.auth;

public record CredTokenResponse(int code, Data data) {
    public record Data(String cred, String token) {}
}
