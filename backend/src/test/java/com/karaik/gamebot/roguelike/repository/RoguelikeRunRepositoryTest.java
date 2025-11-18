package com.karaik.gamebot.roguelike.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RoguelikeRunRepositoryTest {

    @Autowired
    private RoguelikeRunRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void clean() {
        jdbcTemplate.update("DELETE FROM roguelike_run");
    }

    @Test
    void shouldPersistAndRetrieveRuns() {
        Map<String, Object> run = Map.of(
                "id", "test-run",
                "score", 123,
                "startTs", 1700000000L
        );

        repository.saveRuns("uid123", "rogue_4", List.of(run));

        List<Map<String, Object>> stored = repository.listRuns("uid123", "rogue_4");
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).get("score")).isEqualTo(123);
    }
}
