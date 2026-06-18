const { spawn, spawnSync } = require("child_process");

const DEFAULT_PORT = "8081";
const PASSWORD = process.env.SMOKE_PASSWORD || "SmokePass123";
const ADMIN_PASSWORD = process.env.SMOKE_ADMIN_PASSWORD || PASSWORD;
const START_SERVER = process.env.SMOKE_START_SERVER === "1";
const SKIP_ADMIN = process.env.SMOKE_SKIP_ADMIN === "1";
const SERVER_PORT = process.env.SMOKE_PORT || DEFAULT_PORT;
const DEFAULT_BASE_URL = START_SERVER ? `http://localhost:${SERVER_PORT}` : "http://localhost:8080";
const BASE_URL = (process.env.BASE_URL || DEFAULT_BASE_URL).replace(/\/+$/, "");
const RUN_ID = Date.now().toString(36);

let serverProcess = null;

function log(message) {
  console.log(`[smoke] ${message}`);
}

function fail(message) {
  throw new Error(message);
}

function assert(condition, message) {
  if (!condition) fail(message);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method || "GET",
    headers: {
      "Content-Type": "application/json",
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {})
    },
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });

  const text = await response.text();
  let payload = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch (ex) {
    fail(`${path} returned non-JSON response: ${text.slice(0, 160)}`);
  }

  if (!response.ok || !payload || payload.success !== true) {
    const code = payload && payload.code ? payload.code : response.status;
    const message = payload && payload.message ? payload.message : text;
    const error = new Error(`${path} failed: ${code} ${message}`);
    error.code = code;
    error.payload = payload;
    throw error;
  }
  return payload.data;
}

async function optionalRegister(username, password) {
  try {
    await request("/api/auth/register", {
      method: "POST",
      body: { username, password }
    });
    return "registered";
  } catch (ex) {
    if (ex.code === "AUTH_USERNAME_EXISTS") return "exists";
    throw ex;
  }
}

async function login(username, password) {
  const data = await request("/api/auth/login", {
    method: "POST",
    body: { username, password }
  });
  assert(data && data.token, `login did not return token for ${username}`);
  return data.token;
}

async function registerOrLogin(username, password) {
  const state = await optionalRegister(username, password);
  const token = await login(username, password);
  log(`${username} ${state}; logged in`);
  return token;
}

async function ensureCharacter(token, nickname, className) {
  try {
    const created = await request("/api/characters", {
      method: "POST",
      token,
      body: { nickname, className }
    });
    assert(created.characterId > 0, "character create did not return characterId");
    return await request("/api/characters/me", { token });
  } catch (ex) {
    if (ex.code !== "CHARACTER_ALREADY_CREATED" && ex.code !== "CHARACTER_NICKNAME_EXISTS") {
      throw ex;
    }
    return await request("/api/characters/me", { token });
  }
}

async function waitForHealth() {
  for (let attempt = 1; attempt <= 90; attempt++) {
    try {
      const data = await request("/api/health");
      if (data && data.status === "UP") return;
    } catch (ex) {
      await sleep(1000);
    }
  }
  fail(`server did not become healthy at ${BASE_URL}`);
}

function startServerIfRequested() {
  if (!START_SERVER) return;
  const mvn = process.platform === "win32" ? "mvn.cmd" : "mvn";
  const dbUrl = `jdbc:sqlite:./data/smoke-test-${RUN_ID}.db`;
  let command = mvn;
  let args = ["spring-boot:run"];
  log(`starting Spring Boot on ${BASE_URL} with ${dbUrl}`);
  serverProcess = spawn(command, args, {
    cwd: process.cwd(),
    stdio: ["ignore", "pipe", "pipe"],
    shell: process.platform === "win32",
    env: {
      ...process.env,
      SERVER_PORT,
      GAME_BATTLE_SESSION_STORE: "sqlite",
      SPRING_DATASOURCE_URL: dbUrl
    }
  });
  serverProcess.stdout.on("data", chunk => process.stdout.write(chunk));
  serverProcess.stderr.on("data", chunk => process.stderr.write(chunk));
}

async function verifyCoreFlow(playerToken, adminToken) {
  const classes = await request("/api/characters/classes", { token: playerToken });
  assert(Array.isArray(classes) && classes.some(item => item.id === "warrior"), "classes missing warrior");

  const character = await ensureCharacter(playerToken, `S${RUN_ID}`, "warrior");
  assert(character.id > 0 && character.level >= 1, "character profile is invalid");
  log(`player character ready: #${character.id}`);

  const maps = await request("/api/maps", { token: playerToken });
  assert(Array.isArray(maps) && maps.some(item => item.id === "map_novice_field"), "maps missing novice field");

  const scene = await request("/api/maps/map_novice_field/scene", { token: playerToken });
  assert(scene.id === "map_novice_field", "scene id mismatch");
  assert(Array.isArray(scene.monsters) && scene.monsters.length > 0, "scene has no monsters");
  assert(Array.isArray(scene.events) && scene.events.length > 0, "scene has no events");
  assert(scene.player && Number.isFinite(scene.player.x) && Number.isFinite(scene.player.y), "scene player position missing");
  log("map scene and point data ok");

  const tasks = await request("/api/tasks", { token: playerToken });
  assert(Array.isArray(tasks), "tasks response is not a list");
  const firstBloodTask = tasks.find(item => item.id === "task_first_blood");
  const firstWeaponTask = tasks.find(item => item.id === "task_first_weapon");
  assert(firstBloodTask && firstBloodTask.story && firstBloodTask.guide, "task story and guide are required for replay");
  assert(firstBloodTask.targetMapId && firstBloodTask.targetPointId, "task target location is required for guidance");
  assert(firstWeaponTask && Array.isArray(firstWeaponTask.preTaskIds) && firstWeaponTask.preTaskIds.includes("task_first_blood"), "task chain prerequisite is missing");

  const skills = await request("/api/skills", { token: playerToken });
  assert(Array.isArray(skills) && skills.length > 0, "skills list is empty");

  const talents = await request("/api/talents", { token: playerToken });
  assert(talents && Array.isArray(talents.talents), "talents response invalid");

  const activities = await request("/api/activities", { token: playerToken });
  assert(Array.isArray(activities) && activities.some(item => item.id === "activity_growth_sprint"), "activities missing growth sprint");
  assert(activities.every(item => item.status && item.name), "activity metadata incomplete");
  const growthActivity = activities.find(item => item.id === "activity_growth_sprint");
  assert(Array.isArray(growthActivity.effects) && growthActivity.effects.some(item => item.type === "battle_exp" && item.percent === 20), "growth activity battle exp effect missing");
  assert(growthActivity.claimable === true && growthActivity.claimed === false, "growth activity should be claimable before claim");
  const activityClaim = await request("/api/activities/activity_growth_sprint/claim", {
    method: "POST",
    token: playerToken
  });
  assert(activityClaim.goldGained === 300, "activity claim gold mismatch");
  assert(Array.isArray(activityClaim.items) && activityClaim.items.some(item => item.itemId === "consumable_hero_elixir"), "activity claim item missing");
  const activitiesAfterClaim = await request("/api/activities", { token: playerToken });
  const claimedGrowthActivity = activitiesAfterClaim.find(item => item.id === "activity_growth_sprint");
  assert(claimedGrowthActivity && claimedGrowthActivity.claimed === true && claimedGrowthActivity.claimable === false, "growth activity claim state was not persisted");
  const rankingActivity = activitiesAfterClaim.find(item => item.id === "activity_ranking_trial");
  assert(rankingActivity && Array.isArray(rankingActivity.rankingRewards), "ranking activity reward metadata missing");
  assert(rankingActivity.rankingRewards.some(item => item.rankingType === "level" && item.currentRank > 0 && item.eligible === true), "ranking activity eligibility missing");
  assert(rankingActivity.claimable === true, "ranking activity should be claimable for smoke player");
  const rankingClaim = await request("/api/activities/activity_ranking_trial/claim", {
    method: "POST",
    token: playerToken
  });
  assert(rankingClaim.goldGained === 500 && rankingClaim.rankingType === "level" && rankingClaim.currentRank > 0, "ranking activity claim mismatch");
  assert(Array.isArray(rankingClaim.items) && rankingClaim.items.some(item => item.itemId === "mat_rare_essence"), "ranking activity item missing");
  try {
    await request("/api/activities/activity_growth_sprint/claim", {
      method: "POST",
      token: playerToken
    });
    fail("duplicate activity claim should fail");
  } catch (ex) {
    assert(ex.code === "ACTIVITY_ALREADY_CLAIMED", "duplicate activity claim returned unexpected error");
  }

  await request("/api/rankings/level?limit=5", { token: playerToken });
  await request("/api/rankings/power?limit=5", { token: playerToken });
  const rankingSnapshot = await request("/api/rankings/level/snapshot?limit=5", { token: playerToken });
  assert(rankingSnapshot && rankingSnapshot.type === "level", "ranking snapshot type mismatch");
  assert(Array.isArray(rankingSnapshot.entries), "ranking snapshot entries missing");
  assert(rankingSnapshot.generatedAt && rankingSnapshot.nextRefreshAt, "ranking snapshot refresh metadata missing");
  log("task, skill, talent, activity and ranking endpoints ok");

  const battle = await request("/api/battles/start", {
    method: "POST",
    token: playerToken,
    body: { mapId: "map_novice_field", monsterId: "monster_chicken" }
  });
  assert(battle.battleId > 0 && battle.status === "running", "battle did not start");

  let current = battle;
  for (let i = 0; i < 30 && current.status === "running"; i++) {
    current = await request(`/api/battles/${battle.battleId}/next`, {
      method: "POST",
      token: playerToken
    });
  }
  assert(current.status === "finished", "battle did not finish within 30 turns");
  assert(current.win === true, "smoke player should beat novice chicken");
  assert(current.bonusExp > 0 && current.bonusGold > 0, "active activity battle bonus was not applied");
  log(`battle finished: level ${current.levelBefore}->${current.levelAfter}, gold +${current.goldGained}`);

  const encounter = await request("/api/battles/encounter/start", {
    method: "POST",
    token: playerToken,
    body: {
      mapId: "map_novice_field",
      monsterIds: ["monster_chicken", "monster_deer"],
      minCount: 2,
      maxCount: 2,
      eliteChance: 0
    }
  });
  assert(encounter.battleId > 0 && encounter.status === "running", "encounter battle did not start");
  assert(encounter.monster && encounter.monster.maxHp > 28, "encounter battle did not scale monster group");
  assert(Array.isArray(encounter.enemies) && encounter.enemies.length === 2, "encounter did not expose enemy lineup");
  const selectedEnemyId = encounter.enemies[1].id;
  current = encounter;
  current = await request(`/api/battles/${encounter.battleId}/next`, {
    method: "POST",
    token: playerToken,
    body: { targetId: selectedEnemyId }
  });
  assert(current.action && current.action.targets.some(item => item.targetId === selectedEnemyId), "selected target was not used by basic attack");
  for (let i = 0; i < 60 && current.status === "running"; i++) {
    current = await request(`/api/battles/${encounter.battleId}/next`, {
      method: "POST",
      token: playerToken
    });
  }
  assert(current.status === "finished", "encounter battle did not finish within 60 turns");
  assert(current.actions.some(action => Array.isArray(action.targets) && action.targets.length > 0), "battle actions did not expose target details");
  log(`encounter finished: ${encounter.monster.name}`);

  if (!SKIP_ADMIN) {
    await request("/api/admin/config/reload", { method: "POST", token: adminToken });
    const adminActivities = await request("/api/admin/activities", { token: adminToken });
    assert(Array.isArray(adminActivities) && adminActivities.length > 0, "admin activities list is empty");
    const editableActivity = adminActivities.find(item => item.id === "activity_redmoon_hunt") || adminActivities[0];
    const originalTag = editableActivity.tag || "";
    const editedActivity = {
      ...editableActivity,
      tag: `${originalTag || "测试"}-smoke`
    };
    const activitySave = await request(`/api/admin/activities/${editableActivity.id}`, {
      method: "POST",
      token: adminToken,
      body: editedActivity
    });
    assert(activitySave && activitySave.success === true, "admin activity update did not report success");
    const activitiesAfterEdit = await request("/api/admin/activities", { token: adminToken });
    const edited = activitiesAfterEdit.find(item => item.id === editableActivity.id);
    assert(edited && edited.tag === editedActivity.tag, "admin activity update was not applied");
    const activityRestore = await request(`/api/admin/activities/${editableActivity.id}`, {
      method: "POST",
      token: adminToken,
      body: editableActivity
    });
    assert(activityRestore && activityRestore.success === true, "admin activity restore did not report success");
    await request("/api/admin/grant-item", {
      method: "POST",
      token: adminToken,
      body: { characterId: character.id, itemId: "item_wood_sword", quantity: 1 }
    });
    await request("/api/admin/grant-item", {
      method: "POST",
      token: adminToken,
      body: { characterId: character.id, itemId: "item_cloth_armor", quantity: 1 }
    });

    const inventoryBefore = await request("/api/inventory", { token: playerToken });
    const sword = inventoryBefore.items.find(item => item.itemId === "item_wood_sword" && item.type === "equipment");
    const armor = inventoryBefore.items.find(item => item.itemId === "item_cloth_armor" && item.type === "equipment");
    assert(sword, "GM granted sword not found in inventory");
    assert(armor, "GM granted armor not found in inventory");
    assert(Array.isArray(sword.affixes) && sword.affixes.length > 0, "GM granted sword did not roll affixes");
    assert(Array.isArray(armor.affixes) && armor.affixes.length > 0, "GM granted armor did not roll affixes");

    const equipment = await request("/api/equipment/equip", {
      method: "POST",
      token: playerToken,
      body: { inventoryItemId: sword.id }
    });
    assert(equipment.items.some(item => item.itemId === "item_wood_sword" && item.slot === "weapon"), "equipped sword missing from equipment");

    const inventoryAfter = await request("/api/inventory", { token: playerToken });
    assert(!inventoryAfter.items.some(item => item.id === sword.id), "equipped item should not remain in backpack");

    const decomposed = await request("/api/equipment/decompose", {
      method: "POST",
      token: playerToken,
      body: { inventoryItemId: armor.id }
    });
    assert(decomposed.materials.some(item => item.itemId === "mat_common_essence" && item.quantity >= 1), "decompose did not return common essence");
    const inventoryAfterDecompose = await request("/api/inventory", { token: playerToken });
    assert(!inventoryAfterDecompose.items.some(item => item.id === armor.id), "decomposed equipment should leave backpack");
    assert(inventoryAfterDecompose.items.some(item => item.itemId === "mat_common_essence" && item.type === "material"), "decompose essence missing from inventory");

    const rerolled = await request("/api/equipment/reroll-affixes", {
      method: "POST",
      token: playerToken,
      body: { inventoryItemId: sword.id }
    });
    assert(Array.isArray(rerolled.affixes) && rerolled.affixes.length > 0, "affix reroll did not return affixes");
    assert(rerolled.materialCost && rerolled.materialCost.itemId === "mat_common_essence", "affix reroll did not consume common essence");
    const equipmentAfterReroll = await request("/api/equipment", { token: playerToken });
    const equippedSword = equipmentAfterReroll.items.find(item => item.inventoryItemId === sword.id);
    assert(equippedSword && Array.isArray(equippedSword.affixes) && equippedSword.affixes.length > 0, "equipped item did not expose rerolled affixes");
    const inventoryAfterReroll = await request("/api/inventory", { token: playerToken });
    assert(!inventoryAfterReroll.items.some(item => item.itemId === "mat_common_essence"), "affix reroll should consume the decomposed essence");
    log("admin activity edit, reload, GM grant, affix roll, equip, decomposition and affix reroll ok");
  }
}

async function verifyGuildFlow(leaderToken, memberToken) {
  const leader = await request("/api/characters/me", { token: leaderToken });
  const member = await ensureCharacter(memberToken, `G${RUN_ID}`, "taoist");
  const initial = await request("/api/guilds/me", { token: leaderToken });
  assert(initial && initial.inGuild === false, "fresh smoke leader should not already be in a guild");

  const guildName = `烟火${RUN_ID.slice(-8)}`;
  const created = await request("/api/guilds", {
    method: "POST",
    token: leaderToken,
    body: { name: guildName, notice: "smoke 公会闭环" }
  });
  assert(created && created.guild && created.guild.inGuild === true, "guild create did not return current guild");
  assert(created.guild.myRole === "leader" && created.guild.memberCount === 1, "guild creator should be leader");
  assert(Array.isArray(created.guild.donationOptions) && created.guild.donationOptions.some(item => item.id === "small_gold"), "guild donation options missing");
  assert(Array.isArray(created.guild.shopItems) && created.guild.shopItems.some(item => item.id === "guild_potion_pack"), "guild shop items missing");

  const donated = await request("/api/guilds/donate", {
    method: "POST",
    token: leaderToken,
    body: { donationId: "small_gold" }
  });
  assert(donated.guild && donated.guild.myContribution === 20 && donated.guild.totalContribution === 20, "guild donation did not add contribution");
  const donatedOption = donated.guild.donationOptions.find(item => item.id === "small_gold");
  assert(donatedOption && donatedOption.dailyUsed === 1 && donatedOption.remainingTimes === 2, "guild donation daily count mismatch");

  const shop = await request("/api/guilds/shop", { token: leaderToken });
  const potionPack = shop.find(item => item.id === "guild_potion_pack");
  assert(potionPack && potionPack.canBuy === false, "guild shop should require enough contribution for potion pack after one small donation");
  await request("/api/guilds/donate", {
    method: "POST",
    token: leaderToken,
    body: { donationId: "small_gold" }
  });
  const guildActivities = await request("/api/guilds/activities", { token: leaderToken });
  const dailySupport = guildActivities.find(item => item.id === "guild_daily_support");
  assert(dailySupport && dailySupport.achieved === true && dailySupport.claimable === true, "guild activity should be claimable after reaching contribution target");
  assert(dailySupport.currentContribution === 40 && dailySupport.progressPercent === 100, "guild activity progress mismatch");
  const guildRankings = await request("/api/guilds/rankings?limit=10", { token: leaderToken });
  const rankedGuild = guildRankings.find(item => item.guildId === created.guild.id);
  assert(rankedGuild && rankedGuild.rank > 0 && rankedGuild.totalContribution === 40 && rankedGuild.mine === true, "guild ranking entry mismatch");
  const activityClaim = await request("/api/guilds/activities/guild_daily_support/claim", {
    method: "POST",
    token: leaderToken
  });
  assert(activityClaim.goldGained === 150, "guild activity gold reward mismatch");
  assert(Array.isArray(activityClaim.items) && activityClaim.items.some(item => item.itemId === "consumable_small_heal" && item.quantity === 2), "guild activity item reward missing");
  try {
    await request("/api/guilds/activities/guild_daily_support/claim", {
      method: "POST",
      token: leaderToken
    });
    fail("duplicate guild activity claim should fail");
  } catch (ex) {
    assert(ex.code === "GUILD_ACTIVITY_ALREADY_CLAIMED", "duplicate guild activity claim returned unexpected error");
  }
  const bought = await request("/api/guilds/shop/buy", {
    method: "POST",
    token: leaderToken,
    body: { shopItemId: "guild_potion_pack" }
  });
  assert(bought && bought.itemId === "consumable_small_heal" && bought.quantity === 3, "guild shop purchase item mismatch");
  assert(bought.currentContribution === 10, "guild shop purchase did not consume contribution");
  const inventoryAfterGuildShop = await request("/api/inventory", { token: leaderToken });
  assert(inventoryAfterGuildShop.items.some(item => item.itemId === "consumable_small_heal" && item.quantity >= 3), "guild shop item missing from inventory");

  const guilds = await request("/api/guilds?limit=10", { token: leaderToken });
  const listed = guilds.find(item => item.name === guildName);
  assert(listed && listed.memberCount === 1 && listed.totalContribution === 40, "created guild missing contribution from guild list");

  const joined = await request(`/api/guilds/${created.guild.id}/join`, {
    method: "POST",
    token: memberToken
  });
  assert(joined.guild && joined.guild.myRole === "member", "guild join did not make member role");
  const leaderViewAfterJoin = await request("/api/guilds/me", { token: leaderToken });
  assert(leaderViewAfterJoin.memberCount === 2 && leaderViewAfterJoin.members.some(item => item.characterId === member.id), "joined member missing from leader guild view");

  const kicked = await request(`/api/guilds/members/${member.id}/kick`, {
    method: "POST",
    token: leaderToken
  });
  assert(kicked.guild && kicked.guild.memberCount === 1, "guild kick did not reduce member count");
  const memberAfterKick = await request("/api/guilds/me", { token: memberToken });
  assert(memberAfterKick.inGuild === false, "kicked member should no longer be in guild");

  await request(`/api/guilds/${created.guild.id}/join`, {
    method: "POST",
    token: memberToken
  });
  const transferred = await request(`/api/guilds/members/${member.id}/transfer`, {
    method: "POST",
    token: leaderToken
  });
  assert(transferred.guild && transferred.guild.myRole === "member", "old leader should become member after transfer");
  const memberLeaderView = await request("/api/guilds/me", { token: memberToken });
  assert(memberLeaderView.myRole === "leader" && memberLeaderView.leaderCharacterId === member.id, "new leader view mismatch");

  const oldLeaderLeave = await request("/api/guilds/leave", {
    method: "POST",
    token: leaderToken
  });
  assert(oldLeaderLeave.guild && oldLeaderLeave.guild.inGuild === false, "old leader should leave after transfer");
  const disband = await request("/api/guilds/leave", {
    method: "POST",
    token: memberToken
  });
  assert(disband.guild && disband.guild.inGuild === false, "last leader leave should disband guild");
  const leaderAfterDisband = await request("/api/guilds/me", { token: leaderToken });
  assert(leaderAfterDisband.inGuild === false, "leader should not be in guild after disband");
  log(`guild flow ok for leader #${leader.id} and member #${member.id}`);
}

async function trainCharacterToLevel(token, targetLevel) {
  let character = await request("/api/characters/me", { token });
  for (let attempt = 0; attempt < 20 && character.level < targetLevel; attempt++) {
    const fight = await request("/api/battles/fight", {
      method: "POST",
      token,
      body: { mapId: "map_novice_field", monsterId: "monster_chicken" }
    });
    assert(fight.win === true, "training fight should beat novice chicken");
    character.level = fight.levelAfter;
  }
  assert(character.level >= targetLevel, `character did not reach level ${targetLevel}`);
}

async function verifyBackRowSkillFlow() {
  const mageToken = await registerOrLogin(`smoke_mage_${RUN_ID}`, PASSWORD);
  await ensureCharacter(mageToken, `M${RUN_ID}`, "mage");
  await trainCharacterToLevel(mageToken, 2);
  await request("/api/skills/learn", {
    method: "POST",
    token: mageToken,
    body: { skillId: "skill_fire_charm" }
  });
  await request("/api/skills/slot", {
    method: "POST",
    token: mageToken,
    body: { skillId: "skill_fire_charm", skillSlot: 1 }
  });
  const skills = await request("/api/skills", { token: mageToken });
  const fireCharm = skills.find(item => item.id === "skill_fire_charm");
  assert(fireCharm && fireCharm.learned && fireCharm.targetType === "back_row", "fire charm should be learned as back-row skill");

  const encounter = await request("/api/battles/encounter/start", {
    method: "POST",
    token: mageToken,
    body: {
      mapId: "map_novice_field",
      monsterIds: ["monster_chicken"],
      minCount: 3,
      maxCount: 3,
      eliteChance: 0
    }
  });
  assert(Array.isArray(encounter.enemies) && encounter.enemies.length === 3, "back-row smoke encounter did not expose three enemies");
  const rowById = new Map(encounter.enemies.map(enemy => [enemy.id, enemy.row]));
  const frontEnemy = encounter.enemies.find(enemy => enemy.row === "front");
  assert(frontEnemy && encounter.enemies.some(enemy => enemy.row === "back"), "encounter must include front and back rows");

  let current = await request(`/api/battles/${encounter.battleId}/skill`, {
    method: "POST",
    token: mageToken,
    body: { skillId: "skill_fire_charm", targetId: frontEnemy.id }
  });
  assert(current.action && current.action.targetType === "back_row", "manual fire charm should report back_row target type");
  assert(current.action.targets.length > 0, "back-row skill did not hit any target");
  assert(current.action.targets.every(target => rowById.get(target.targetId) === "back"), "back-row skill should ignore guarded front target and hit back row");

  for (let i = 0; i < 60 && current.status === "running"; i++) {
    current = await request(`/api/battles/${encounter.battleId}/next`, {
      method: "POST",
      token: mageToken
    });
  }
  assert(current.status === "finished", "back-row skill encounter did not finish within 60 turns");
  log("back-row skill target rule ok");
}

async function main() {
  startServerIfRequested();
  if (START_SERVER) {
    await sleep(1000);
  }
  await waitForHealth();

  const playerUsername = `smoke_${RUN_ID}`;
  const playerToken = await registerOrLogin(playerUsername, PASSWORD);
  const guildMemberToken = await registerOrLogin(`smoke_guild_${RUN_ID}`, PASSWORD);

  let adminToken = null;
  if (!SKIP_ADMIN) {
    try {
      adminToken = await registerOrLogin("admin", ADMIN_PASSWORD);
      await ensureCharacter(adminToken, `A${RUN_ID}`, "warrior");
    } catch (ex) {
      fail(`admin smoke failed. Use SMOKE_ADMIN_PASSWORD, SMOKE_SKIP_ADMIN=1, or SMOKE_START_SERVER=1 with a fresh smoke DB. ${ex.message}`);
    }
  }

  await verifyCoreFlow(playerToken, adminToken);
  await verifyGuildFlow(playerToken, guildMemberToken);
  await verifyBackRowSkillFlow();
  log("PASS");
}

main()
  .catch(ex => {
    console.error(`[smoke] FAIL: ${ex.stack || ex.message}`);
    process.exitCode = 1;
  })
  .finally(() => {
    if (serverProcess) {
      if (process.platform === "win32") {
        spawnSync("taskkill", ["/pid", String(serverProcess.pid), "/T", "/F"], { stdio: "ignore" });
      } else {
        serverProcess.kill();
      }
      if (serverProcess.stdout) serverProcess.stdout.destroy();
      if (serverProcess.stderr) serverProcess.stderr.destroy();
      serverProcess.unref();
      setTimeout(() => process.exit(process.exitCode || 0), 1000);
    }
  });
