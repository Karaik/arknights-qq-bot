package com.karaik.gamebot.roguelike.domain.binding;

import java.util.List;

public record BindingResponse(int code, Data data) {
    public record Data(List<Game> list) {}
    public record Game(String appCode, List<Binding> bindingList) {}
    public record Binding(String uid) {}
}
