package com.karaik.gamebot.roguelike.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karaik.gamebot.roguelike.domain.entity.RoguelikeRunEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class RoguelikeRunRepository {

    private final RoguelikeRunMapper mapper;
    private final ObjectMapper objectMapper;

    public RoguelikeRunRepository(RoguelikeRunMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public void saveRuns(String uid, String themeId, List<Map<String, Object>> runs) {
        if (CollectionUtils.isEmpty(runs)) {
            return;
        }
        for (Map<String, Object> run : runs) {
            String id = (String) run.get("id");
            if (id == null) {
                continue;
            }
            RoguelikeRunEntity entity = new RoguelikeRunEntity();
            entity.setId(id);
            entity.setUid(uid);
            entity.setThemeId(themeId);
            entity.setStartTs(parseLong(run.get("startTs")));
            entity.setRecordJson(toJson(run));

            RoguelikeRunEntity existing = mapper.selectById(id);
            if (existing == null) {
                mapper.insert(entity);
            } else {
                entity.setCreatedAt(existing.getCreatedAt());
                mapper.updateById(entity);
            }
        }
    }

    public List<Map<String, Object>> listRuns(String uid, String themeId) {
        List<RoguelikeRunEntity> entities = mapper.selectList(
                new LambdaQueryWrapper<RoguelikeRunEntity>()
                        .eq(RoguelikeRunEntity::getUid, uid)
                        .eq(RoguelikeRunEntity::getThemeId, themeId)
                        .orderByDesc(RoguelikeRunEntity::getStartTs)
        );
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(entity -> fromJson(entity.getRecordJson()))
                .toList();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize run json", e);
        }
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize run json", e);
        }
    }
}

