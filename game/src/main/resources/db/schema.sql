PRAGMA journal_mode = WAL;
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auth_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL,
    revoked INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_auth_tokens_account_id ON auth_tokens(account_id);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_token ON auth_tokens(token);

CREATE TABLE IF NOT EXISTS characters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL UNIQUE,
    nickname TEXT NOT NULL UNIQUE,
    class_name TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    exp INTEGER NOT NULL DEFAULT 0,
    gold INTEGER NOT NULL DEFAULT 100,
    hp INTEGER NOT NULL DEFAULT 120,
    attack INTEGER NOT NULL DEFAULT 12,
    defense INTEGER NOT NULL DEFAULT 4,
    attack_speed INTEGER NOT NULL DEFAULT 100,
    power INTEGER NOT NULL DEFAULT 440,
    current_map_id TEXT NOT NULL DEFAULT 'map_novice_field',
    current_node_id TEXT,
    last_x INTEGER NOT NULL DEFAULT 180,
    last_y INTEGER NOT NULL DEFAULT 520,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_characters_level ON characters(level DESC);
CREATE INDEX IF NOT EXISTS idx_characters_power ON characters(power DESC);
CREATE INDEX IF NOT EXISTS idx_characters_gold ON characters(gold DESC);

CREATE TABLE IF NOT EXISTS inventory_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    item_id TEXT NOT NULL,
    item_type TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    bind_status INTEGER NOT NULL DEFAULT 0,
    extra_json TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_character_id ON inventory_items(character_id);

CREATE TABLE IF NOT EXISTS equipped_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    slot TEXT NOT NULL,
    inventory_item_id INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, slot),
    UNIQUE(inventory_item_id),
    FOREIGN KEY (character_id) REFERENCES characters(id),
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

CREATE TABLE IF NOT EXISTS task_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    task_id TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    progress_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, task_id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE TABLE IF NOT EXISTS battle_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    map_id TEXT NOT NULL,
    monster_id TEXT NOT NULL,
    win INTEGER NOT NULL,
    rounds INTEGER NOT NULL,
    exp_gained INTEGER NOT NULL DEFAULT 0,
    gold_gained INTEGER NOT NULL DEFAULT 0,
    actions_json TEXT NOT NULL DEFAULT '[]',
    drops_json TEXT NOT NULL DEFAULT '[]',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_battle_logs_character_id ON battle_logs(character_id);

CREATE TABLE IF NOT EXISTS battle_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    map_id TEXT NOT NULL,
    monster_id TEXT NOT NULL,
    source_type TEXT NOT NULL DEFAULT 'normal',
    source_id TEXT,
    reward_multiplier INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL,
    round INTEGER NOT NULL DEFAULT 1,
    player_hp INTEGER NOT NULL,
    player_max_hp INTEGER NOT NULL,
    player_attack INTEGER NOT NULL,
    player_defense INTEGER NOT NULL,
    player_attack_speed INTEGER NOT NULL,
    monster_hp INTEGER NOT NULL,
    monster_max_hp INTEGER NOT NULL,
    monster_attack INTEGER NOT NULL,
    monster_defense INTEGER NOT NULL,
    monster_attack_speed INTEGER NOT NULL,
    next_actor TEXT NOT NULL,
    settled INTEGER NOT NULL DEFAULT 0,
    actions_json TEXT NOT NULL DEFAULT '[]',
    result_json TEXT NOT NULL DEFAULT '{}',
    skill_state_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TEXT,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_battle_sessions_character_status ON battle_sessions(character_id, status);

CREATE TABLE IF NOT EXISTS currency_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    currency_type TEXT NOT NULL,
    change_amount INTEGER NOT NULL,
    before_amount INTEGER NOT NULL,
    after_amount INTEGER NOT NULL,
    reason TEXT NOT NULL,
    ref_id TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_currency_logs_character_id ON currency_logs(character_id);

CREATE TABLE IF NOT EXISTS drop_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    source_type TEXT NOT NULL,
    source_id TEXT NOT NULL,
    item_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_drop_logs_character_id ON drop_logs(character_id);

CREATE TABLE IF NOT EXISTS idle_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL UNIQUE,
    map_id TEXT NOT NULL,
    monster_id TEXT NOT NULL,
    started_at TEXT NOT NULL,
    last_claimed_at TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE TABLE IF NOT EXISTS boss_states (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    boss_id TEXT NOT NULL UNIQUE,
    available_at TEXT NOT NULL,
    last_killed_by INTEGER,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (last_killed_by) REFERENCES characters(id)
);

CREATE TABLE IF NOT EXISTS gm_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    admin_account_id INTEGER NOT NULL,
    target_character_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    payload_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_account_id) REFERENCES accounts(id),
    FOREIGN KEY (target_character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_gm_logs_admin_account_id ON gm_logs(admin_account_id);
CREATE INDEX IF NOT EXISTS idx_gm_logs_target_character_id ON gm_logs(target_character_id);

CREATE TABLE IF NOT EXISTS gm_system_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    admin_account_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    payload_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_gm_system_logs_admin ON gm_system_logs(admin_account_id);

CREATE TABLE IF NOT EXISTS mails (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    attachment_gold INTEGER NOT NULL DEFAULT 0,
    attachment_item_id TEXT,
    attachment_item_type TEXT,
    attachment_quantity INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 0,
    read_at TEXT,
    deleted INTEGER NOT NULL DEFAULT 0,
    expires_at TEXT,
    source_type TEXT NOT NULL DEFAULT 'system',
    source_id TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claimed_at TEXT,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_mails_character_status ON mails(character_id, status);

CREATE TABLE IF NOT EXISTS world_boss_states (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    boss_id TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'available',
    current_hp INTEGER NOT NULL,
    max_hp INTEGER NOT NULL,
    available_at TEXT NOT NULL,
    killed_at TEXT,
    rewards_sent INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS world_boss_damage_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    boss_id TEXT NOT NULL,
    character_id INTEGER NOT NULL,
    damage INTEGER NOT NULL DEFAULT 0,
    battle_id INTEGER,
    rewarded INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_world_boss_damage_boss ON world_boss_damage_logs(boss_id, damage DESC);
CREATE INDEX IF NOT EXISTS idx_world_boss_damage_character ON world_boss_damage_logs(character_id);

CREATE TABLE IF NOT EXISTS map_event_states (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    event_id TEXT NOT NULL,
    trigger_count INTEGER NOT NULL DEFAULT 0,
    last_triggered_at TEXT,
    next_available_at TEXT,
    completed INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, event_id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_map_event_states_character ON map_event_states(character_id);

CREATE TABLE IF NOT EXISTS activity_claims (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    activity_id TEXT NOT NULL,
    gold_gained INTEGER NOT NULL DEFAULT 0,
    items_json TEXT NOT NULL DEFAULT '[]',
    claimed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, activity_id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_activity_claims_character ON activity_claims(character_id);

CREATE TABLE IF NOT EXISTS guilds (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    notice TEXT NOT NULL DEFAULT '',
    leader_character_id INTEGER NOT NULL,
    member_count INTEGER NOT NULL DEFAULT 1,
    total_contribution INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guilds_leader ON guilds(leader_character_id);

CREATE TABLE IF NOT EXISTS guild_members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL UNIQUE,
    role TEXT NOT NULL DEFAULT 'member',
    contribution INTEGER NOT NULL DEFAULT 0,
    joined_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guild_id) REFERENCES guilds(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guild_members_guild ON guild_members(guild_id);

CREATE TABLE IF NOT EXISTS guild_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id INTEGER NOT NULL,
    character_id INTEGER,
    action TEXT NOT NULL,
    payload_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guild_id) REFERENCES guilds(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guild_logs_guild ON guild_logs(guild_id);

CREATE TABLE IF NOT EXISTS guild_donations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    donation_id TEXT NOT NULL,
    gold_cost INTEGER NOT NULL DEFAULT 0,
    contribution_gained INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guild_id) REFERENCES guilds(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guild_donations_member_day ON guild_donations(character_id, donation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_guild_donations_guild ON guild_donations(guild_id);

CREATE TABLE IF NOT EXISTS guild_shop_purchases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    shop_item_id TEXT NOT NULL,
    item_id TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    contribution_cost INTEGER NOT NULL DEFAULT 0,
    gold_cost INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guild_id) REFERENCES guilds(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guild_shop_purchases_member_day ON guild_shop_purchases(character_id, shop_item_id, created_at);
CREATE INDEX IF NOT EXISTS idx_guild_shop_purchases_guild ON guild_shop_purchases(guild_id);

CREATE TABLE IF NOT EXISTS guild_activity_claims (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    activity_id TEXT NOT NULL,
    reward_gold INTEGER NOT NULL DEFAULT 0,
    reward_items_json TEXT NOT NULL DEFAULT '[]',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(guild_id, character_id, activity_id),
    FOREIGN KEY (guild_id) REFERENCES guilds(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_guild_activity_claims_member ON guild_activity_claims(character_id, activity_id);

CREATE TABLE IF NOT EXISTS character_skills (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    skill_id TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    skill_slot INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, skill_id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_character_skills_character ON character_skills(character_id);

CREATE TABLE IF NOT EXISTS character_talents (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id INTEGER NOT NULL,
    talent_id TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(character_id, talent_id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE INDEX IF NOT EXISTS idx_character_talents_character ON character_talents(character_id);

CREATE TABLE IF NOT EXISTS battle_preparations (
    character_id INTEGER PRIMARY KEY,
    bonus_hp INTEGER NOT NULL DEFAULT 0,
    bonus_attack INTEGER NOT NULL DEFAULT 0,
    bonus_defense INTEGER NOT NULL DEFAULT 0,
    bonus_attack_speed INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

CREATE TABLE IF NOT EXISTS login_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    ip TEXT,
    user_agent TEXT,
    success INTEGER NOT NULL,
    reason TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_login_logs_account_id ON login_logs(account_id);
