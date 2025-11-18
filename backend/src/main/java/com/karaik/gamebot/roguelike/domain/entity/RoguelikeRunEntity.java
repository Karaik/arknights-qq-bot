package com.karaik.gamebot.roguelike.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.karaik.gamebot.common.persistence.AuditingEntity;

@TableName("roguelike_run")
public class RoguelikeRunEntity extends AuditingEntity {

    @TableId
    private String id;

    private String uid;

    private String themeId;

    private Long startTs;

    private String recordJson;

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public Long getStartTs() {
        return startTs;
    }

    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    public String getRecordJson() {
        return recordJson;
    }

    public void setRecordJson(String recordJson) {
        this.recordJson = recordJson;
    }
}
