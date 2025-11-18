package com.karaik.gamebot.roguelike.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.karaik.gamebot.roguelike.domain.entity.RoguelikeRunEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoguelikeRunMapper extends BaseMapper<RoguelikeRunEntity> {
}

