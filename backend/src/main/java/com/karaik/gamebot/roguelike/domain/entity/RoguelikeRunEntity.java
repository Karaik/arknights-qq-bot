package com.karaik.gamebot.roguelike.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.karaik.gamebot.common.persistence.AuditingEntity;
import lombok.Data;

@Data
@TableName("roguelike_run")
public class RoguelikeRunEntity extends AuditingEntity {

    @TableId
    private String id;

    private String uid;

    private String themeId;

    private Long startTs;

    private String recordJson;
}
