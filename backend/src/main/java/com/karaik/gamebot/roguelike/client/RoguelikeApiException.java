package com.karaik.gamebot.roguelike.client;

public class RoguelikeApiException extends RuntimeException {
    public RoguelikeApiException(String message) {
        super(message);
    }

    public RoguelikeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
