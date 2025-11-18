package com.karaik.gamebot.roguelike.account;

/**
 * 负责在业务账号与游戏内 UID 之间做映射的服务，可对接更复杂的绑定逻辑。
 */
public interface RoguelikeAccountService {

    /**
     * 根据传入的用户标识解析出真实游戏 UID。
     */
    String resolveUid(String userKey);
}

