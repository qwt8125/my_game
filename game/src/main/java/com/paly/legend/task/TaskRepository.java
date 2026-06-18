package com.paly.legend.task;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TaskProgressRecord> mapper = (rs, rowNum) -> {
        TaskProgressRecord record = new TaskProgressRecord();
        record.setId(rs.getLong("id"));
        record.setCharacterId(rs.getLong("character_id"));
        record.setTaskId(rs.getString("task_id"));
        record.setStatus(rs.getInt("status"));
        record.setProgressJson(rs.getString("progress_json"));
        return record;
    };

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TaskProgressRecord> findByCharacterId(long characterId) {
        return jdbcTemplate.query(
                "SELECT id, character_id, task_id, status, progress_json FROM task_progress WHERE character_id = ?",
                mapper,
                characterId);
    }

    public TaskProgressRecord findByCharacterIdAndTaskId(long characterId, String taskId) {
        List<TaskProgressRecord> records = jdbcTemplate.query(
                "SELECT id, character_id, task_id, status, progress_json FROM task_progress WHERE character_id = ? AND task_id = ?",
                mapper,
                characterId,
                taskId);
        return records.isEmpty() ? null : records.get(0);
    }

    public void insert(long characterId, String taskId, int status, String progressJson) {
        jdbcTemplate.update(
                "INSERT INTO task_progress(character_id, task_id, status, progress_json) VALUES(?, ?, ?, ?)",
                characterId,
                taskId,
                status,
                progressJson);
    }

    public void update(long characterId, String taskId, int status, String progressJson) {
        jdbcTemplate.update(
                "UPDATE task_progress SET status = ?, progress_json = ?, updated_at = CURRENT_TIMESTAMP WHERE character_id = ? AND task_id = ?",
                status,
                progressJson,
                characterId,
                taskId);
    }

    public void save(long characterId, String taskId, int status, String progressJson) {
        if (findByCharacterIdAndTaskId(characterId, taskId) == null) {
            insert(characterId, taskId, status, progressJson);
        } else {
            update(characterId, taskId, status, progressJson);
        }
    }
}
