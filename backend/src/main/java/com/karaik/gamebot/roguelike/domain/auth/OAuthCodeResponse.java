package com.karaik.gamebot.roguelike.domain.auth;

public record OAuthCodeResponse(int status, OAuthCodeResponse.Data data) {
    public record Data(String code) {}
}
