package com.karaik.gamebot.roguelike.domain.rogue;

import java.util.Map;

public record RogueInfoResponse(int code, Map<String, Object> data) {}
