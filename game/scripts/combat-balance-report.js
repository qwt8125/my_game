const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const configDir = path.join(root, "src", "main", "resources", "config");

function readJson(name) {
  return JSON.parse(fs.readFileSync(path.join(configDir, name), "utf8"));
}

const classes = readJson("classes.json");
const monsters = readJson("monsters.json");
const skills = readJson("skills.json");
const items = readJson("items.json");
const drops = readJson("drops.json");
const enhancementRules = readJson("enhancement-rules.json");
const equipmentAffixes = readJson("equipment-affixes.json");
const levels = readJson("levels.json");
const talents = readJson("talents.json");
const maps = readJson("maps.json");
const mapEvents = readJson("map-events.json");
const tasks = readJson("tasks.json");
const activities = readJson("activities.json");

const MAX_ROUNDS = 50;
const MAX_LEVEL = 10;
const EQUIPMENT_SLOTS = ["weapon", "armor", "helmet", "necklace", "bracelet", "ring", "boots"];
const DECOMPOSE_ESSENCE_IDS = ["mat_common_essence", "mat_fine_essence", "mat_rare_essence", "mat_epic_essence"];
const STRICT = process.env.STRICT_BALANCE === "1";

const monsterIds = new Set(monsters.map(item => item.id));
const monsterById = new Map(monsters.map(item => [item.id, item]));
const itemIds = new Set(items.map(item => item.id));
const itemById = new Map(items.map(item => [item.id, item]));
const affixRuleByQuality = new Map(equipmentAffixes.map(item => [item.quality, item]));
const mapIds = new Set(maps.map(item => item.id));
const taskIds = new Set(tasks.map(item => item.id));
const talentIds = new Set(talents.map(item => item.id));
const mapById = new Map(maps.map(item => [item.id, item]));
const validSkillTargetTypes = new Set(["single", "front_row", "back_row", "random3", "all"]);
const validAffixStats = new Set(["hp", "attack", "defense", "attackSpeed", "skillTriggerBonus"]);
const validActivityEffectTypes = new Set(["battle_exp", "battle_gold", "drop_rate", "idle_exp", "idle_gold", "world_boss_gold"]);
const validRankingTypes = new Set(["level", "power", "gold"]);
const equipmentDropRateCaps = {
  common: 0.08,
  fine: 0.06,
  rare: 0.05,
  epic: 0.04,
  legendary: 0.025
};

function byLevel(level) {
  return monsters.filter(monster => monster.level === level);
}

function skillLevel(characterLevel, skill) {
  if (characterLevel < skill.requiredLevel) return 0;
  return Math.min(skill.maxLevel || 1, 1 + Math.floor((characterLevel - skill.requiredLevel) / 2));
}

function buildCharacter(classConfig, level) {
  const result = {
    className: classConfig.id,
    hp: classConfig.hp + Math.max(0, level - 1) * classConfig.hpPerLevel,
    attack: classConfig.attack + Math.max(0, level - 1) * classConfig.attackPerLevel,
    defense: classConfig.defense + Math.max(0, level - 1) * classConfig.defensePerLevel,
    attackSpeed: classConfig.attackSpeed + Math.max(0, level - 1) * classConfig.attackSpeedPerLevel,
    activeSkills: []
  };

  for (const skill of skills.filter(item => item.className === classConfig.id)) {
    const levelValue = skillLevel(level, skill);
    if (!levelValue) continue;
    if (skill.type === "passive") {
      result.hp += (skill.passiveHp || 0) * levelValue;
      result.attack += (skill.passiveAttack || 0) * levelValue;
      result.defense += (skill.passiveDefense || 0) * levelValue;
      result.attackSpeed += (skill.passiveAttackSpeed || 0) * levelValue;
    } else {
      result.activeSkills.push({ skill, level: levelValue, cooldown: 0 });
    }
  }
  return result;
}

function activeSkillDamage(character, monster, cast) {
  const skill = cast.skill;
  const multiplier = (skill.damageMultiplier || 0) + Math.max(0, cast.level - 1) * (skill.levelDamageMultiplier || 0);
  const flat = (skill.flatDamage || 0) + Math.max(0, cast.level - 1) * (skill.levelFlatDamage || 0);
  const defense = Math.max(0, monster.defense - Math.max(0, (skill.defenseBreak || 0) * cast.level));
  return Math.max(1, Math.floor(character.attack * multiplier) + flat - defense);
}

function pickSkill(character) {
  for (const cast of character.activeSkills) {
    if (cast.cooldown <= 0) return cast;
  }
  return null;
}

function tickCooldowns(character) {
  for (const cast of character.activeSkills) {
    cast.cooldown = Math.max(0, cast.cooldown - 1);
  }
}

function simulate(classConfig, level, monster) {
  const character = buildCharacter(classConfig, level);
  let playerHp = character.hp;
  let monsterHp = monster.hp;
  let nextActor = character.attackSpeed >= monster.attackSpeed ? "player" : "monster";
  let rounds = 1;
  let skillCasts = 0;

  for (let action = 0; action < MAX_ROUNDS * 2; action++) {
    if (nextActor === "player") {
      tickCooldowns(character);
      const cast = pickSkill(character);
      let damage;
      if (cast) {
        damage = activeSkillDamage(character, monster, cast);
        cast.cooldown = Math.max(0, cast.skill.cooldownRounds || 0);
        skillCasts++;
      } else {
        damage = Math.max(1, character.attack - monster.defense);
      }
      monsterHp = Math.max(0, monsterHp - damage);
      if (monsterHp <= 0) {
        return { win: true, rounds, hpLeft: playerHp, skillCasts, character };
      }
      nextActor = "monster";
    } else {
      const damage = Math.max(1, monster.attack - character.defense);
      playerHp = Math.max(0, playerHp - damage);
      if (playerHp <= 0) {
        return { win: false, rounds, hpLeft: 0, skillCasts, character };
      }
      nextActor = "player";
      rounds++;
    }
    if (rounds > MAX_ROUNDS) {
      return { win: false, rounds: MAX_ROUNDS, hpLeft: playerHp, skillCasts, character };
    }
  }
  return { win: false, rounds: MAX_ROUNDS, hpLeft: playerHp, skillCasts, character };
}

function combatStatus(level, result) {
  if (!result.win) return "FAIL";
  const hpRate = result.hpLeft / Math.max(1, result.character.hp);
  if (level <= 3 && hpRate < 0.25) return "LOW_HP";
  if (result.rounds >= 18) return "SLOW";
  if (result.rounds <= 1 && level >= 4) return "BURST";
  return "OK";
}

function addIssue(list, severity, area, id, message) {
  list.push({ severity, area, id, message });
}

function validateNoPlaceholders(issues, area, id, value, field) {
  if (typeof value === "string" && value.includes("???")) {
    addIssue(issues, "ERROR", area, id, `${field} contains placeholder question marks`);
  }
}

function validateReferences() {
  const issues = [];

  for (const item of items) {
    validateNoPlaceholders(issues, "item", item.id, item.name, "name");
    if (item.setId && !item.setName) {
      addIssue(issues, "ERROR", "equipment-set", item.id, "set item must define setName");
    }
    if (item.setName && !item.setId) {
      addIssue(issues, "ERROR", "equipment-set", item.id, "set item must define setId");
    }
    if (item.type === "equipment" && !affixRuleByQuality.has(item.quality)) {
      addIssue(issues, "ERROR", "equipment-affix", item.id, `missing affix rule for quality ${item.quality}`);
    }
    for (const bonus of item.setBonuses || []) {
      if (!item.setId) {
        addIssue(issues, "ERROR", "equipment-set", item.id, "setBonuses require setId");
      }
      if (!Number.isInteger(bonus.pieces) || bonus.pieces <= 1) {
        addIssue(issues, "ERROR", "equipment-set", `${item.id}:${bonus.pieces}`, "set bonus pieces must be greater than 1");
      }
      const totalStats = (bonus.hp || 0) + (bonus.attack || 0) + (bonus.defense || 0) + (bonus.attackSpeed || 0);
      if (totalStats <= 0) {
        addIssue(issues, "ERROR", "equipment-set", `${item.id}:${bonus.pieces}`, "set bonus must provide at least one positive stat");
      }
    }
  }

  for (const rule of equipmentAffixes) {
    if (!rule.quality) {
      addIssue(issues, "ERROR", "equipment-affix", "blank", "affix rule quality is required");
    }
    if (!Number.isInteger(rule.affixCount) || rule.affixCount <= 0) {
      addIssue(issues, "ERROR", "equipment-affix", rule.quality, "affixCount must be a positive integer");
    }
    const material = itemById.get(rule.rerollMaterialId);
    if (!material) {
      addIssue(issues, "ERROR", "equipment-affix", `${rule.quality}:${rule.rerollMaterialId}`, "reroll material does not exist");
    } else if (material.type !== "material") {
      addIssue(issues, "ERROR", "equipment-affix", `${rule.quality}:${rule.rerollMaterialId}`, "reroll cost item is not a material");
    }
    if (!Number.isInteger(rule.rerollMaterialQuantity) || rule.rerollMaterialQuantity <= 0) {
      addIssue(issues, "ERROR", "equipment-affix", rule.quality, "rerollMaterialQuantity must be positive");
    }
    let totalWeight = 0;
    for (const stat of rule.stats || []) {
      if (!validAffixStats.has(stat.stat)) {
        addIssue(issues, "ERROR", "equipment-affix", `${rule.quality}:${stat.stat}`, "affix stat is invalid");
      }
      if (!(stat.min > 0) || stat.max < stat.min || !(stat.weight > 0)) {
        addIssue(issues, "ERROR", "equipment-affix", `${rule.quality}:${stat.stat}`, "affix range and weight must be positive");
      }
      if (stat.stat === "skillTriggerBonus" && stat.max > 0.2) {
        addIssue(issues, "ERROR", "equipment-affix", `${rule.quality}:${stat.stat}`, "skill trigger bonus is too high");
      }
      totalWeight += Math.max(0, stat.weight || 0);
    }
    if (!Array.isArray(rule.stats) || rule.stats.length < rule.affixCount || totalWeight <= 0) {
      addIssue(issues, "ERROR", "equipment-affix", rule.quality, "affix pool cannot satisfy affixCount");
    }
  }

  for (const itemId of DECOMPOSE_ESSENCE_IDS) {
    const material = itemById.get(itemId);
    if (!material) {
      addIssue(issues, "ERROR", "decompose", itemId, "decompose essence material does not exist");
    } else if (material.type !== "material") {
      addIssue(issues, "ERROR", "decompose", itemId, "decompose essence item is not a material");
    }
  }

  for (const skill of skills) {
    const targetType = typeof skill.targetType === "string" ? skill.targetType.trim() : "";
    if (targetType && !validSkillTargetTypes.has(targetType)) {
      addIssue(issues, "ERROR", "skill", `${skill.id}:${targetType}`, "skill targetType is invalid");
    }
  }

  for (const monster of monsters) {
    validateNoPlaceholders(issues, "monster", monster.id, monster.name, "name");
  }

  for (const drop of drops) {
    if (drop.sourceType === "monster" && !monsterIds.has(drop.sourceId)) {
      addIssue(issues, "ERROR", "drop", drop.sourceId, "drop source monster does not exist");
    }
    const sourceMonster = monsterById.get(drop.sourceId);
    for (const item of drop.items || []) {
      if (!itemIds.has(item.itemId)) {
        addIssue(issues, "ERROR", "drop", `${drop.sourceId}:${item.itemId}`, "drop item does not exist");
      }
      const itemConfig = itemById.get(item.itemId);
      if (sourceMonster && itemConfig && itemConfig.type === "equipment") {
        if (itemConfig.requiredLevel > sourceMonster.level + 2) {
          addIssue(issues, "WARN", "drop-tier", `${drop.sourceId}:${item.itemId}`, "equipment requiredLevel is more than 2 above monster level");
        }
        const cap = equipmentDropRateCaps[itemConfig.quality] || 0.05;
        if (item.rate > cap) {
          addIssue(issues, "WARN", "drop-quality", `${drop.sourceId}:${item.itemId}`, `equipment drop rate exceeds ${itemConfig.quality} cap ${cap}`);
        }
      }
      if (item.rate <= 0 || item.rate > 1) {
        addIssue(issues, "ERROR", "drop", `${drop.sourceId}:${item.itemId}`, "drop rate must be in (0,1]");
      }
      if (item.minQuantity <= 0 || item.maxQuantity < item.minQuantity) {
        addIssue(issues, "ERROR", "drop", `${drop.sourceId}:${item.itemId}`, "drop quantity range is invalid");
      }
    }
  }

  for (const rule of enhancementRules) {
    for (const cost of rule.materialCosts || []) {
      const material = items.find(item => item.id === cost.itemId);
      if (!material) {
        addIssue(issues, "ERROR", "enhancement", cost.itemId, "enhancement material does not exist");
      } else if (material.type !== "material") {
        addIssue(issues, "ERROR", "enhancement", cost.itemId, "enhancement cost item is not a material");
      }
      if (cost.quantity <= 0) {
        addIssue(issues, "ERROR", "enhancement", cost.itemId, "enhancement material quantity must be positive");
      }
    }
  }

  for (const talent of talents) {
    if (talent.preTalentId && !talentIds.has(talent.preTalentId)) {
      addIssue(issues, "ERROR", "talent", talent.id, `preTalentId ${talent.preTalentId} does not exist`);
    }
    const preTalent = talents.find(item => item.id === talent.preTalentId);
    if (preTalent && talent.preTalentLevel > preTalent.maxLevel) {
      addIssue(issues, "ERROR", "talent", talent.id, "preTalentLevel exceeds previous talent maxLevel");
    }
    if (talent.requiredLevel > MAX_LEVEL) {
      addIssue(issues, "WARN", "talent", talent.id, "requiredLevel is above current 1-10 progression band");
    }
  }

  for (const map of maps) {
    validateNoPlaceholders(issues, "map", map.id, map.name, "name");
    for (const monsterId of map.monsterIds || []) {
      if (!monsterIds.has(monsterId)) {
        addIssue(issues, "ERROR", "map", `${map.id}:${monsterId}`, "map monster does not exist");
      }
    }
  }

  for (const task of tasks) {
    validateNoPlaceholders(issues, "task", task.id, task.name, "name");
    validateNoPlaceholders(issues, "task", task.id, task.story, "story");
    validateNoPlaceholders(issues, "task", task.id, task.guide, "guide");
    for (const preTaskId of task.preTaskIds || []) {
      if (!taskIds.has(preTaskId)) {
        addIssue(issues, "ERROR", "task", `${task.id}:${preTaskId}`, "task prerequisite does not exist");
      }
    }
    if (task.type === "kill_monster" && !monsterIds.has(task.targetId)) {
      addIssue(issues, "ERROR", "task", `${task.id}:${task.targetId}`, "task target monster does not exist");
    }
    if (task.type === "explore_event") {
      const eventExists = mapEvents.some(event => event.id === task.targetId);
      if (!eventExists) {
        addIssue(issues, "ERROR", "task", `${task.id}:${task.targetId}`, "task target event does not exist");
      }
    }
    for (const reward of ((task.rewards || {}).items || [])) {
      if (!itemIds.has(reward.itemId)) {
        addIssue(issues, "ERROR", "task", `${task.id}:${reward.itemId}`, "task reward item does not exist");
      }
    }
  }

  for (const activity of activities) {
    validateNoPlaceholders(issues, "activity", activity.id, activity.name, "name");
    if (!["active", "upcoming", "ended"].includes(activity.status)) {
      addIssue(issues, "ERROR", "activity", activity.id, "activity status is invalid");
    }
    for (const reward of activity.rewardItems || []) {
      if (!itemIds.has(reward.itemId)) {
        addIssue(issues, "ERROR", "activity", `${activity.id}:${reward.itemId}`, "activity reward item does not exist");
      }
      if (reward.quantity <= 0) {
        addIssue(issues, "ERROR", "activity", `${activity.id}:${reward.itemId}`, "activity reward quantity must be positive");
      }
    }
    for (const effect of activity.effects || []) {
      if (!validActivityEffectTypes.has(effect.type)) {
        addIssue(issues, "ERROR", "activity-effect", `${activity.id}:${effect.type}`, "activity effect type is invalid");
      }
      if (effect.percent <= 0 || effect.percent > 500) {
        addIssue(issues, "ERROR", "activity-effect", `${activity.id}:${effect.type}`, "activity effect percent must be 1-500");
      }
    }
    for (const reward of activity.rankingRewards || []) {
      if (!validRankingTypes.has(reward.rankingType)) {
        addIssue(issues, "ERROR", "activity-ranking", `${activity.id}:${reward.rankingType}`, "activity ranking type is invalid");
      }
      if (reward.maxRank <= 0 || reward.maxRank > 100) {
        addIssue(issues, "ERROR", "activity-ranking", `${activity.id}:${reward.maxRank}`, "activity ranking maxRank must be 1-100");
      }
      if (reward.rewardGold < 0) {
        addIssue(issues, "ERROR", "activity-ranking", `${activity.id}:${reward.rewardGold}`, "activity ranking reward gold cannot be negative");
      }
      for (const item of reward.rewardItems || []) {
        if (!itemIds.has(item.itemId)) {
          addIssue(issues, "ERROR", "activity-ranking", `${activity.id}:${item.itemId}`, "activity ranking reward item does not exist");
        }
        if (item.quantity <= 0) {
          addIssue(issues, "ERROR", "activity-ranking", `${activity.id}:${item.itemId}`, "activity ranking reward quantity must be positive");
        }
      }
    }
  }

  for (const event of mapEvents) {
    validateNoPlaceholders(issues, "map-event", event.id, event.name, "name");
    validateNoPlaceholders(issues, "map-event", event.id, event.dialogue, "dialogue");
    const eventMap = mapById.get(event.mapId);
    if (!mapIds.has(event.mapId)) {
      addIssue(issues, "ERROR", "map-event", event.id, `mapId ${event.mapId} does not exist`);
    }
    if (event.targetMapId && !mapIds.has(event.targetMapId)) {
      addIssue(issues, "ERROR", "map-event", event.id, `targetMapId ${event.targetMapId} does not exist`);
    }
    for (const monsterId of event.targetMonsterIds || []) {
      if (!monsterIds.has(monsterId)) {
        addIssue(issues, "ERROR", "map-event", `${event.id}:${monsterId}`, "event target monster does not exist");
      }
    }
    if (event.type === "monster_area" || event.type === "random_encounter") {
      const minCount = event.encounterMinCount == null ? 1 : event.encounterMinCount;
      const maxCount = event.encounterMaxCount == null ? 3 : event.encounterMaxCount;
      const eliteChance = event.encounterEliteChance == null ? 0.18 : event.encounterEliteChance;
      const interval = event.encounterIntervalSeconds == null ? 5 : event.encounterIntervalSeconds;
      let totalWeight = 0;
      for (const encounterMonster of event.encounterMonsters || []) {
        if (!monsterIds.has(encounterMonster.monsterId)) {
          addIssue(issues, "ERROR", "map-event", `${event.id}:${encounterMonster.monsterId}`, "encounter monster does not exist");
        }
        if (eventMap && !(eventMap.monsterIds || []).includes(encounterMonster.monsterId)) {
          addIssue(issues, "ERROR", "map-event", `${event.id}:${encounterMonster.monsterId}`, "encounter monster is not in event map");
        }
        if (encounterMonster.weight <= 0) {
          addIssue(issues, "ERROR", "map-event", `${event.id}:${encounterMonster.monsterId}`, "encounter monster weight must be positive");
        }
        totalWeight += Math.max(0, encounterMonster.weight || 0);
      }
      if ((event.encounterMonsters || []).length && totalWeight <= 0) {
        addIssue(issues, "ERROR", "map-event", event.id, "encounter monster total weight must be positive");
      }
      if (minCount <= 0 || maxCount < minCount || maxCount > 5) {
        addIssue(issues, "ERROR", "map-event", event.id, "encounter count range is invalid");
      }
      if (eliteChance < 0 || eliteChance > 0.6) {
        addIssue(issues, "ERROR", "map-event", event.id, "encounter elite chance must be in [0,0.6]");
      }
      if (interval < 2 || interval > 30) {
        addIssue(issues, "ERROR", "map-event", event.id, "encounter interval must be 2-30 seconds");
      }
    }
    for (const taskId of event.requiredTaskIds || []) {
      if (!taskIds.has(taskId)) {
        addIssue(issues, "ERROR", "map-event", `${event.id}:${taskId}`, "event required task does not exist");
      }
    }
    for (const reward of event.rewardItems || []) {
      if (!itemIds.has(reward.itemId)) {
        addIssue(issues, "ERROR", "map-event", `${event.id}:${reward.itemId}`, "event reward item does not exist");
      }
    }
  }

  return issues;
}

function validateProgression() {
  const issues = [];
  const equipment = items.filter(item => item.type === "equipment");
  const consumables = items.filter(item => item.type === "consumable");

  for (let level = 1; level <= MAX_LEVEL; level++) {
    const levelConfig = levels.find(item => item.level === level);
    if (!levelConfig) {
      addIssue(issues, "ERROR", "level", String(level), "level config missing");
    }
    if (!byLevel(level).length) {
      addIssue(issues, level <= 8 ? "ERROR" : "WARN", "monster", String(level), "no same-level monster configured");
    }
    for (const slot of EQUIPMENT_SLOTS) {
      const available = equipment.some(item => item.slot === slot && item.requiredLevel <= level);
      if (!available && level >= 3) {
        addIssue(issues, "WARN", "equipment", `${slot}@${level}`, "slot has no available item by this level");
      }
    }
    const hasConsumable = consumables.some(item => item.requiredLevel <= level);
    if (!hasConsumable) {
      addIssue(issues, "WARN", "consumable", String(level), "no consumable available by this level");
    }
  }

  for (const item of equipment) {
    const hasDrop = drops.some(drop => (drop.items || []).some(dropItem => dropItem.itemId === item.id));
    if (!hasDrop) {
      addIssue(issues, "WARN", "equipment", item.id, "equipment has no drop source");
    }
  }

  return issues;
}

function printCombatReport() {
  const lines = [];
  const combatWarnings = [];
  lines.push("class,level,monster,result,rounds,hpLeft,maxHp,skillCasts,warning");
  for (const classConfig of classes) {
    for (let level = 1; level <= MAX_LEVEL; level++) {
      for (const monster of byLevel(level)) {
        const result = simulate(classConfig, level, monster);
        const status = combatStatus(level, result);
        if (status !== "OK") {
          combatWarnings.push({ severity: status === "FAIL" ? "ERROR" : "WARN", area: "combat", id: `${classConfig.id}@${level}:${monster.id}`, message: status });
        }
        lines.push([
          classConfig.id,
          level,
          monster.id,
          result.win ? "win" : "lose",
          result.rounds,
          result.hpLeft,
          result.character.hp,
          result.skillCasts,
          status
        ].join(","));
      }
    }
  }
  console.log(lines.join("\n"));
  return combatWarnings;
}

function printIssues(issues) {
  console.log("\nseverity,area,id,message");
  if (!issues.length) {
    console.log("OK,summary,all,config health checks passed");
    return;
  }
  for (const issue of issues) {
    console.log([issue.severity, issue.area, issue.id, issue.message].join(","));
  }
}

function main() {
  const combatIssues = printCombatReport();
  const issues = [
    ...combatIssues,
    ...validateReferences(),
    ...validateProgression()
  ];
  printIssues(issues);

  const errorCount = issues.filter(issue => issue.severity === "ERROR").length;
  const warnCount = issues.filter(issue => issue.severity === "WARN").length;
  console.error(`Balance health summary: errors=${errorCount}, warnings=${warnCount}, strict=${STRICT ? "on" : "off"}`);
  if (errorCount > 0 || (STRICT && warnCount > 0)) {
    process.exitCode = 2;
  }
}

main();
