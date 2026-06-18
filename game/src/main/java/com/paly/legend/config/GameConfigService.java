package com.paly.legend.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GameConfigService {

    private static final Logger log = LoggerFactory.getLogger(GameConfigService.class);

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String configLocation;

    private List<MapConfig> maps = Collections.emptyList();
    private Map<String, MapConfig> mapById = Collections.emptyMap();
    private Map<String, MonsterConfig> monsterById = Collections.emptyMap();
    private List<MonsterConfig> monsters = Collections.emptyList();
    private Map<String, ItemConfig> itemById = Collections.emptyMap();
    private Map<Integer, LevelConfig> levelByLevel = Collections.emptyMap();
    private Map<String, DropConfig> monsterDropBySourceId = Collections.emptyMap();
    private List<TaskConfig> tasks = Collections.emptyList();
    private Map<String, TaskConfig> taskById = Collections.emptyMap();
    private List<NpcConfig> npcs = Collections.emptyList();
    private Map<String, NpcConfig> npcById = Collections.emptyMap();
    private List<MapEventConfig> mapEvents = Collections.emptyList();
    private Map<String, MapEventConfig> mapEventById = Collections.emptyMap();
    private List<BossConfig> bosses = Collections.emptyList();
    private Map<String, BossConfig> bossById = Collections.emptyMap();
    private List<WorldBossConfig> worldBosses = Collections.emptyList();
    private Map<String, WorldBossConfig> worldBossById = Collections.emptyMap();
    private List<ClassConfig> classes = Collections.emptyList();
    private Map<String, ClassConfig> classById = Collections.emptyMap();
    private List<SkillConfig> skills = Collections.emptyList();
    private Map<String, SkillConfig> skillById = Collections.emptyMap();
    private List<TalentConfig> talents = Collections.emptyList();
    private Map<String, TalentConfig> talentById = Collections.emptyMap();
    private List<EnhancementRuleConfig> enhancementRules = Collections.emptyList();
    private Map<String, EquipmentAffixQualityConfig> equipmentAffixRuleByQuality = Collections.emptyMap();
    private List<ActivityConfig> activities = Collections.emptyList();
    private Map<String, ActivityConfig> activityById = Collections.emptyMap();
    private GuildConfig guildConfig = new GuildConfig();

    public GameConfigService(ObjectMapper objectMapper,
                             ResourceLoader resourceLoader,
                             @Value("${game.config.location:classpath:config/}") String configLocation) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.configLocation = configLocation == null || configLocation.trim().isEmpty()
                ? "classpath:config/"
                : configLocation.trim();
    }

    @PostConstruct
    public void load() {
        ConfigSnapshot snapshot = loadSnapshot();
        applySnapshot(snapshot);
        log.info("Loaded game configs: {}", snapshot.summary());
    }

    public synchronized String reload() {
        ConfigSnapshot snapshot = loadSnapshot();
        applySnapshot(snapshot);
        log.info("Reloaded game configs: {}", snapshot.summary());
        return snapshot.summary();
    }

    private ConfigSnapshot loadSnapshot() {
        List<MapConfig> loadedMaps = readList(configResource("maps.json"), new TypeReference<List<MapConfig>>() {
        });
        List<MonsterConfig> loadedMonsters = readList(configResource("monsters.json"), new TypeReference<List<MonsterConfig>>() {
        });
        List<ItemConfig> loadedItems = readList(configResource("items.json"), new TypeReference<List<ItemConfig>>() {
        });
        List<LevelConfig> loadedLevels = readList(configResource("levels.json"), new TypeReference<List<LevelConfig>>() {
        });
        List<DropConfig> loadedDrops = readList(configResource("drops.json"), new TypeReference<List<DropConfig>>() {
        });
        List<TaskConfig> loadedTasks = readList(configResource("tasks.json"), new TypeReference<List<TaskConfig>>() {
        });
        List<NpcConfig> loadedNpcs = readList(configResource("npcs.json"), new TypeReference<List<NpcConfig>>() {
        });
        List<MapEventConfig> loadedMapEvents = readList(configResource("map-events.json"), new TypeReference<List<MapEventConfig>>() {
        });
        List<BossConfig> loadedBosses = readList(configResource("bosses.json"), new TypeReference<List<BossConfig>>() {
        });
        List<WorldBossConfig> loadedWorldBosses = readList(configResource("world-bosses.json"), new TypeReference<List<WorldBossConfig>>() {
        });
        List<ClassConfig> loadedClasses = readList(configResource("classes.json"), new TypeReference<List<ClassConfig>>() {
        });
        List<SkillConfig> loadedSkills = readList(configResource("skills.json"), new TypeReference<List<SkillConfig>>() {
        });
        List<TalentConfig> loadedTalents = readList(configResource("talents.json"), new TypeReference<List<TalentConfig>>() {
        });
        List<EnhancementRuleConfig> loadedEnhancementRules = readList(configResource("enhancement-rules.json"), new TypeReference<List<EnhancementRuleConfig>>() {
        });
        List<EquipmentAffixQualityConfig> loadedEquipmentAffixes = readList(configResource("equipment-affixes.json"), new TypeReference<List<EquipmentAffixQualityConfig>>() {
        });
        List<ActivityConfig> loadedActivities = readList(configResource("activities.json"), new TypeReference<List<ActivityConfig>>() {
        });
        GuildConfig loadedGuildConfig = readList(configResource("guilds.json"), new TypeReference<GuildConfig>() {
        });

        ConfigSnapshot snapshot = new ConfigSnapshot();
        snapshot.maps = Collections.unmodifiableList(new ArrayList<MapConfig>(loadedMaps));
        snapshot.mapById = indexMaps(loadedMaps);
        snapshot.monsters = Collections.unmodifiableList(new ArrayList<MonsterConfig>(loadedMonsters));
        snapshot.monsterById = indexMonsters(loadedMonsters);
        snapshot.itemById = indexItems(loadedItems);
        snapshot.levelByLevel = indexLevels(loadedLevels);
        snapshot.monsterDropBySourceId = indexMonsterDrops(loadedDrops);
        snapshot.tasks = Collections.unmodifiableList(new ArrayList<TaskConfig>(loadedTasks));
        snapshot.taskById = indexTasks(loadedTasks);
        snapshot.npcs = Collections.unmodifiableList(new ArrayList<NpcConfig>(loadedNpcs));
        snapshot.npcById = indexNpcs(loadedNpcs);
        snapshot.mapEvents = Collections.unmodifiableList(new ArrayList<MapEventConfig>(loadedMapEvents));
        snapshot.mapEventById = indexMapEvents(loadedMapEvents);
        snapshot.bosses = Collections.unmodifiableList(new ArrayList<BossConfig>(loadedBosses));
        snapshot.bossById = indexBosses(loadedBosses);
        snapshot.worldBosses = Collections.unmodifiableList(new ArrayList<WorldBossConfig>(loadedWorldBosses));
        snapshot.worldBossById = indexWorldBosses(loadedWorldBosses);
        snapshot.classes = Collections.unmodifiableList(new ArrayList<ClassConfig>(loadedClasses));
        snapshot.classById = indexClasses(loadedClasses);
        snapshot.skills = Collections.unmodifiableList(new ArrayList<SkillConfig>(loadedSkills));
        snapshot.skillById = indexSkills(loadedSkills);
        snapshot.talents = Collections.unmodifiableList(new ArrayList<TalentConfig>(loadedTalents));
        snapshot.talentById = indexTalents(loadedTalents);
        snapshot.enhancementRules = Collections.unmodifiableList(new ArrayList<EnhancementRuleConfig>(loadedEnhancementRules));
        snapshot.equipmentAffixRuleByQuality = indexEquipmentAffixRules(loadedEquipmentAffixes);
        snapshot.activities = Collections.unmodifiableList(new ArrayList<ActivityConfig>(loadedActivities));
        snapshot.activityById = indexActivities(loadedActivities);
        snapshot.guildConfig = loadedGuildConfig == null ? new GuildConfig() : loadedGuildConfig;
        validateSnapshot(snapshot);
        return snapshot;
    }

    private synchronized void applySnapshot(ConfigSnapshot snapshot) {
        this.maps = snapshot.maps;
        this.mapById = snapshot.mapById;
        this.monsters = snapshot.monsters;
        this.monsterById = snapshot.monsterById;
        this.itemById = snapshot.itemById;
        this.levelByLevel = snapshot.levelByLevel;
        this.monsterDropBySourceId = snapshot.monsterDropBySourceId;
        this.tasks = snapshot.tasks;
        this.taskById = snapshot.taskById;
        this.npcs = snapshot.npcs;
        this.npcById = snapshot.npcById;
        this.mapEvents = snapshot.mapEvents;
        this.mapEventById = snapshot.mapEventById;
        this.bosses = snapshot.bosses;
        this.bossById = snapshot.bossById;
        this.worldBosses = snapshot.worldBosses;
        this.worldBossById = snapshot.worldBossById;
        this.classes = snapshot.classes;
        this.classById = snapshot.classById;
        this.skills = snapshot.skills;
        this.skillById = snapshot.skillById;
        this.talents = snapshot.talents;
        this.talentById = snapshot.talentById;
        this.enhancementRules = snapshot.enhancementRules;
        this.equipmentAffixRuleByQuality = snapshot.equipmentAffixRuleByQuality;
        this.activities = snapshot.activities;
        this.activityById = snapshot.activityById;
        this.guildConfig = snapshot.guildConfig;
    }

    public List<MapConfig> listMaps() {
        return maps;
    }

    public MapConfig getMapRequired(String mapId) {
        MapConfig map = mapById.get(mapId);
        if (map == null) {
            throw new BusinessException("MAP_NOT_FOUND", "地图不存在", HttpStatus.NOT_FOUND);
        }
        return map;
    }

    public MonsterConfig getMonsterRequired(String monsterId) {
        MonsterConfig monster = monsterById.get(monsterId);
        if (monster == null) {
            throw new BusinessException("MONSTER_NOT_FOUND", "怪物不存在", HttpStatus.NOT_FOUND);
        }
        return monster;
    }

    public List<MonsterConfig> listMonsters() {
        return monsters;
    }

    public ItemConfig getItemRequired(String itemId) {
        ItemConfig item = itemById.get(itemId);
        if (item == null) {
            throw new BusinessException("ITEM_NOT_FOUND", "物品不存在", HttpStatus.NOT_FOUND);
        }
        return item;
    }

    public Integer getRequiredExpToNextLevel(int level) {
        LevelConfig config = levelByLevel.get(level);
        return config == null ? null : config.getRequiredExp();
    }

    public List<DropItemConfig> getMonsterDrops(String monsterId) {
        DropConfig dropConfig = monsterDropBySourceId.get(monsterId);
        if (dropConfig == null || dropConfig.getItems() == null) {
            return Collections.emptyList();
        }
        return dropConfig.getItems();
    }

    public List<TaskConfig> listTasks() {
        return tasks;
    }

    public TaskConfig getTaskRequired(String taskId) {
        TaskConfig task = taskById.get(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在", HttpStatus.NOT_FOUND);
        }
        return task;
    }

    public List<NpcConfig> listNpcsByMapId(String mapId) {
        List<NpcConfig> result = new ArrayList<NpcConfig>();
        for (NpcConfig npc : npcs) {
            if (mapId.equals(npc.getMapId())) {
                result.add(npc);
            }
        }
        return result;
    }

    public NpcConfig getNpcRequired(String npcId) {
        NpcConfig npc = npcById.get(npcId);
        if (npc == null) {
            throw new BusinessException("NPC_NOT_FOUND", "NPC 不存在", HttpStatus.NOT_FOUND);
        }
        return npc;
    }

    public List<MapEventConfig> listMapEventsByMapId(String mapId) {
        List<MapEventConfig> result = new ArrayList<MapEventConfig>();
        for (MapEventConfig event : mapEvents) {
            if (mapId.equals(event.getMapId())) {
                result.add(event);
            }
        }
        return result;
    }

    public MapEventConfig getMapEventRequired(String eventId) {
        MapEventConfig event = mapEventById.get(eventId);
        if (event == null) {
            throw new BusinessException("MAP_EVENT_NOT_FOUND", "地图事件不存在", HttpStatus.NOT_FOUND);
        }
        return event;
    }

    public List<BossConfig> listBosses() {
        return bosses;
    }

    public BossConfig getBossRequired(String bossId) {
        BossConfig boss = bossById.get(bossId);
        if (boss == null) {
            throw new BusinessException("BOSS_NOT_FOUND", "BOSS 不存在", HttpStatus.NOT_FOUND);
        }
        return boss;
    }

    public List<WorldBossConfig> listWorldBosses() {
        return worldBosses;
    }

    public WorldBossConfig getWorldBossRequired(String bossId) {
        WorldBossConfig boss = worldBossById.get(bossId);
        if (boss == null) {
            throw new BusinessException("WORLD_BOSS_NOT_FOUND", "世界 BOSS 不存在", HttpStatus.NOT_FOUND);
        }
        return boss;
    }

    public List<ClassConfig> listClasses() {
        return classes;
    }

    public ClassConfig getClassRequired(String className) {
        ClassConfig config = classById.get(className);
        if (config == null) {
            throw new BusinessException("CLASS_NOT_FOUND", "职业不存在", HttpStatus.NOT_FOUND);
        }
        return config;
    }

    public List<SkillConfig> listSkills() {
        return skills;
    }

    public List<SkillConfig> listSkillsByClassName(String className) {
        List<SkillConfig> result = new ArrayList<SkillConfig>();
        for (SkillConfig skill : skills) {
            if (className.equals(skill.getClassName())) {
                result.add(skill);
            }
        }
        return result;
    }

    public SkillConfig getSkillRequired(String skillId) {
        SkillConfig skill = skillById.get(skillId);
        if (skill == null) {
            throw new BusinessException("SKILL_NOT_FOUND", "技能不存在", HttpStatus.NOT_FOUND);
        }
        return skill;
    }

    public List<TalentConfig> listTalents() {
        return talents;
    }

    public TalentConfig getTalentRequired(String talentId) {
        TalentConfig talent = talentById.get(talentId);
        if (talent == null) {
            throw new BusinessException("TALENT_NOT_FOUND", "天赋不存在", HttpStatus.NOT_FOUND);
        }
        return talent;
    }

    public EnhancementRuleConfig getEnhancementRule(ItemConfig item, int nextLevel) {
        EnhancementRuleConfig fallback = null;
        for (EnhancementRuleConfig rule : enhancementRules) {
            if (rule.getNextLevel() != nextLevel) {
                continue;
            }
            if (rule.getQuality() != null && !rule.getQuality().trim().isEmpty()
                    && !rule.getQuality().equals(item.getQuality())) {
                continue;
            }
            if (item.getRequiredLevel() < rule.getMinRequiredLevel()) {
                continue;
            }
            if (rule.getQuality() == null || rule.getQuality().trim().isEmpty()) {
                fallback = rule;
            } else {
                return rule;
            }
        }
        return fallback;
    }

    public EquipmentAffixQualityConfig getEquipmentAffixRule(String quality) {
        return equipmentAffixRuleByQuality.get(quality);
    }

    public List<ActivityConfig> listActivities() {
        return activities;
    }

    public synchronized String saveActivities(List<ActivityConfig> nextActivities) {
        List<ActivityConfig> safeActivities = nextActivities == null
                ? Collections.<ActivityConfig>emptyList()
                : new ArrayList<ActivityConfig>(nextActivities);
        ConfigSnapshot snapshot = loadSnapshot();
        snapshot.activities = Collections.unmodifiableList(new ArrayList<ActivityConfig>(safeActivities));
        snapshot.activityById = indexActivities(safeActivities);
        validateSnapshot(snapshot);
        writeConfig("activities.json", safeActivities);
        applySnapshot(snapshot);
        log.info("Saved activity configs: {}", snapshot.summary());
        return snapshot.summary();
    }

    public ActivityConfig getActivityRequired(String activityId) {
        ActivityConfig activity = activityById.get(activityId);
        if (activity == null) {
            throw new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在", HttpStatus.NOT_FOUND);
        }
        return activity;
    }

    public GuildConfig getGuildConfig() {
        return guildConfig == null ? new GuildConfig() : guildConfig;
    }

    private <T> T readList(String location, TypeReference<T> typeReference) {
        Resource resource = resourceLoader.getResource(location);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load config: " + location, ex);
        }
    }

    private String configResource(String fileName) {
        String base = configLocation.endsWith("/") || configLocation.endsWith("\\")
                ? configLocation
                : configLocation + "/";
        return base + fileName;
    }

    private void writeConfig(String fileName, Object value) {
        Set<File> targets = writableConfigTargets(fileName);
        if (targets.isEmpty()) {
            throw new IllegalStateException("Config file is not writable: " + configResource(fileName));
        }
        for (File target : targets) {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Failed to create config directory: " + parent.getAbsolutePath());
            }
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(target, value);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to write config: " + target.getAbsolutePath(), ex);
            }
        }
    }

    private Set<File> writableConfigTargets(String fileName) {
        Set<File> targets = new LinkedHashSet<File>();
        String location = configResource(fileName);
        try {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists() && resource.isFile()) {
                targets.add(resource.getFile());
            }
        } catch (IOException ignored) {
            // Classpath resources inside jars are not writable; explicit file targets below cover local development.
        }
        File sourceConfig = new File("src/main/resources/config", fileName);
        if (sourceConfig.exists()) {
            targets.add(sourceConfig);
        }
        File compiledConfig = new File("target/classes/config", fileName);
        if (compiledConfig.exists()) {
            targets.add(compiledConfig);
        }
        return targets;
    }

    private Map<String, MapConfig> indexMaps(List<MapConfig> values) {
        Map<String, MapConfig> result = new LinkedHashMap<String, MapConfig>();
        for (MapConfig value : values) {
            ensureUnique(result, value.getId(), "map");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, MonsterConfig> indexMonsters(List<MonsterConfig> values) {
        Map<String, MonsterConfig> result = new LinkedHashMap<String, MonsterConfig>();
        for (MonsterConfig value : values) {
            ensureUnique(result, value.getId(), "monster");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, ItemConfig> indexItems(List<ItemConfig> values) {
        Map<String, ItemConfig> result = new LinkedHashMap<String, ItemConfig>();
        for (ItemConfig value : values) {
            ensureUnique(result, value.getId(), "item");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<Integer, LevelConfig> indexLevels(List<LevelConfig> values) {
        Map<Integer, LevelConfig> result = new LinkedHashMap<Integer, LevelConfig>();
        for (LevelConfig value : values) {
            if (value.getLevel() <= 0) {
                throw new IllegalStateException("Invalid level config level: " + value.getLevel());
            }
            if (result.containsKey(value.getLevel())) {
                throw new IllegalStateException("Duplicate level config: " + value.getLevel());
            }
            result.put(value.getLevel(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, DropConfig> indexMonsterDrops(List<DropConfig> values) {
        Map<String, DropConfig> result = new LinkedHashMap<String, DropConfig>();
        for (DropConfig value : values) {
            if (!"monster".equals(value.getSourceType())) {
                continue;
            }
            ensureUnique(result, value.getSourceId(), "monster drop");
            result.put(value.getSourceId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, TaskConfig> indexTasks(List<TaskConfig> values) {
        Map<String, TaskConfig> result = new LinkedHashMap<String, TaskConfig>();
        for (TaskConfig value : values) {
            ensureUnique(result, value.getId(), "task");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, NpcConfig> indexNpcs(List<NpcConfig> values) {
        Map<String, NpcConfig> result = new LinkedHashMap<String, NpcConfig>();
        for (NpcConfig value : values) {
            ensureUnique(result, value.getId(), "npc");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, MapEventConfig> indexMapEvents(List<MapEventConfig> values) {
        Map<String, MapEventConfig> result = new LinkedHashMap<String, MapEventConfig>();
        for (MapEventConfig value : values) {
            ensureUnique(result, value.getId(), "map event");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, BossConfig> indexBosses(List<BossConfig> values) {
        Map<String, BossConfig> result = new LinkedHashMap<String, BossConfig>();
        for (BossConfig value : values) {
            ensureUnique(result, value.getId(), "boss");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, WorldBossConfig> indexWorldBosses(List<WorldBossConfig> values) {
        Map<String, WorldBossConfig> result = new LinkedHashMap<String, WorldBossConfig>();
        for (WorldBossConfig value : values) {
            ensureUnique(result, value.getId(), "world boss");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, ClassConfig> indexClasses(List<ClassConfig> values) {
        Map<String, ClassConfig> result = new LinkedHashMap<String, ClassConfig>();
        for (ClassConfig value : values) {
            ensureUnique(result, value.getId(), "class");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, SkillConfig> indexSkills(List<SkillConfig> values) {
        Map<String, SkillConfig> result = new LinkedHashMap<String, SkillConfig>();
        for (SkillConfig value : values) {
            ensureUnique(result, value.getId(), "skill");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, TalentConfig> indexTalents(List<TalentConfig> values) {
        Map<String, TalentConfig> result = new LinkedHashMap<String, TalentConfig>();
        for (TalentConfig value : values) {
            ensureUnique(result, value.getId(), "talent");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, EquipmentAffixQualityConfig> indexEquipmentAffixRules(List<EquipmentAffixQualityConfig> values) {
        Map<String, EquipmentAffixQualityConfig> result = new LinkedHashMap<String, EquipmentAffixQualityConfig>();
        for (EquipmentAffixQualityConfig value : values) {
            ensureUnique(result, value.getQuality(), "equipment affix quality");
            result.put(value.getQuality(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<String, ActivityConfig> indexActivities(List<ActivityConfig> values) {
        Map<String, ActivityConfig> result = new LinkedHashMap<String, ActivityConfig>();
        for (ActivityConfig value : values) {
            ensureUnique(result, value.getId(), "activity");
            result.put(value.getId(), value);
        }
        return Collections.unmodifiableMap(result);
    }

    private void ensureUnique(Map<String, ?> values, String id, String type) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalStateException("Blank " + type + " config id");
        }
        if (values.containsKey(id)) {
            throw new IllegalStateException("Duplicate " + type + " config id: " + id);
        }
    }

    private void validateSnapshot(ConfigSnapshot snapshot) {
        validateClassRefs(snapshot);
        validateItemRefs(snapshot);
        validateEquipmentAffixRules(snapshot);
        validateMapMonsterRefs(snapshot);
        validateDropRefs(snapshot);
        validateTaskRefs(snapshot);
        validateNpcRefs(snapshot);
        validateMapEventRefs(snapshot);
        validateBossRefs(snapshot);
        validateWorldBossRefs(snapshot);
        validateSkillRefs(snapshot);
        validateTalentRefs(snapshot);
        validateEnhancementRules(snapshot);
        validateActivityRefs(snapshot);
        validateGuildConfig(snapshot);
    }

    private void validateClassRefs(ConfigSnapshot snapshot) {
        for (ClassConfig config : snapshot.classes) {
            if (config.getHp() <= 0 || config.getAttack() <= 0 || config.getDefense() < 0 || config.getAttackSpeed() <= 0) {
                throw new IllegalStateException("Invalid class base stats: " + config.getId());
            }
            if (config.getHpPerLevel() < 0 || config.getAttackPerLevel() < 0
                    || config.getDefensePerLevel() < 0 || config.getAttackSpeedPerLevel() < 0) {
                throw new IllegalStateException("Invalid class growth stats: " + config.getId());
            }
        }
    }

    private void validateItemRefs(ConfigSnapshot snapshot) {
        for (ItemConfig item : snapshot.itemById.values()) {
            if (!"equipment".equals(item.getType()) && !"material".equals(item.getType()) && !"consumable".equals(item.getType())) {
                throw new IllegalStateException("Invalid item type: " + item.getId());
            }
            if ("equipment".equals(item.getType()) && (item.getSlot() == null || item.getSlot().trim().isEmpty())) {
                throw new IllegalStateException("Equipment item missing slot: " + item.getId());
            }
            if ("equipment".equals(item.getType())) {
                boolean hasSet = item.getSetId() != null && !item.getSetId().trim().isEmpty();
                if (hasSet && (item.getSetName() == null || item.getSetName().trim().isEmpty())) {
                    throw new IllegalStateException("Equipment set item missing set name: " + item.getId());
                }
                if (!hasSet && item.getSetBonuses() != null && !item.getSetBonuses().isEmpty()) {
                    throw new IllegalStateException("Equipment item has set bonuses without set id: " + item.getId());
                }
                if (item.getSetBonuses() != null) {
                    for (EquipmentSetBonusConfig bonus : item.getSetBonuses()) {
                        if (bonus.getPieces() <= 1) {
                            throw new IllegalStateException("Equipment set bonus pieces must be greater than 1: " + item.getId());
                        }
                        if (bonus.getHp() < 0 || bonus.getAttack() < 0 || bonus.getDefense() < 0 || bonus.getAttackSpeed() < 0) {
                            throw new IllegalStateException("Equipment set bonus stats cannot be negative: " + item.getId());
                        }
                    }
                }
            }
            if ("consumable".equals(item.getType())
                    && item.getBuffHp() <= 0
                    && item.getBuffAttack() <= 0
                    && item.getBuffDefense() <= 0
                    && item.getBuffAttackSpeed() <= 0) {
                throw new IllegalStateException("Consumable item has no buff effect: " + item.getId());
            }
            if (item.getSellGold() < 0 || item.getRequiredLevel() < 0) {
                throw new IllegalStateException("Invalid item economy fields: " + item.getId());
            }
        }
    }

    private void validateEquipmentAffixRules(ConfigSnapshot snapshot) {
        for (ItemConfig item : snapshot.itemById.values()) {
            if ("equipment".equals(item.getType()) && !snapshot.equipmentAffixRuleByQuality.containsKey(item.getQuality())) {
                throw new IllegalStateException("Equipment item missing affix rule for quality: " + item.getId());
            }
        }
        for (EquipmentAffixQualityConfig rule : snapshot.equipmentAffixRuleByQuality.values()) {
            if (rule.getAffixCount() <= 0 || rule.getRerollMaterialQuantity() <= 0) {
                throw new IllegalStateException("Invalid equipment affix numeric fields: " + rule.getQuality());
            }
            if (!snapshot.itemById.containsKey(rule.getRerollMaterialId())) {
                throw new IllegalStateException("Affix reroll references missing material: " + rule.getRerollMaterialId());
            }
            ItemConfig material = snapshot.itemById.get(rule.getRerollMaterialId());
            if (!"material".equals(material.getType())) {
                throw new IllegalStateException("Affix reroll item is not material: " + rule.getRerollMaterialId());
            }
            if (rule.getStats() == null || rule.getStats().isEmpty()) {
                throw new IllegalStateException("Equipment affix rule has no stats: " + rule.getQuality());
            }
            int totalWeight = 0;
            for (EquipmentAffixStatConfig stat : rule.getStats()) {
                if (!isValidAffixStat(stat.getStat())) {
                    throw new IllegalStateException("Invalid equipment affix stat: " + rule.getQuality() + "/" + stat.getStat());
                }
                if (stat.getWeight() <= 0 || stat.getMin() <= 0 || stat.getMax() < stat.getMin()) {
                    throw new IllegalStateException("Invalid equipment affix stat range: " + rule.getQuality() + "/" + stat.getStat());
                }
                if ("skillTriggerBonus".equals(stat.getStat()) && stat.getMax() > 0.2) {
                    throw new IllegalStateException("Skill trigger affix bonus too high: " + rule.getQuality());
                }
                totalWeight += stat.getWeight();
            }
            if (totalWeight <= 0 || rule.getAffixCount() > rule.getStats().size()) {
                throw new IllegalStateException("Invalid equipment affix pool: " + rule.getQuality());
            }
        }
    }

    private boolean isValidAffixStat(String stat) {
        return "hp".equals(stat)
                || "attack".equals(stat)
                || "defense".equals(stat)
                || "attackSpeed".equals(stat)
                || "skillTriggerBonus".equals(stat);
    }

    private void validateMapMonsterRefs(ConfigSnapshot snapshot) {
        for (MapConfig map : snapshot.maps) {
            if (map.getWidth() <= 0 || map.getHeight() <= 0 || map.getRequiredLevel() <= 0) {
                throw new IllegalStateException("Invalid map fields: " + map.getId());
            }
            if (map.getMonsterIds() == null) {
                continue;
            }
            for (String monsterId : map.getMonsterIds()) {
                if (!snapshot.monsterById.containsKey(monsterId)) {
                    throw new IllegalStateException("Map " + map.getId() + " references missing monster: " + monsterId);
                }
            }
        }
    }

    private void validateDropRefs(ConfigSnapshot snapshot) {
        for (DropConfig dropConfig : snapshot.monsterDropBySourceId.values()) {
            if (!snapshot.monsterById.containsKey(dropConfig.getSourceId())) {
                throw new IllegalStateException("Drop references missing monster: " + dropConfig.getSourceId());
            }
            if (dropConfig.getItems() == null) {
                continue;
            }
            for (DropItemConfig item : dropConfig.getItems()) {
                if (!snapshot.itemById.containsKey(item.getItemId())) {
                    throw new IllegalStateException("Drop references missing item: " + item.getItemId());
                }
                if (item.getRate() < 0 || item.getRate() > 1 || item.getMinQuantity() <= 0 || item.getMaxQuantity() < item.getMinQuantity()) {
                    throw new IllegalStateException("Invalid drop item rule: " + dropConfig.getSourceId() + "/" + item.getItemId());
                }
            }
        }
    }

    private void validateTaskRefs(ConfigSnapshot snapshot) {
        for (TaskConfig task : snapshot.tasks) {
            if ("kill_monster".equals(task.getType()) && !snapshot.monsterById.containsKey(task.getTargetId())) {
                throw new IllegalStateException("Task references missing monster: " + task.getTargetId());
            }
            if ("talk_npc".equals(task.getType()) && !snapshot.npcById.containsKey(task.getTargetId())) {
                throw new IllegalStateException("Task references missing npc: " + task.getTargetId());
            }
            if ("explore_event".equals(task.getType()) && !snapshot.mapEventById.containsKey(task.getTargetId())) {
                throw new IllegalStateException("Task references missing map event: " + task.getTargetId());
            }
            if (task.getPreTaskIds() != null) {
                for (String preTaskId : task.getPreTaskIds()) {
                    if (!snapshot.taskById.containsKey(preTaskId)) {
                        throw new IllegalStateException("Task references missing pre task: " + preTaskId);
                    }
                    if (task.getId().equals(preTaskId)) {
                        throw new IllegalStateException("Task references itself as pre task: " + task.getId());
                    }
                }
            }
            if (task.getRewards() == null || task.getRewards().getItems() == null) {
                continue;
            }
            for (TaskRewardItemConfig item : task.getRewards().getItems()) {
                if (!snapshot.itemById.containsKey(item.getItemId())) {
                    throw new IllegalStateException("Task references missing item: " + item.getItemId());
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalStateException("Task reward item quantity invalid: " + task.getId());
                }
            }
        }
    }

    private void validateNpcRefs(ConfigSnapshot snapshot) {
        for (NpcConfig npc : snapshot.npcs) {
            if (!snapshot.mapById.containsKey(npc.getMapId())) {
                throw new IllegalStateException("Npc references missing map: " + npc.getMapId());
            }
            if (npc.getTaskIds() != null) {
                for (String taskId : npc.getTaskIds()) {
                    if (!snapshot.taskById.containsKey(taskId)) {
                        throw new IllegalStateException("Npc references missing task: " + taskId);
                    }
                }
            }
            if (npc.getRequiredTaskIds() != null) {
                for (String taskId : npc.getRequiredTaskIds()) {
                    if (!snapshot.taskById.containsKey(taskId)) {
                        throw new IllegalStateException("Npc references missing required task: " + taskId);
                    }
                }
            }
        }
    }

    private void validateMapEventRefs(ConfigSnapshot snapshot) {
        for (MapEventConfig event : snapshot.mapEvents) {
            if (!snapshot.mapById.containsKey(event.getMapId())) {
                throw new IllegalStateException("Map event references missing map: " + event.getMapId());
            }
            if (event.getTargetMapId() != null && !event.getTargetMapId().trim().isEmpty()
                    && !snapshot.mapById.containsKey(event.getTargetMapId())) {
                throw new IllegalStateException("Map event references missing target map: " + event.getTargetMapId());
            }
            if (event.getTargetMonsterIds() != null) {
                for (String monsterId : event.getTargetMonsterIds()) {
                    if (!snapshot.monsterById.containsKey(monsterId)) {
                        throw new IllegalStateException("Map event references missing monster: " + monsterId);
                    }
                    MapConfig map = snapshot.mapById.get(event.getMapId());
                    if (map.getMonsterIds() == null || !map.getMonsterIds().contains(monsterId)) {
                        throw new IllegalStateException("Map event monster is not in map: " + event.getId());
                    }
                }
            }
            if ("monster_area".equals(event.getType()) || "random_encounter".equals(event.getType())) {
                if (event.getEncounterMonsters() != null && !event.getEncounterMonsters().isEmpty()) {
                    int totalWeight = 0;
                    for (EncounterMonsterConfig encounterMonster : event.getEncounterMonsters()) {
                        if (encounterMonster.getMonsterId() == null || !snapshot.monsterById.containsKey(encounterMonster.getMonsterId())) {
                            throw new IllegalStateException("Map event encounter references missing monster: " + event.getId());
                        }
                        MapConfig map = snapshot.mapById.get(event.getMapId());
                        if (map.getMonsterIds() == null || !map.getMonsterIds().contains(encounterMonster.getMonsterId())) {
                            throw new IllegalStateException("Map event encounter monster is not in map: " + event.getId());
                        }
                        if (encounterMonster.getWeight() <= 0) {
                            throw new IllegalStateException("Invalid encounter monster weight: " + event.getId());
                        }
                        totalWeight += encounterMonster.getWeight();
                    }
                    if (totalWeight <= 0) {
                        throw new IllegalStateException("Encounter monster total weight must be positive: " + event.getId());
                    }
                }
                if (event.getEncounterMinCount() <= 0 || event.getEncounterMaxCount() < event.getEncounterMinCount()
                        || event.getEncounterMaxCount() > 5) {
                    throw new IllegalStateException("Invalid encounter count range: " + event.getId());
                }
                if (event.getEncounterEliteChance() < 0 || event.getEncounterEliteChance() > 0.6) {
                    throw new IllegalStateException("Invalid encounter elite chance: " + event.getId());
                }
                if (event.getEncounterIntervalSeconds() < 2 || event.getEncounterIntervalSeconds() > 30) {
                    throw new IllegalStateException("Invalid encounter interval seconds: " + event.getId());
                }
            }
            if (event.getRequiredTaskIds() != null) {
                for (String taskId : event.getRequiredTaskIds()) {
                    if (!snapshot.taskById.containsKey(taskId)) {
                        throw new IllegalStateException("Map event references missing required task: " + taskId);
                    }
                }
            }
            if (event.getRewardItems() != null) {
                for (TaskRewardItemConfig item : event.getRewardItems()) {
                    if (!snapshot.itemById.containsKey(item.getItemId())) {
                        throw new IllegalStateException("Map event references missing reward item: " + item.getItemId());
                    }
                    if (item.getQuantity() <= 0) {
                        throw new IllegalStateException("Map event reward item quantity invalid: " + event.getId());
                    }
                }
            }
            if (event.getNextEventIds() != null) {
                for (String nextEventId : event.getNextEventIds()) {
                    if (!snapshot.mapEventById.containsKey(nextEventId)) {
                        throw new IllegalStateException("Map event references missing next event: " + nextEventId);
                    }
                }
            }
        }
    }

    private void validateBossRefs(ConfigSnapshot snapshot) {
        for (BossConfig boss : snapshot.bosses) {
            MapConfig map = snapshot.mapById.get(boss.getMapId());
            if (map == null) {
                throw new IllegalStateException("Boss references missing map: " + boss.getMapId());
            }
            if (!snapshot.monsterById.containsKey(boss.getMonsterId())) {
                throw new IllegalStateException("Boss references missing monster: " + boss.getMonsterId());
            }
            if (map.getMonsterIds() == null || !map.getMonsterIds().contains(boss.getMonsterId())) {
                throw new IllegalStateException("Boss monster is not in map: " + boss.getId());
            }
        }
    }

    private void validateWorldBossRefs(ConfigSnapshot snapshot) {
        for (WorldBossConfig boss : snapshot.worldBosses) {
            MapConfig map = snapshot.mapById.get(boss.getMapId());
            if (map == null) {
                throw new IllegalStateException("World boss references missing map: " + boss.getMapId());
            }
            if (!snapshot.monsterById.containsKey(boss.getMonsterId())) {
                throw new IllegalStateException("World boss references missing monster: " + boss.getMonsterId());
            }
            if (map.getMonsterIds() == null || !map.getMonsterIds().contains(boss.getMonsterId())) {
                throw new IllegalStateException("World boss monster is not in map: " + boss.getId());
            }
        }
    }

    private void validateSkillRefs(ConfigSnapshot snapshot) {
        for (SkillConfig skill : snapshot.skills) {
            if (!snapshot.classById.containsKey(skill.getClassName())) {
                throw new IllegalStateException("Skill references missing class: " + skill.getId());
            }
            if (!"active".equals(skill.getType()) && !"passive".equals(skill.getType())) {
                throw new IllegalStateException("Invalid skill type: " + skill.getId());
            }
            String targetType = skill.getTargetType();
            if (targetType != null && !targetType.trim().isEmpty()
                    && !"single".equals(targetType)
                    && !"front_row".equals(targetType)
                    && !"back_row".equals(targetType)
                    && !"random3".equals(targetType)
                    && !"all".equals(targetType)) {
                throw new IllegalStateException("Invalid skill target type: " + skill.getId());
            }
            if (skill.getMaxLevel() <= 0) {
                throw new IllegalStateException("Invalid skill max level: " + skill.getId());
            }
            if (skill.getRequiredLevel() <= 0 || skill.getTriggerChance() < 0 || skill.getTriggerChance() > 1
                    || skill.getCooldownRounds() < 0 || skill.getUpgradeGoldBase() < 0 || skill.getUpgradeGoldStep() < 0) {
                throw new IllegalStateException("Invalid skill numeric fields: " + skill.getId());
            }
            if ("active".equals(skill.getType()) && skill.getDamageMultiplier() <= 0 && skill.getFlatDamage() <= 0 && skill.getSelfHealMultiplier() <= 0) {
                throw new IllegalStateException("Active skill has no effect: " + skill.getId());
            }
            if (skill.getUpgradeMaterials() != null) {
                for (TaskRewardItemConfig item : skill.getUpgradeMaterials()) {
                    if (!snapshot.itemById.containsKey(item.getItemId())) {
                        throw new IllegalStateException("Skill references missing upgrade material: " + item.getItemId());
                    }
                    ItemConfig itemConfig = snapshot.itemById.get(item.getItemId());
                    if (!"material".equals(itemConfig.getType())) {
                        throw new IllegalStateException("Skill upgrade material is not material: " + item.getItemId());
                    }
                    if (item.getQuantity() <= 0) {
                        throw new IllegalStateException("Skill upgrade material quantity invalid: " + skill.getId());
                    }
                }
            }
        }
    }

    private void validateTalentRefs(ConfigSnapshot snapshot) {
        for (TalentConfig talent : snapshot.talents) {
            if (talent.getMaxLevel() <= 0) {
                throw new IllegalStateException("Invalid talent max level: " + talent.getId());
            }
            if (talent.getRequiredLevel() <= 0 || talent.getPreTalentLevel() < 0
                    || talent.getSkillTriggerBonus() < 0 || talent.getGoldBonusPercent() < 0) {
                throw new IllegalStateException("Invalid talent numeric fields: " + talent.getId());
            }
            String preTalentId = talent.getPreTalentId();
            if (preTalentId != null && !preTalentId.trim().isEmpty() && !snapshot.talentById.containsKey(preTalentId)) {
                throw new IllegalStateException("Talent references missing pre talent: " + talent.getId());
            }
        }
    }

    private void validateEnhancementRules(ConfigSnapshot snapshot) {
        for (EnhancementRuleConfig rule : snapshot.enhancementRules) {
            if (rule.getNextLevel() <= 0 || rule.getGoldCost() < 0 || rule.getMinRequiredLevel() < 0) {
                throw new IllegalStateException("Invalid enhancement rule numeric fields: " + rule.getNextLevel());
            }
            if (rule.getMaterialCosts() == null) {
                continue;
            }
            for (TaskRewardItemConfig item : rule.getMaterialCosts()) {
                if (!snapshot.itemById.containsKey(item.getItemId())) {
                    throw new IllegalStateException("Enhancement rule references missing item: " + item.getItemId());
                }
                ItemConfig itemConfig = snapshot.itemById.get(item.getItemId());
                if (!"material".equals(itemConfig.getType())) {
                    throw new IllegalStateException("Enhancement rule item is not material: " + item.getItemId());
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalStateException("Enhancement rule material quantity invalid: " + item.getItemId());
                }
            }
        }
    }

    private void validateActivityRefs(ConfigSnapshot snapshot) {
        for (ActivityConfig activity : snapshot.activities) {
            if (activity.getName() == null || activity.getName().trim().isEmpty()) {
                throw new IllegalStateException("Activity name is blank: " + activity.getId());
            }
            String status = activity.getStatus();
            if (!"active".equals(status) && !"upcoming".equals(status) && !"ended".equals(status)) {
                throw new IllegalStateException("Invalid activity status: " + activity.getId());
            }
            if (activity.getRewardGold() < 0) {
                throw new IllegalStateException("Invalid activity reward gold: " + activity.getId());
            }
            String targetView = activity.getTargetView();
            if (targetView != null && !targetView.trim().isEmpty() && !isValidActivityTargetView(targetView)) {
                throw new IllegalStateException("Invalid activity target view: " + activity.getId() + "/" + targetView);
            }
            if (activity.getRewardItems() == null) {
            } else {
                for (TaskRewardItemConfig item : activity.getRewardItems()) {
                    if (!snapshot.itemById.containsKey(item.getItemId())) {
                        throw new IllegalStateException("Activity references missing reward item: " + item.getItemId());
                    }
                    if (item.getQuantity() <= 0) {
                        throw new IllegalStateException("Activity reward item quantity invalid: " + activity.getId());
                    }
                }
            }
            if (activity.getEffects() != null) {
                for (ActivityEffectConfig effect : activity.getEffects()) {
                    if (effect == null || !isValidActivityEffectType(effect.getType())) {
                        throw new IllegalStateException("Invalid activity effect type: " + activity.getId());
                    }
                    if (effect.getPercent() <= 0 || effect.getPercent() > 500) {
                        throw new IllegalStateException("Invalid activity effect percent: " + activity.getId());
                    }
                }
            }
            if (activity.getRankingRewards() != null) {
                for (ActivityRankingRewardConfig rankingReward : activity.getRankingRewards()) {
                    if (!isValidRankingType(rankingReward.getRankingType())) {
                        throw new IllegalStateException("Invalid activity ranking type: " + activity.getId());
                    }
                    if (rankingReward.getMaxRank() <= 0 || rankingReward.getMaxRank() > 100) {
                        throw new IllegalStateException("Invalid activity ranking max rank: " + activity.getId());
                    }
                    if (rankingReward.getRewardGold() < 0) {
                        throw new IllegalStateException("Invalid activity ranking reward gold: " + activity.getId());
                    }
                    if (rankingReward.getRewardItems() != null) {
                        for (TaskRewardItemConfig item : rankingReward.getRewardItems()) {
                            if (!snapshot.itemById.containsKey(item.getItemId())) {
                                throw new IllegalStateException("Activity ranking reward references missing item: " + item.getItemId());
                            }
                            if (item.getQuantity() <= 0) {
                                throw new IllegalStateException("Activity ranking reward item quantity invalid: " + activity.getId());
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateGuildConfig(ConfigSnapshot snapshot) {
        GuildConfig config = snapshot.guildConfig == null ? new GuildConfig() : snapshot.guildConfig;
        Set<String> donationIds = new LinkedHashSet<String>();
        List<GuildDonationOptionConfig> donations = config.getDonations() == null
                ? Collections.<GuildDonationOptionConfig>emptyList()
                : config.getDonations();
        for (GuildDonationOptionConfig option : donations) {
            if (option.getId() == null || option.getId().trim().isEmpty()) {
                throw new IllegalStateException("Guild donation id is blank");
            }
            if (!donationIds.add(option.getId())) {
                throw new IllegalStateException("Duplicate guild donation id: " + option.getId());
            }
            if (option.getName() == null || option.getName().trim().isEmpty()) {
                throw new IllegalStateException("Guild donation name is blank: " + option.getId());
            }
            if (option.getGoldCost() <= 0 || option.getContribution() <= 0 || option.getDailyLimit() <= 0) {
                throw new IllegalStateException("Guild donation numeric fields invalid: " + option.getId());
            }
        }
        Set<String> shopIds = new LinkedHashSet<String>();
        List<GuildShopItemConfig> shopItems = config.getShopItems() == null
                ? Collections.<GuildShopItemConfig>emptyList()
                : config.getShopItems();
        for (GuildShopItemConfig shopItem : shopItems) {
            if (shopItem.getId() == null || shopItem.getId().trim().isEmpty()) {
                throw new IllegalStateException("Guild shop item id is blank");
            }
            if (!shopIds.add(shopItem.getId())) {
                throw new IllegalStateException("Duplicate guild shop item id: " + shopItem.getId());
            }
            if (!snapshot.itemById.containsKey(shopItem.getItemId())) {
                throw new IllegalStateException("Guild shop references missing item: " + shopItem.getItemId());
            }
            if (shopItem.getQuantity() <= 0 || shopItem.getDailyLimit() <= 0) {
                throw new IllegalStateException("Guild shop quantity or daily limit invalid: " + shopItem.getId());
            }
            if (shopItem.getContributionCost() < 0 || shopItem.getGoldCost() < 0 || shopItem.getMinContribution() < 0) {
                throw new IllegalStateException("Guild shop cost invalid: " + shopItem.getId());
            }
        }
        Set<String> activityIds = new LinkedHashSet<String>();
        List<GuildActivityConfig> activities = config.getActivities() == null
                ? Collections.<GuildActivityConfig>emptyList()
                : config.getActivities();
        for (GuildActivityConfig activity : activities) {
            if (activity.getId() == null || activity.getId().trim().isEmpty()) {
                throw new IllegalStateException("Guild activity id is blank");
            }
            if (!activityIds.add(activity.getId())) {
                throw new IllegalStateException("Duplicate guild activity id: " + activity.getId());
            }
            if (activity.getName() == null || activity.getName().trim().isEmpty()) {
                throw new IllegalStateException("Guild activity name is blank: " + activity.getId());
            }
            if (activity.getTargetContribution() <= 0 || activity.getRewardGold() < 0) {
                throw new IllegalStateException("Guild activity numeric fields invalid: " + activity.getId());
            }
            if (activity.getRewardItems() != null) {
                for (TaskRewardItemConfig rewardItem : activity.getRewardItems()) {
                    if (!snapshot.itemById.containsKey(rewardItem.getItemId())) {
                        throw new IllegalStateException("Guild activity references missing item: " + rewardItem.getItemId());
                    }
                    if (rewardItem.getQuantity() <= 0) {
                        throw new IllegalStateException("Guild activity reward quantity invalid: " + activity.getId());
                    }
                }
            }
        }
    }

    private boolean isValidRankingType(String type) {
        return "level".equals(type) || "power".equals(type) || "gold".equals(type);
    }

    private boolean isValidActivityEffectType(String type) {
        return "battle_exp".equals(type)
                || "battle_gold".equals(type)
                || "drop_rate".equals(type)
                || "idle_exp".equals(type)
                || "idle_gold".equals(type)
                || "world_boss_gold".equals(type);
    }

    private boolean isValidActivityTargetView(String view) {
        return "dashboard".equals(view)
                || "maps".equals(view)
                || "tasks".equals(view)
                || "idle".equals(view)
                || "bosses".equals(view)
                || "worldBosses".equals(view)
                || "inventory".equals(view)
                || "equipment".equals(view)
                || "skills".equals(view)
                || "talents".equals(view)
                || "mails".equals(view)
                || "activities".equals(view)
                || "rankings".equals(view);
    }

    private static class ConfigSnapshot {
        private List<MapConfig> maps;
        private Map<String, MapConfig> mapById;
        private Map<String, MonsterConfig> monsterById;
        private List<MonsterConfig> monsters;
        private Map<String, ItemConfig> itemById;
        private Map<Integer, LevelConfig> levelByLevel;
        private Map<String, DropConfig> monsterDropBySourceId;
        private List<TaskConfig> tasks;
        private Map<String, TaskConfig> taskById;
        private List<NpcConfig> npcs;
        private Map<String, NpcConfig> npcById;
        private List<MapEventConfig> mapEvents;
        private Map<String, MapEventConfig> mapEventById;
        private List<BossConfig> bosses;
        private Map<String, BossConfig> bossById;
        private List<WorldBossConfig> worldBosses;
        private Map<String, WorldBossConfig> worldBossById;
        private List<ClassConfig> classes;
        private Map<String, ClassConfig> classById;
        private List<SkillConfig> skills;
        private Map<String, SkillConfig> skillById;
        private List<TalentConfig> talents;
        private Map<String, TalentConfig> talentById;
        private List<EnhancementRuleConfig> enhancementRules;
        private Map<String, EquipmentAffixQualityConfig> equipmentAffixRuleByQuality;
        private List<ActivityConfig> activities;
        private Map<String, ActivityConfig> activityById;
        private GuildConfig guildConfig;

        private String summary() {
            return "maps=" + maps.size()
                    + ", monsters=" + monsterById.size()
                    + ", items=" + itemById.size()
                    + ", levels=" + levelByLevel.size()
                    + ", drops=" + monsterDropBySourceId.size()
                    + ", tasks=" + tasks.size()
                    + ", npcs=" + npcs.size()
                    + ", mapEvents=" + mapEvents.size()
                    + ", bosses=" + bosses.size()
                    + ", worldBosses=" + worldBosses.size()
                    + ", classes=" + classes.size()
                    + ", skills=" + skills.size()
                    + ", talents=" + talents.size()
                    + ", enhancementRules=" + enhancementRules.size()
                    + ", equipmentAffixRules=" + equipmentAffixRuleByQuality.size()
                    + ", activities=" + activities.size()
                    + ", guildDonations=" + (guildConfig == null || guildConfig.getDonations() == null ? 0 : guildConfig.getDonations().size())
                    + ", guildShopItems=" + (guildConfig == null || guildConfig.getShopItems() == null ? 0 : guildConfig.getShopItems().size())
                    + ", guildActivities=" + (guildConfig == null || guildConfig.getActivities() == null ? 0 : guildConfig.getActivities().size());
        }
    }
}
