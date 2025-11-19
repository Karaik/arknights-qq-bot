package com.karaik.gamebot.roguelike.service;

import com.karaik.gamebot.roguelike.domain.dto.RoguelikeAnalysisResult;
import com.karaik.gamebot.roguelike.domain.dto.RoguelikeThemeSummary;

import java.util.List;

/**
 * 应用层入口，聚合账号绑定、官方数据拉取与主题分析。
 */
public interface RoguelikeApplicationService {

    /**
     * 查询所有可用主题摘要信息。
     *
     * @return 主题概要列表
     */
    List<RoguelikeThemeSummary> listThemes();

    /**
     * 返回用户所有主题的分析概览。
     *
     * @param userKey 业务用户标识
     * @return 分析结果集合
     */
    List<RoguelikeAnalysisResult> listAnalyses(String userKey);

    /**
     * 拉取或读取缓存并返回主题分析结果。
     *
     * @param userKey 业务用户标识
     * @param themeIdOrName 主题 ID 或名称
     * @param refresh 是否强制刷新外部数据
     * @return 分析结果，为空则表示尚无历史记录
     */
    RoguelikeAnalysisResult getAnalysis(String userKey, String themeIdOrName, boolean refresh);

    /**
     * 强制拉取官方最新记录，落库并返回分析结果。
     *
     * @param userKey 业务用户标识
     * @param themeIdOrName 主题 ID 或名称（为空则自动识别当前主题）
     * @return 最新分析结果
     */
    RoguelikeAnalysisResult refreshAndAnalyze(String userKey, String themeIdOrName);
}
