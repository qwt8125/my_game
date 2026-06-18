package com.paly.legend.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        addBattleActionsColumnIfMissing();
        addAttackSpeedIfMissing();
        addCharacterLocationColumnsIfMissing();
        recalculatePowerWithAttackSpeed();
        createV02TablesIfMissing();
        createV03TablesIfMissing();
        createMailTablesIfMissing();
        createWorldBossTablesIfMissing();
        createMapEventTablesIfMissing();
        createSkillTalentTablesIfMissing();
        addSkillSlotColumnIfMissing();
        addBattleSkillStateColumnIfMissing();
        createGmSystemLogsIfMissing();
        createBattlePreparationsIfMissing();
        createGuildTablesIfMissing();
        createGuildContributionTablesIfMissing();
    }

    private void addBattleActionsColumnIfMissing() {
        List<String> columns = jdbcTemplate.query(
                "PRAGMA table_info(battle_logs)",
                (rs, rowNum) -> rs.getString("name"));
        if (!columns.contains("actions_json")) {
            jdbcTemplate.execute("ALTER TABLE battle_logs ADD COLUMN actions_json TEXT NOT NULL DEFAULT '[]'");
        }
    }

    private void createV02TablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS idle_sessions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL UNIQUE,"
                + "map_id TEXT NOT NULL,"
                + "monster_id TEXT NOT NULL,"
                + "started_at TEXT NOT NULL,"
                + "last_claimed_at TEXT NOT NULL,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS boss_states ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "boss_id TEXT NOT NULL UNIQUE,"
                + "available_at TEXT NOT NULL,"
                + "last_killed_by INTEGER,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (last_killed_by) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS gm_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "admin_account_id INTEGER NOT NULL,"
                + "target_character_id INTEGER NOT NULL,"
                + "action TEXT NOT NULL,"
                + "payload_json TEXT NOT NULL DEFAULT '{}',"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (admin_account_id) REFERENCES accounts(id),"
                + "FOREIGN KEY (target_character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_gm_logs_admin_account_id ON gm_logs(admin_account_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_gm_logs_target_character_id ON gm_logs(target_character_id)");
    }

    private void addAttackSpeedIfMissing() {
        List<String> columns = jdbcTemplate.query(
                "PRAGMA table_info(characters)",
                (rs, rowNum) -> rs.getString("name"));
        if (!columns.contains("attack_speed")) {
            jdbcTemplate.execute("ALTER TABLE characters ADD COLUMN attack_speed INTEGER NOT NULL DEFAULT 100");
        }
    }

    private void addCharacterLocationColumnsIfMissing() {
        addColumnIfMissing("characters", "current_node_id", "TEXT");
        addColumnIfMissing("characters", "last_x", "INTEGER NOT NULL DEFAULT 180");
        addColumnIfMissing("characters", "last_y", "INTEGER NOT NULL DEFAULT 520");
    }

    private void recalculatePowerWithAttackSpeed() {
        jdbcTemplate.execute("UPDATE characters SET power = hp + attack * 8 + defense * 6 + attack_speed * 2");
    }

    private void createV03TablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS battle_sessions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL,"
                + "map_id TEXT NOT NULL,"
                + "monster_id TEXT NOT NULL,"
                + "source_type TEXT NOT NULL DEFAULT 'normal',"
                + "source_id TEXT,"
                + "reward_multiplier INTEGER NOT NULL DEFAULT 1,"
                + "status TEXT NOT NULL,"
                + "round INTEGER NOT NULL DEFAULT 1,"
                + "player_hp INTEGER NOT NULL,"
                + "player_max_hp INTEGER NOT NULL,"
                + "player_attack INTEGER NOT NULL,"
                + "player_defense INTEGER NOT NULL,"
                + "player_attack_speed INTEGER NOT NULL,"
                + "monster_hp INTEGER NOT NULL,"
                + "monster_max_hp INTEGER NOT NULL,"
                + "monster_attack INTEGER NOT NULL,"
                + "monster_defense INTEGER NOT NULL,"
                + "monster_attack_speed INTEGER NOT NULL,"
                + "next_actor TEXT NOT NULL,"
                + "settled INTEGER NOT NULL DEFAULT 0,"
                + "actions_json TEXT NOT NULL DEFAULT '[]',"
                + "result_json TEXT NOT NULL DEFAULT '{}',"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "finished_at TEXT,"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_battle_sessions_character_status ON battle_sessions(character_id, status)");
    }

    private void createMailTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS mails ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL,"
                + "title TEXT NOT NULL,"
                + "content TEXT NOT NULL DEFAULT '',"
                + "attachment_gold INTEGER NOT NULL DEFAULT 0,"
                + "attachment_item_id TEXT,"
                + "attachment_item_type TEXT,"
                + "attachment_quantity INTEGER NOT NULL DEFAULT 0,"
                + "status INTEGER NOT NULL DEFAULT 0,"
                + "read_at TEXT,"
                + "deleted INTEGER NOT NULL DEFAULT 0,"
                + "expires_at TEXT,"
                + "source_type TEXT NOT NULL DEFAULT 'system',"
                + "source_id TEXT,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "claimed_at TEXT,"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_mails_character_status ON mails(character_id, status)");
        addColumnIfMissing("mails", "read_at", "TEXT");
        addColumnIfMissing("mails", "deleted", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("mails", "expires_at", "TEXT");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        List<String> columns = jdbcTemplate.query(
                "PRAGMA table_info(" + tableName + ")",
                (rs, rowNum) -> rs.getString("name"));
        if (!columns.contains(columnName)) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void createWorldBossTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS world_boss_states ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "boss_id TEXT NOT NULL UNIQUE,"
                + "status TEXT NOT NULL DEFAULT 'available',"
                + "current_hp INTEGER NOT NULL,"
                + "max_hp INTEGER NOT NULL,"
                + "available_at TEXT NOT NULL,"
                + "killed_at TEXT,"
                + "rewards_sent INTEGER NOT NULL DEFAULT 0,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS world_boss_damage_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "boss_id TEXT NOT NULL,"
                + "character_id INTEGER NOT NULL,"
                + "damage INTEGER NOT NULL DEFAULT 0,"
                + "battle_id INTEGER,"
                + "rewarded INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_world_boss_damage_boss ON world_boss_damage_logs(boss_id, damage DESC)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_world_boss_damage_character ON world_boss_damage_logs(character_id)");
    }

    private void createMapEventTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS map_event_states ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL,"
                + "event_id TEXT NOT NULL,"
                + "trigger_count INTEGER NOT NULL DEFAULT 0,"
                + "last_triggered_at TEXT,"
                + "next_available_at TEXT,"
                + "completed INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE(character_id, event_id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_map_event_states_character ON map_event_states(character_id)");
    }

    private void addBattleSkillStateColumnIfMissing() {
        addColumnIfMissing("battle_sessions", "skill_state_json", "TEXT NOT NULL DEFAULT '{}'");
    }

    private void addSkillSlotColumnIfMissing() {
        addColumnIfMissing("character_skills", "skill_slot", "INTEGER NOT NULL DEFAULT 0");
    }

    private void createSkillTalentTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS character_skills ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL,"
                + "skill_id TEXT NOT NULL,"
                + "level INTEGER NOT NULL DEFAULT 1,"
                + "skill_slot INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE(character_id, skill_id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_character_skills_character ON character_skills(character_id)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS character_talents ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "character_id INTEGER NOT NULL,"
                + "talent_id TEXT NOT NULL,"
                + "level INTEGER NOT NULL DEFAULT 1,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE(character_id, talent_id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_character_talents_character ON character_talents(character_id)");
    }

    private void createGmSystemLogsIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS gm_system_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "admin_account_id INTEGER NOT NULL,"
                + "action TEXT NOT NULL,"
                + "payload_json TEXT NOT NULL DEFAULT '{}',"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (admin_account_id) REFERENCES accounts(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_gm_system_logs_admin ON gm_system_logs(admin_account_id)");
    }

    private void createBattlePreparationsIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS battle_preparations ("
                + "character_id INTEGER PRIMARY KEY,"
                + "bonus_hp INTEGER NOT NULL DEFAULT 0,"
                + "bonus_attack INTEGER NOT NULL DEFAULT 0,"
                + "bonus_defense INTEGER NOT NULL DEFAULT 0,"
                + "bonus_attack_speed INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
    }

    private void createGuildTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guilds ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL UNIQUE,"
                + "notice TEXT NOT NULL DEFAULT '',"
                + "leader_character_id INTEGER NOT NULL,"
                + "member_count INTEGER NOT NULL DEFAULT 1,"
                + "total_contribution INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (leader_character_id) REFERENCES characters(id))");
        addColumnIfMissing("guilds", "total_contribution", "INTEGER NOT NULL DEFAULT 0");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guilds_leader ON guilds(leader_character_id)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guild_members ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "guild_id INTEGER NOT NULL,"
                + "character_id INTEGER NOT NULL UNIQUE,"
                + "role TEXT NOT NULL DEFAULT 'member',"
                + "contribution INTEGER NOT NULL DEFAULT 0,"
                + "joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (guild_id) REFERENCES guilds(id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_members_guild ON guild_members(guild_id)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guild_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "guild_id INTEGER NOT NULL,"
                + "character_id INTEGER,"
                + "action TEXT NOT NULL,"
                + "payload_json TEXT NOT NULL DEFAULT '{}',"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (guild_id) REFERENCES guilds(id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_logs_guild ON guild_logs(guild_id)");
    }

    private void createGuildContributionTablesIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guild_donations ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "guild_id INTEGER NOT NULL,"
                + "character_id INTEGER NOT NULL,"
                + "donation_id TEXT NOT NULL,"
                + "gold_cost INTEGER NOT NULL DEFAULT 0,"
                + "contribution_gained INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (guild_id) REFERENCES guilds(id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_donations_member_day ON guild_donations(character_id, donation_id, created_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_donations_guild ON guild_donations(guild_id)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guild_shop_purchases ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "guild_id INTEGER NOT NULL,"
                + "character_id INTEGER NOT NULL,"
                + "shop_item_id TEXT NOT NULL,"
                + "item_id TEXT NOT NULL,"
                + "quantity INTEGER NOT NULL DEFAULT 1,"
                + "contribution_cost INTEGER NOT NULL DEFAULT 0,"
                + "gold_cost INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (guild_id) REFERENCES guilds(id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_shop_purchases_member_day ON guild_shop_purchases(character_id, shop_item_id, created_at)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_shop_purchases_guild ON guild_shop_purchases(guild_id)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guild_activity_claims ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "guild_id INTEGER NOT NULL,"
                + "character_id INTEGER NOT NULL,"
                + "activity_id TEXT NOT NULL,"
                + "reward_gold INTEGER NOT NULL DEFAULT 0,"
                + "reward_items_json TEXT NOT NULL DEFAULT '[]',"
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE(guild_id, character_id, activity_id),"
                + "FOREIGN KEY (guild_id) REFERENCES guilds(id),"
                + "FOREIGN KEY (character_id) REFERENCES characters(id))");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_guild_activity_claims_member ON guild_activity_claims(character_id, activity_id)");
    }
}
