const state = {
  token: localStorage.getItem("legend_token") || "",
  theme: localStorage.getItem("legend_theme") || "light",
  view: "dashboard",
  ranking: "level",
  me: null,
  character: null,
  currentBattle: null,
  selectedBattleTargetId: null,
  battleTimer: null,
  encounterTimer: null,
  encounterCountdownTimer: null,
  autoEncounter: null,
  battleStreamAbort: null,
  battleStreaming: false,
  currentSceneMapId: null,
  currentScene: null,
  mapNames: {},
  mapsLoaded: false,
  mapLogsVisible: !(typeof window !== "undefined" && window.matchMedia("(max-width: 820px)").matches),
  expandedMapCluster: null,
  mapLogDrawerOpen: false,
  latestMapLog: "暂无交互记录",
  mapScale: 1,
  mapPanX: 0,
  mapPanY: 0,
  mapPointer: null,
  mapTargetPointIds: new Set(),
  mapTrackedTasks: [],
  mapTaskFilter: "all",
  tasks: [],
  skills: [],
  selectedStoryTaskId: null,
  selectedMapPointId: null,
  mapActionPendingPointId: null,
  selectedInventoryItemId: null,
  inventoryItems: [],
  equippedItemsBySlot: {},
  classes: [],
  guild: null,
  guilds: [],
  adminActivities: [],
  selectedAdminActivityId: null
};

const $ = (id) => document.getElementById(id);

const qualityText = {
  common: "普通",
  fine: "精良",
  rare: "稀有",
  epic: "史诗",
  legendary: "传说"
};

const itemTypeText = {
  equipment: "装备",
  material: "材料",
  consumable: "消耗品"
};

const slotText = {
  weapon: "武器",
  armor: "衣服",
  helmet: "头盔",
  necklace: "项链",
  bracelet: "手镯",
  ring: "戒指",
  boots: "鞋子",
  shoes: "鞋子"
};

const affixStatText = {
  hp: "生命",
  attack: "攻击",
  defense: "防御",
  attackSpeed: "攻速",
  skillTriggerBonus: "技能触发"
};

const assetVersion = "20260522-image2-pixel";

const battleStatusText = {
  running: "进行中",
  finished: "已结束"
};

const mailSourceText = {
  gm: "系统邮件",
  system: "系统邮件",
  world_boss: "世界BOSS",
  boss: "BOSS奖励",
  idle: "挂机收益"
};

const activityStatusText = {
  active: "进行中",
  upcoming: "即将开始",
  ended: "已结束"
};

const activityTypeText = {
  growth: "成长",
  drop: "掉落",
  ranking: "排行",
  festival: "节日"
};

const rankingTypeText = {
  level: "等级榜",
  power: "战力榜",
  gold: "财富榜"
};

function textOf(dict, value, fallback) {
  if (!value) return fallback || "-";
  return dict[value] || fallback || value;
}

function generatedAssetPath(group, id) {
  return `/assets/generated/${group}/${id}.png?v=${assetVersion}`;
}

function classImagePath(className) {
  return generatedAssetPath("classes", className || "warrior");
}

function monsterImagePath(monsterId) {
  return generatedAssetPath("monsters", monsterId || "monster_chicken");
}

function skillImagePath(skillId) {
  return generatedAssetPath("skills", skillId || "skill_power_slash");
}

function npcImagePath(npcId) {
  return generatedAssetPath("npcs", npcId || "npc_village_chief");
}

function assetFallback(img, fallback = "") {
  img.onerror = null;
  if (fallback) {
    img.src = fallback;
  } else {
    img.classList.add("hidden");
  }
}

function formatDateTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 16);
}

function cacheMapNames(maps) {
  (maps || []).forEach(map => {
    state.mapNames[map.id] = map.name;
  });
  state.mapsLoaded = true;
}

async function ensureMapNames() {
  if (state.mapsLoaded) return;
  const maps = await api("/api/maps");
  cacheMapNames(maps);
}

function applyTheme(theme) {
  state.theme = theme === "dark" ? "dark" : "light";
  document.body.dataset.theme = state.theme;
  localStorage.setItem("legend_theme", state.theme);
  const label = state.theme === "light" ? "切换为暗色" : "切换为白色";
  const authToggle = $("authThemeToggle");
  const gameToggle = $("themeToggle");
  if (authToggle) authToggle.textContent = label;
  if (gameToggle) gameToggle.textContent = label;
}

function toggleTheme() {
  applyTheme(state.theme === "light" ? "dark" : "light");
}

async function api(path, options = {}) {
  const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const response = await fetch(path, Object.assign({}, options, { headers }));
  const body = await response.json().catch(() => null);
  if (!response.ok || !body || body.success === false) {
    throw new Error(body && body.message ? body.message : `请求失败：${response.status}`);
  }
  return body.data;
}

function showMessage(message) {
  $("authMessage").textContent = message || "";
  $("statusLine").textContent = message || $("statusLine").textContent;
}

function clearPasswordForm() {
  ["oldPassword", "newPassword", "confirmPassword"].forEach(id => {
    const field = $(id);
    if (field) field.value = "";
  });
  $("passwordMessage").textContent = "";
}

function openPasswordModal() {
  clearPasswordForm();
  $("passwordModal").classList.remove("hidden");
  $("oldPassword").focus();
}

function closePasswordModal() {
  $("passwordModal").classList.add("hidden");
  clearPasswordForm();
}

async function changePassword() {
  const oldPassword = $("oldPassword").value;
  const newPassword = $("newPassword").value;
  const confirmPassword = $("confirmPassword").value;
  const message = $("passwordMessage");
  message.textContent = "";
  if (newPassword !== confirmPassword) {
    message.textContent = "两次输入的新密码不一致";
    return;
  }
  const submit = $("passwordSubmitBtn");
  submit.disabled = true;
  try {
    await api("/api/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ oldPassword, newPassword })
    });
    closePasswordModal();
    showMessage("密码已修改");
  } catch (error) {
    message.textContent = error.message;
  } finally {
    submit.disabled = false;
  }
}

function setAuthed(authed) {
  $("shell").classList.toggle("auth-only", !authed);
  $("sidebar").classList.toggle("hidden", !authed);
  $("globalActions").classList.toggle("hidden", !authed);
  $("authPanel").classList.toggle("hidden", authed);
  $("gamePanel").classList.toggle("hidden", !authed);
  $("logoutBtn").style.display = authed ? "block" : "none";
  if (!authed) {
    closePasswordModal();
  }
  if (state.me) {
    $("adminTab").classList.toggle("hidden", state.me.username !== "admin");
  }
}

async function login() {
  const username = $("username").value.trim();
  const password = $("password").value;
  const data = await api("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });
  state.token = data.token;
  localStorage.setItem("legend_token", state.token);
  await boot();
}

async function register() {
  const username = $("username").value.trim();
  const password = $("password").value;
  await api("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });
  await login();
}

async function boot() {
  if (!state.token) {
    setAuthed(false);
    return;
  }
  try {
    state.me = await api("/api/auth/me");
    setAuthed(true);
    $("createCharacterPanel").classList.toggle("hidden", state.me.characterCreated);
    if (state.me.characterCreated) {
      await loadCharacter();
      await refreshView();
    } else {
      await loadClasses();
      $("statusLine").textContent = "请先创建角色";
      renderStats(null);
    }
  } catch (error) {
    localStorage.removeItem("legend_token");
    state.token = "";
    setAuthed(false);
    showMessage(error.message);
  }
}

async function createCharacter() {
  const nickname = $("nickname").value.trim();
  const className = $("classSelect").value || "warrior";
  await api("/api/characters", {
    method: "POST",
    body: JSON.stringify({ nickname, className })
  });
  await boot();
}

async function loadClasses() {
  state.classes = await api("/api/characters/classes");
  const select = $("classSelect");
  if (!select) return;
  select.innerHTML = state.classes.map(item => `<option value="${item.id}">${item.name}</option>`).join("");
  renderClassPreview();
}

function renderClassPreview() {
  const box = $("classPreview");
  const select = $("classSelect");
  if (!box || !select) return;
  const item = state.classes.find(row => row.id === select.value) || state.classes[0];
  if (!item) {
    box.innerHTML = "";
    return;
  }
  box.innerHTML = `
    <img src="${classImagePath(item.id)}" alt="${item.name}" onerror="assetFallback(this)">
    <div>
      <strong>${item.name}</strong>
      <span>${item.description}</span>
      <span>生命 ${item.hp} / 攻击 ${item.attack} / 防御 ${item.defense} / 攻速 ${item.attackSpeed}</span>
    </div>
  `;
}

async function loadCharacter() {
  state.character = await api("/api/characters/me");
  if (!state.mapsLoaded) {
    await ensureMapNames().catch(() => {});
  }
  $("statusLine").textContent = `${state.character.nickname} Lv.${state.character.level} 战力 ${state.character.power}`;
  renderStats(state.character);
}

function renderStats(character) {
  const box = $("characterStats");
  if (!character) {
    box.innerHTML = "";
    return;
  }
  const stats = [
    ["职业", character.classDisplayName || textOf({ warrior: "战士", mage: "法师", taoist: "道士" }, character.className, character.className)],
    ["等级", character.level],
    ["经验", character.exp],
    ["金币", character.gold],
    ["生命", character.hp],
    ["攻击", character.attack],
    ["防御", character.defense],
    ["攻速", character.attackSpeed],
    ["战力", character.power],
    ["天赋点", `${character.availableTalentPoints || 0}/${character.talentPoints || 0}`],
    ["位置", state.mapNames[character.currentMapId] || "当前地图"]
  ];
  const className = character.className || "warrior";
  const classLabel = character.classDisplayName || textOf({ warrior: "战士", mage: "法师", taoist: "道士" }, className, className);
  box.innerHTML = `
    <div class="stat character-portrait-card">
      <img src="${classImagePath(className)}" alt="${classLabel}" onerror="assetFallback(this)">
      <div>
        <span>角色</span>
        <strong>${character.nickname}</strong>
        <small>${classLabel} Lv.${character.level}</small>
      </div>
    </div>
    ${stats.map(([label, value]) => `<div class="stat"><span>${label}</span><strong>${value}</strong></div>`).join("")}
  `;
}

async function loadMaps() {
  const maps = await api("/api/maps");
  cacheMapNames(maps);
  $("mapList").innerHTML = maps.map(map => `
    <div class="item map-list-item ${state.currentSceneMapId === map.id ? "active" : ""}" data-map-id="${map.id}">
      <div class="item-header"><strong>${map.name}</strong><small>Lv.${map.requiredLevel} / 战力 ${map.recommendedPower}</small></div>
      <div class="item-actions">
        <button onclick="loadMapScene('${map.id}')">进入</button>
        <button class="secondary" onclick="loadMapDetail('${map.id}')">怪物</button>
      </div>
      <div id="monsters-${map.id}"></div>
    </div>
  `).join("");
  const defaultMapId = state.currentSceneMapId || (state.character && state.character.currentMapId) || (maps[0] && maps[0].id);
  if (defaultMapId) {
    await loadMapScene(defaultMapId, true);
  }
  await loadMapTaskTracker();
}

async function loadMapDetail(mapId) {
  const detail = await api(`/api/maps/${mapId}`);
  const node = $(`monsters-${mapId}`);
  node.innerHTML = detail.monsters.map(monster => `
    <div class="item media-item">
      <img class="asset-thumb" src="${monsterImagePath(monster.id)}" alt="${monster.name}" onerror="assetFallback(this)">
      <div class="media-body">
        <div class="item-header"><strong>${monster.name}</strong><small>Lv.${monster.level} 经验 ${monster.exp}</small></div>
        <small>生命 ${monster.hp} / 攻击 ${monster.attack} / 防御 ${monster.defense} / 攻速 ${monster.attackSpeed}</small>
        <div class="item-actions"><button onclick="startBattle('${detail.id}', '${monster.id}', 'battleLog')">攻击</button></div>
      </div>
    </div>
  `).join("");
}

async function loadMapScene(mapId, fromListRefresh = false) {
  const scene = await api(`/api/maps/${mapId}/scene`);
  state.currentSceneMapId = mapId;
  state.currentScene = scene;
  state.expandedMapCluster = null;
  state.selectedMapPointId = null;
  state.mapActionPendingPointId = null;
  if (!fromListRefresh) {
    await refreshMapListHighlight();
  }
  renderMapScene(scene);
}

async function refreshMapListHighlight() {
  document.querySelectorAll(".map-list-item").forEach(node => {
    node.classList.toggle("active", node.dataset.mapId === state.currentSceneMapId);
  });
}

function pointKindText(type) {
  if (type === "npc") return "NPC";
  if (type === "monster_area") return "怪区";
  if (type === "random_encounter") return "探索";
  if (type === "portal") return "传送";
  if (type === "reward") return "奖励";
  return "事件";
}

function pointClass(type) {
  if (type === "npc") return "npc";
  if (type === "portal") return "portal";
  if (type === "reward") return "reward";
  if (type === "random_encounter") return "explore";
  return "monster";
}

function pointActionText(type) {
  if (type === "npc") return "对话";
  if (type === "portal") return "传送";
  if (type === "reward") return "探索";
  if (type === "random_encounter") return "探索";
  return "进入战斗";
}

function mapTerrainClass(sprite) {
  const value = String(sprite || "").toLowerCase();
  if (value.includes("mine")) return "mine";
  if (value.includes("valley")) return "valley";
  if (value.includes("skeleton")) return "skeleton";
  if (value.includes("temple")) return "temple";
  if (value.includes("redmoon") || value.includes("demon")) return "redmoon";
  return "field";
}

function renderMapScene(scene) {
  $("mapSceneTitle").textContent = `${scene.name} / Lv.${scene.requiredLevel}`;
  const points = [...(scene.npcs || []), ...(scene.events || [])];
  updateMapTargetPointIds();
  const clusters = clusterMapPoints(points, scene);
  const player = scene.player || { x: 180, y: 520 };
  const aspect = `${Math.max(1, scene.width || 16)} / ${Math.max(1, scene.height || 10)}`;
  const targetCount = state.mapTargetPointIds ? state.mapTargetPointIds.size : 0;
  $("mapScene").innerHTML = `
    <div class="map-toolbar">
      <button class="secondary" type="button" onclick="recenterPlayerMarker()">回到角色</button>
      <button class="secondary" type="button" onclick="zoomMap(1)">放大</button>
      <button class="secondary" type="button" onclick="zoomMap(-1)">缩小</button>
      <button class="secondary" type="button" onclick="resetMapView()">重置视角</button>
      <button id="mapLogToggle" class="secondary" type="button" onclick="toggleMapLogs()">${state.mapLogsVisible ? "隐藏日志" : "显示日志"}</button>
    </div>
    <div class="map-viewport" style="--map-aspect:${aspect};">
      <div id="mapCanvas" class="map-canvas terrain-${mapTerrainClass(scene.backgroundSprite)} ${scene.locked ? "locked" : ""}"
        style="${mapTransformStyle()}"
        onclick="movePlayerByCanvasTap(event)"
        onpointerdown="startMapPan(event)"
        onpointermove="moveMapPan(event)"
        onpointerup="endMapPan(event)"
        onpointercancel="endMapPan(event)"
        onpointerleave="endMapPan(event)">
        <div class="map-terrain-layer">
          <span class="terrain-band band-a"></span>
          <span class="terrain-band band-b"></span>
          <span class="terrain-band band-c"></span>
        </div>
        ${renderMapRouteLayer(scene, points, player)}
        <div id="playerMarker" class="player-marker" style="left:${coordPercent(player.x, scene.width)}%;top:${coordPercent(player.y, scene.height)}%;">
          <span></span>
        </div>
        ${clusters.map(cluster => renderMapCluster(cluster, scene)).join("")}
        ${renderMapPointDetail(scene)}
        <div class="map-gridline horizontal"></div>
        <div class="map-gridline vertical"></div>
      </div>
      ${renderMapMiniMap(scene, points, player)}
    </div>
    <div class="map-legend">
      <span>点位 ${points.length}</span>
      <span>怪物 ${scene.monsters ? scene.monsters.length : 0}</span>
      <span>任务目标 ${targetCount}</span>
      <span>${scene.locked ? "等级不足" : "可探索"}</span>
    </div>
  `;
  applyMapLogsVisibility();
  renderMapLogDrawer();
}

function mapPointX(point, scene) {
  return coordPercent(point.x, scene.width);
}

function mapPointY(point, scene) {
  return coordPercent(point.y, scene.height);
}

function renderMapRouteLayer(scene, points, player) {
  const pointMap = Object.fromEntries((points || []).map(point => [point.id, point]));
  const lines = [];
  const seen = new Set();
  (points || []).forEach(point => {
    (point.nextEventIds || []).forEach(nextId => {
      const next = pointMap[nextId];
      if (!next) return;
      const key = [point.id, next.id].sort().join("::");
      if (seen.has(key)) return;
      seen.add(key);
      lines.push(renderRouteLine(mapPointX(point, scene), mapPointY(point, scene), mapPointX(next, scene), mapPointY(next, scene), "event"));
    });
  });
  const selected = findMapPoint(state.selectedMapPointId);
  if (selected) {
    lines.push(renderRouteLine(coordPercent(player.x, scene.width), coordPercent(player.y, scene.height), mapPointX(selected, scene), mapPointY(selected, scene), "selected"));
  }
  (points || []).forEach(point => {
    if (!state.mapTargetPointIds.has(point.id) || (selected && selected.id === point.id)) return;
    lines.push(renderRouteLine(coordPercent(player.x, scene.width), coordPercent(player.y, scene.height), mapPointX(point, scene), mapPointY(point, scene), "task"));
  });
  if (!lines.length) return "";
  return `
    <svg class="map-route-layer" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
      ${lines.join("")}
    </svg>
  `;
}

function renderRouteLine(x1, y1, x2, y2, type) {
  return `
    <g class="route-line ${type}">
      <line x1="${x1.toFixed(2)}" y1="${y1.toFixed(2)}" x2="${x2.toFixed(2)}" y2="${y2.toFixed(2)}"></line>
      <circle cx="${x2.toFixed(2)}" cy="${y2.toFixed(2)}" r="0.72"></circle>
    </g>
  `;
}

function renderMapMiniMap(scene, points, player) {
  return `
    <div class="map-mini" aria-label="小地图">
      <div class="map-mini-title">
        <strong>小地图</strong>
        <small>${scene.name}</small>
      </div>
      <div class="map-mini-canvas">
        <span class="mini-player" style="left:${coordPercent(player.x, scene.width)}%;top:${coordPercent(player.y, scene.height)}%;" title="角色"></span>
        ${(points || []).map(point => `
          <button class="mini-point ${pointClass(point.type)} ${state.mapTargetPointIds.has(point.id) ? "target" : ""} ${state.selectedMapPointId === point.id ? "selected" : ""}"
            type="button"
            style="left:${mapPointX(point, scene)}%;top:${mapPointY(point, scene)}%;"
            onclick="activateMapPoint('${point.id}', event)"
            title="${point.name}"
            aria-label="${point.name}"
            ${point.locked || scene.locked ? "disabled" : ""}></button>
        `).join("")}
      </div>
    </div>
  `;
}

function mapTransformStyle() {
  return `--map-scale:${state.mapScale};--map-pan-x:${state.mapPanX}px;--map-pan-y:${state.mapPanY}px;`;
}

function applyMapTransform() {
  const canvas = $("mapCanvas");
  if (canvas) {
    canvas.style.setProperty("--map-scale", state.mapScale);
    canvas.style.setProperty("--map-pan-x", `${state.mapPanX}px`);
    canvas.style.setProperty("--map-pan-y", `${state.mapPanY}px`);
  }
}

function zoomMap(direction) {
  const next = Math.max(1, Math.min(1.8, Number((state.mapScale + direction * 0.2).toFixed(2))));
  state.mapScale = next;
  if (state.mapScale === 1) {
    state.mapPanX = 0;
    state.mapPanY = 0;
  }
  applyMapTransform();
}

function resetMapView() {
  state.mapScale = 1;
  state.mapPanX = 0;
  state.mapPanY = 0;
  state.mapPointer = null;
  applyMapTransform();
  appendMapLog("视角已重置");
}

function startMapPan(event) {
  if (event.target.closest(".map-point") || event.target.closest(".map-cluster") || event.target.closest(".map-point-detail")) return;
  if (state.mapScale <= 1) return;
  state.mapPointer = {
    id: event.pointerId,
    x: event.clientX,
    y: event.clientY,
    panX: state.mapPanX,
    panY: state.mapPanY,
    moved: false
  };
  event.currentTarget.setPointerCapture(event.pointerId);
}

function moveMapPan(event) {
  const pointer = state.mapPointer;
  if (!pointer || pointer.id !== event.pointerId) return;
  const dx = event.clientX - pointer.x;
  const dy = event.clientY - pointer.y;
  if (Math.abs(dx) + Math.abs(dy) > 5) pointer.moved = true;
  state.mapPanX = pointer.panX + dx;
  state.mapPanY = pointer.panY + dy;
  applyMapTransform();
}

function endMapPan(event) {
  if (!state.mapPointer || state.mapPointer.id !== event.pointerId) return;
  if (event.currentTarget && event.currentTarget.releasePointerCapture) {
    try {
      event.currentTarget.releasePointerCapture(event.pointerId);
    } catch (ignore) {
      // Pointer capture may already be released by the browser.
    }
  }
  setTimeout(() => {
    state.mapPointer = null;
  }, 0);
}

function clusterMapPoints(points, scene) {
  const threshold = Math.max(7, Math.min(11, 920 / Math.max(scene.width || 920, scene.height || 620)));
  const clusters = [];
  (points || []).forEach(point => {
    const x = coordPercent(point.x, scene.width);
    const y = coordPercent(point.y, scene.height);
    const match = clusters.find(cluster => Math.abs(cluster.x - x) <= threshold && Math.abs(cluster.y - y) <= threshold);
    if (match) {
      match.points.push(point);
      match.x = (match.x * (match.points.length - 1) + x) / match.points.length;
      match.y = (match.y * (match.points.length - 1) + y) / match.points.length;
    } else {
      clusters.push({ id: `cluster-${clusters.length}`, x, y, points: [point] });
    }
  });
  return clusters;
}

function renderMapCluster(cluster, scene) {
  if (cluster.points.length === 1) {
    return renderMapPoint(cluster.points[0], scene, cluster.x, cluster.y);
  }
  const expanded = state.expandedMapCluster === cluster.id;
  const target = cluster.points.some(point => state.mapTargetPointIds.has(point.id));
  return `
    <div class="map-cluster ${expanded ? "expanded" : ""} ${target ? "target" : ""}" style="left:${cluster.x}%;top:${cluster.y}%;">
      <button class="cluster-button" type="button" onclick="toggleMapCluster('${cluster.id}', event)" title="展开附近点位">
        <span>${cluster.points.length}</span>
        <small>${target ? "目标" : "附近"}</small>
      </button>
      ${expanded ? `
        <div class="cluster-menu">
          ${cluster.points.map(point => `
            <button class="${state.mapTargetPointIds.has(point.id) ? "target" : ""} ${state.selectedMapPointId === point.id ? "selected" : ""}" type="button" onclick="activateMapPoint('${point.id}', event)" ${point.locked || scene.locked ? "disabled" : ""}>
              <strong>${point.name}</strong>
              <small>${point.statusText || (point.locked ? "锁定" : pointKindText(point.type))}</small>
            </button>
          `).join("")}
        </div>
      ` : ""}
    </div>
  `;
}

function renderMapPoint(point, scene, x, y) {
  const disabled = point.locked || scene.locked ? "disabled" : "";
  const target = state.mapTargetPointIds.has(point.id);
  const selected = state.selectedMapPointId === point.id;
  const pending = state.mapActionPendingPointId === point.id;
  return `
    <button class="map-point ${pointClass(point.type)} ${target ? "target" : ""} ${selected ? "selected" : ""} ${pending ? "pending" : ""} ${point.locked ? "locked" : ""} ${point.completed ? "completed" : ""} ${point.coolingDown ? "cooling" : ""}"
      style="left:${typeof x === "number" ? x : coordPercent(point.x, scene.width)}%;top:${typeof y === "number" ? y : coordPercent(point.y, scene.height)}%;"
      onclick="activateMapPoint('${point.id}', event)" ${disabled}
      title="${point.nextAvailableAt ? `${point.description || point.name} / ${point.nextAvailableAt}` : (point.description || point.name)}">
      <span class="point-mark"></span>
      <span class="point-label">${point.name}</span>
      <small>${point.statusText || (point.locked ? "锁定" : pointKindText(point.type))}</small>
    </button>
  `;
}

function renderMapPointDetail(scene) {
  const point = findMapPoint(state.selectedMapPointId);
  if (!point) return "";
  const left = coordPercent(point.x, scene.width);
  const top = coordPercent(point.y, scene.height);
  const positionClass = mapPointDetailPositionClass(left, top);
  const locked = scene.locked || point.locked;
  const target = state.mapTargetPointIds.has(point.id);
  const pending = state.mapActionPendingPointId === point.id;
  const pointImage = pointImagePath(point);
  return `
    <div class="map-point-detail ${pointClass(point.type)} ${target ? "target" : ""} ${positionClass}" style="left:${left}%;top:${top}%;" onclick="event.stopPropagation()">
      <div class="detail-head">
        <span class="point-mark"></span>
        ${pointImage ? `<img class="point-portrait" src="${pointImage}" alt="${point.name}" onerror="assetFallback(this)">` : ""}
        <div>
          <strong>${point.name}</strong>
          <small>${pointKindText(point.type)} / ${point.statusText || (point.locked ? "锁定" : "可交互")}</small>
        </div>
        <button class="ghost compact-button" type="button" onclick="closeMapPointDetail(event)" aria-label="关闭">×</button>
      </div>
      <p>${point.description || point.dialogue || point.name}</p>
      <div class="detail-meta">
        ${renderPointMeta(scene, point)}
      </div>
      <div class="item-actions">
        <button type="button" onclick="executeMapPoint('${point.id}', event)" ${locked || pending ? "disabled" : ""}>${pending ? "前往中" : pointActionText(point.type)}</button>
        ${isEncounterPoint(point) ? `<button class="secondary" type="button" onclick="startAutoEncounterFromPoint('${point.id}', event)" ${locked ? "disabled" : ""}>Auto hunt</button>` : ""}
        ${target ? `<button class="secondary" type="button" onclick="movePlayerToSelectedPoint(event)">定位</button>` : ""}
      </div>
    </div>
  `;
}

function pointImagePath(point) {
  if (!point) return "";
  if (point.type === "npc") return npcImagePath(point.id);
  if (point.type === "monster_area" || point.type === "random_encounter") {
    const monsterId = point.targetMonsterIds && point.targetMonsterIds[0];
    return monsterId ? monsterImagePath(monsterId) : "";
  }
  return "";
}

function isEncounterPoint(point) {
  return !!(point && (point.type === "monster_area" || point.type === "random_encounter") && point.targetMonsterIds && point.targetMonsterIds.length);
}

function mapPointDetailPositionClass(left, top) {
  const classes = [];
  if (left < 18) classes.push("edge-left");
  if (left > 82) classes.push("edge-right");
  if (top < 28) classes.push("below");
  return classes.join(" ");
}

function renderPointMeta(scene, point) {
  const lines = [];
  if (state.mapTargetPointIds.has(point.id)) lines.push("任务目标");
  if (point.nextAvailableAt) lines.push(`可用时间 ${formatDateTime(point.nextAvailableAt)}`);
  if (point.coolingDown) lines.push("冷却中");
  if (point.completed) lines.push("已完成");
  if (point.targetMapId) lines.push(`目标地图 ${state.mapNames[point.targetMapId] || point.targetMapId}`);
  const monsters = monsterNamesForPoint(scene, point);
  if (monsters) lines.push(`可能遭遇 ${monsters}`);
  if (!lines.length) lines.push(point.dialogue || "点击执行当前点位行动");
  return lines.map(line => `<span>${line}</span>`).join("");
}

function monsterNamesForPoint(scene, point) {
  if (!point.targetMonsterIds || !point.targetMonsterIds.length) return "";
  const monsterMap = {};
  (scene.monsters || []).forEach(monster => {
    monsterMap[monster.id] = monster.name;
  });
  return point.targetMonsterIds.map(id => monsterMap[id] || id).join("，");
}

function allScenePoints() {
  const scene = state.currentScene;
  if (!scene) return [];
  return [...(scene.npcs || []), ...(scene.events || [])];
}

function findMapPoint(pointId) {
  if (!pointId) return null;
  return allScenePoints().find(item => item.id === pointId) || null;
}

function normalizeText(value) {
  return String(value || "").replace(/\s+/g, "").toLowerCase();
}

function isTaskPointMatch(task, point) {
  if (task.targetPointId) return task.targetPointId === point.id;
  if (task.targetMapId && state.currentScene && task.targetMapId !== state.currentScene.id) return false;
  const target = normalizeText(task.targetName);
  const name = normalizeText(point.name);
  if (!target || !name) return false;
  if (task.type === "talk_npc") return point.type === "npc" && (name.includes(target) || target.includes(name));
  if (task.type === "explore_event") return point.type !== "npc" && (name.includes(target) || target.includes(name));
  if (task.type === "kill_monster") return point.type === "monster_area" || point.type === "random_encounter";
  return false;
}

function findTaskTargetPoint(task) {
  if (task.targetMapId && state.currentScene && task.targetMapId !== state.currentScene.id) return null;
  if (task.targetPointId) {
    return allScenePoints().find(point => point.id === task.targetPointId) || null;
  }
  return allScenePoints().find(point => isTaskPointMatch(task, point));
}

function taskTargetMapName(task) {
  if (!task || !task.targetMapId) return "";
  return state.mapNames[task.targetMapId] || "目标地图";
}

function isTaskTargetOnAnotherMap(task) {
  return !!(task && task.targetMapId && state.currentScene && task.targetMapId !== state.currentScene.id);
}

function updateMapTargetPointIds(tasks) {
  const ids = new Set();
  (tasks || state.mapTrackedTasks || []).forEach(task => {
    if (task.locked || task.status === 2) return;
    const point = findTaskTargetPoint(task);
    if (point) ids.add(point.id);
  });
  state.mapTargetPointIds = ids;
}

function findKnownTask(taskId) {
  return (state.tasks || []).find(item => item.id === taskId)
    || (state.mapTrackedTasks || []).find(item => item.id === taskId);
}

function focusTaskTarget(taskId) {
  const task = findKnownTask(taskId);
  if (isTaskTargetOnAnotherMap(task)) {
    goTaskTargetMap(taskId);
    return;
  }
  const point = task && findTaskTargetPoint(task);
  if (!point) {
    appendMapLog("当前地图未找到任务目标");
    return;
  }
  state.expandedMapCluster = null;
  state.selectedMapPointId = point.id;
  renderMapScene(state.currentScene);
  movePlayerTo(point.id);
  appendMapLog(`已定位任务目标：${point.name}`);
}

async function goTaskTargetMap(taskId) {
  try {
    const task = findKnownTask(taskId);
    if (!task || !task.targetMapId) {
      appendMapLog("该任务暂无可定位地图");
      return;
    }
    await loadMapScene(task.targetMapId);
    await loadMapTaskTracker();
    const point = findTaskTargetPoint(task);
    if (point) {
      state.expandedMapCluster = null;
      state.selectedMapPointId = point.id;
      renderMapScene(state.currentScene);
      await movePlayerTo(point.id);
      appendMapLog(`已前往${taskTargetMapName(task)}，定位任务目标：${point.name}`);
    } else {
      appendMapLog(`已前往${taskTargetMapName(task)}，请查看地图目标提示`);
    }
  } catch (error) {
    showMessage(error.message);
  }
}

async function openTaskTarget(taskId) {
  const task = findKnownTask(taskId);
  if (!task || task.locked) return;
  if (state.view !== "maps") {
    switchView("maps");
  }
  if (task.targetMapId) {
    await goTaskTargetMap(taskId);
  } else {
    focusTaskTarget(taskId);
  }
}

function toggleMapCluster(clusterId, event) {
  if (event) event.stopPropagation();
  state.expandedMapCluster = state.expandedMapCluster === clusterId ? null : clusterId;
  if (state.currentScene) renderMapScene(state.currentScene);
}

function activateMapPoint(pointId, event) {
  if (event) event.stopPropagation();
  const scene = state.currentScene;
  if (!scene || scene.locked) return;
  const point = findMapPoint(pointId);
  if (!point || point.locked) return;
  state.expandedMapCluster = null;
  state.selectedMapPointId = point.id;
  renderMapScene(scene);
}

function closeMapPointDetail(event) {
  if (event) event.stopPropagation();
  state.selectedMapPointId = null;
  state.mapActionPendingPointId = null;
  if (state.currentScene) renderMapScene(state.currentScene);
}

function movePlayerToSelectedPoint(event) {
  if (event) event.stopPropagation();
  const point = findMapPoint(state.selectedMapPointId);
  if (!point) return;
  movePlayerTo(point.id);
  appendMapLog(`已定位：${point.name}`);
}

async function executeMapPoint(pointId, event) {
  if (event) event.stopPropagation();
  const scene = state.currentScene;
  const point = findMapPoint(pointId);
  if (!scene || scene.locked || !point || point.locked) return;
  state.expandedMapCluster = null;
  state.selectedMapPointId = point.id;
  state.mapActionPendingPointId = point.id;
  renderMapScene(scene);
  appendMapLog(`正在前往：${point.name}`);
  try {
    const action = point.type === "npc" ? talkNpc : triggerMapEvent;
    await action(point.id);
  } finally {
    state.mapActionPendingPointId = null;
    if (state.currentScene && state.selectedMapPointId === point.id) {
      renderMapScene(state.currentScene);
    }
  }
}

function coordPercent(value, total) {
  if (!total) return 0;
  return Math.max(0, Math.min(100, value * 100 / total));
}

function applyMapLogsVisibility() {
  const sidePanel = $("mapSidePanel");
  const layout = $("layoutMap");
  const toggle = $("mapLogToggle");
  if (sidePanel) sidePanel.classList.toggle("collapsed", !state.mapLogsVisible);
  if (layout) layout.classList.toggle("logs-collapsed", !state.mapLogsVisible);
  if (toggle) {
    toggle.textContent = state.mapLogsVisible ? "隐藏日志" : "显示日志";
    toggle.setAttribute("aria-expanded", String(state.mapLogsVisible));
  }
}

function toggleMapLogs() {
  state.mapLogsVisible = !state.mapLogsVisible;
  applyMapLogsVisibility();
  renderMapLogDrawer();
}

function syncMobileMapLogs() {
  const eventMobile = $("mapEventLogMobile");
  const battleMobile = $("battleLogMobile");
  if (eventMobile && $("mapEventLog")) eventMobile.innerHTML = $("mapEventLog").innerHTML;
  if (battleMobile && $("battleLog")) battleMobile.innerHTML = $("battleLog").innerHTML;
}

function renderMapLogDrawer() {
  const drawer = $("mapLogDrawer");
  const preview = $("mapLogDrawerPreview");
  if (!drawer || !preview) return;
  drawer.classList.toggle("hidden", state.view !== "maps");
  drawer.classList.toggle("open", state.mapLogDrawerOpen);
  preview.textContent = state.latestMapLog || "暂无交互记录";
  $("mapLogDrawerHandle").setAttribute("aria-expanded", String(state.mapLogDrawerOpen));
  syncMobileMapLogs();
}

function toggleMapLogDrawer() {
  state.mapLogDrawerOpen = !state.mapLogDrawerOpen;
  renderMapLogDrawer();
}

function recenterPlayerMarker() {
  const scene = state.currentScene;
  const marker = $("playerMarker");
  if (!scene || !marker) return;
  const player = scene.player || { x: 180, y: 520 };
  const x = coordPercent(player.x, scene.width);
  const y = coordPercent(player.y, scene.height);
  marker.style.left = `${x}%`;
  marker.style.top = `${y}%`;
  updateMiniPlayerMarker(x, y);
  appendMapLog("已回到角色当前位置");
}

function updateMiniPlayerMarker(x, y) {
  const miniPlayer = document.querySelector(".mini-player");
  if (!miniPlayer) return;
  miniPlayer.style.left = `${x}%`;
  miniPlayer.style.top = `${y}%`;
}

function movePlayerByCanvasTap(event) {
  const scene = state.currentScene;
  if (!scene || scene.locked || event.target.closest(".map-point") || event.target.closest(".map-cluster") || event.target.closest(".map-point-detail")) return;
  if (state.mapPointer && state.mapPointer.moved) return;
  state.expandedMapCluster = null;
  state.selectedMapPointId = null;
  const marker = $("playerMarker");
  if (!marker) return;
  const rect = event.currentTarget.getBoundingClientRect();
  const x = Math.max(0, Math.min(100, ((event.clientX - rect.left) * 100 / rect.width - 50) / state.mapScale + 50));
  const y = Math.max(0, Math.min(100, ((event.clientY - rect.top) * 100 / rect.height - 50) / state.mapScale + 50));
  marker.style.left = `${x}%`;
  marker.style.top = `${y}%`;
  updateMiniPlayerMarker(x, y);
  appendMapLog("移动到附近位置");
}

function movePlayerTo(pointId) {
  const scene = state.currentScene;
  if (!scene) return Promise.resolve();
  const point = findMapPoint(pointId);
  const marker = $("playerMarker");
  if (!point || !marker) return Promise.resolve();
  const currentLeft = parseFloat(marker.style.left || "0");
  const nextLeft = coordPercent(point.x, scene.width);
  marker.classList.remove("arrived", "walk-left", "walk-right");
  marker.classList.add("walking", nextLeft < currentLeft ? "walk-left" : "walk-right");
  marker.style.left = `${coordPercent(point.x, scene.width)}%`;
  marker.style.top = `${coordPercent(point.y, scene.height)}%`;
  updateMiniPlayerMarker(coordPercent(point.x, scene.width), coordPercent(point.y, scene.height));
  return new Promise(resolve => setTimeout(() => {
    marker.classList.remove("walking", "walk-left", "walk-right");
    marker.classList.add("arrived");
    setTimeout(() => marker.classList.remove("arrived"), 360);
    resolve();
  }, 320));
}

function appendMapLog(message) {
  const node = $("mapEventLog");
  state.latestMapLog = message || state.latestMapLog;
  if (!node) return;
  const entry = document.createElement("div");
  entry.className = "log-entry";
  entry.textContent = message;
  node.prepend(entry);
  renderMapLogDrawer();
}

async function talkNpc(npcId) {
  await movePlayerTo(npcId);
  const result = await api(`/api/npcs/${npcId}/talk`, { method: "POST" });
  appendMapLog(`${result.npcName}：${(result.dialogueLines || []).join(" ")}`);
  await loadCharacter();
  await loadMapScene(result.mapId || state.currentSceneMapId);
  if (state.view === "maps") await loadMapTaskTracker();
  if (state.view === "tasks") await loadTasks();
}

async function triggerMapEvent(eventId) {
  await movePlayerTo(eventId);
  const result = await api(`/api/map-events/${eventId}/trigger`, { method: "POST" });
  appendMapLog(result.message || result.eventName);
  if (result.action === "battle") {
    await startBattle(result.mapId, result.monsterId, "battleLog");
    return;
  }
  await loadCharacter();
  await loadMapScene(result.targetMapId || result.mapId || state.currentSceneMapId);
  if (state.view === "maps") await loadMapTaskTracker();
  if (state.view === "tasks") await loadTasks();
}

function stopBattleTimer() {
  if (state.battleTimer) {
    clearTimeout(state.battleTimer);
    state.battleTimer = null;
  }
  if (state.battleStreamAbort) {
    state.battleStreamAbort.abort();
    state.battleStreamAbort = null;
  }
  state.battleStreaming = false;
}

function stopAutoEncounter(message) {
  if (state.encounterTimer) {
    clearTimeout(state.encounterTimer);
    state.encounterTimer = null;
  }
  if (state.encounterCountdownTimer) {
    clearInterval(state.encounterCountdownTimer);
    state.encounterCountdownTimer = null;
  }
  state.autoEncounter = null;
  if (message) appendMapLog(message);
  const status = $("autoEncounterStatus");
  if (status) status.remove();
}

async function startAutoEncounterFromPoint(pointId, event) {
  if (event) event.stopPropagation();
  const point = findMapPoint(pointId);
  const scene = state.currentScene;
  if (!point || !scene || !isEncounterPoint(point)) return;
  await movePlayerTo(point.id);
  state.selectedMapPointId = point.id;
  startAutoEncounter({
    mapId: scene.id,
    eventId: point.id,
    pointId: point.id,
    pointName: point.name,
    monsterIds: point.targetMonsterIds || [],
    minCount: point.encounterMinCount || 1,
    maxCount: point.encounterMaxCount || (point.type === "random_encounter" ? 2 : 4),
    eliteChance: typeof point.encounterEliteChance === "number" ? point.encounterEliteChance : (point.type === "random_encounter" ? 0.12 : 0.2),
    intervalMs: Math.max(2, point.encounterIntervalSeconds || 5) * 1000
  });
}

async function startAutoEncounter(options) {
  stopBattleTimer();
  stopAutoEncounter();
  state.autoEncounter = { ...options, running: true, battleCount: 0 };
  appendMapLog(`Auto hunt started: ${options.pointName}`);
  await startEncounterBattle();
}

async function startEncounterBattle() {
  if (!state.autoEncounter || !state.autoEncounter.running) return;
  const options = state.autoEncounter;
  const result = await api("/api/battles/encounter/start", {
    method: "POST",
    body: JSON.stringify({
      mapId: options.mapId,
      eventId: options.eventId,
      monsterIds: options.monsterIds,
      minCount: options.minCount,
      maxCount: options.maxCount,
      eliteChance: options.eliteChance
    })
  });
  options.battleCount += 1;
  state.currentBattle = { id: result.battleId, logId: "battleLog" };
  state.selectedBattleTargetId = firstAliveEnemyId(result);
  appendMapLog(`Encounter #${options.battleCount}: ${result.monster ? result.monster.name : "monster"}`);
  renderBattle(result, "battleLog");
  startBattleStream(result);
}

function scheduleNextEncounter() {
  if (!state.autoEncounter || !state.autoEncounter.running) return;
  const interval = Math.max(2000, state.autoEncounter.intervalMs || 5000);
  let remaining = Math.ceil(interval / 1000);
  renderEncounterStatus(remaining);
  state.encounterCountdownTimer = setInterval(() => {
    remaining -= 1;
    if (remaining <= 0) {
      clearInterval(state.encounterCountdownTimer);
      state.encounterCountdownTimer = null;
    }
    renderEncounterStatus(Math.max(0, remaining));
  }, 1000);
  state.encounterTimer = setTimeout(() => {
    state.encounterTimer = null;
    startEncounterBattle().catch(error => {
      stopAutoEncounter();
      showMessage(error.message);
    });
  }, interval);
}

function renderEncounterStatus(seconds) {
  const node = $("battleLog");
  if (!node || !state.autoEncounter) return;
  let status = $("autoEncounterStatus");
  if (!status) {
    status = document.createElement("div");
    status.id = "autoEncounterStatus";
    status.className = "auto-encounter-status";
    node.prepend(status);
  }
  status.innerHTML = `
    <strong>Auto hunt: ${state.autoEncounter.pointName}</strong>
    <span>Next encounter in ${seconds}s</span>
    <button class="secondary compact-stop" type="button" onclick="stopAutoEncounter('Auto hunt stopped')">Stop</button>
  `;
  syncMobileMapLogs();
}

async function startBattle(mapId, monsterId, logId = "battleLog") {
  stopAutoEncounter();
  stopBattleTimer();
  const result = await api("/api/battles/start", {
    method: "POST",
    body: JSON.stringify({ mapId, monsterId })
  });
  state.currentBattle = { id: result.battleId, logId };
  state.selectedBattleTargetId = firstAliveEnemyId(result);
  renderBattle(result, logId);
  startBattleStream(result);
}

async function nextBattle() {
  if (!state.currentBattle) return;
  if (state.battleStreaming) return;
  const result = await api(`/api/battles/${state.currentBattle.id}/next`, {
    method: "POST",
    body: JSON.stringify({ targetId: state.selectedBattleTargetId })
  });
  renderBattle(result, state.currentBattle.logId);
  if (result.status === "finished") {
    stopBattleTimer();
    await loadCharacter();
    if (state.view === "maps" && state.currentSceneMapId) await loadMapScene(state.currentSceneMapId);
    if (state.view === "maps") await loadMapTaskTracker();
    if (state.view === "tasks") await loadTasks();
    if (state.view === "bosses") await loadBosses();
    if (state.view === "worldBosses") await loadWorldBosses();
    scheduleNextEncounter();
    return;
  }
  scheduleBattleNext(result);
}

function scheduleBattleNext(result) {
  if (state.battleTimer) {
    clearTimeout(state.battleTimer);
    state.battleTimer = null;
  }
  if (!result || result.status !== "running") return;
  if (result.nextActor === "player" && (result.skills || []).length) return;
  state.battleTimer = setTimeout(() => {
    nextBattle().catch(error => {
      stopBattleTimer();
      showMessage(error.message);
    });
  }, Math.max(700, result.suggestedDelayMs || 1000));
}

function startBattleStream(initialResult) {
  if ((initialResult.skills || []).length) {
    scheduleBattleNext(initialResult);
    return;
  }
  if (!window.ReadableStream || !window.TextDecoder || !window.AbortController) {
    scheduleBattleNext(initialResult);
    return;
  }
  if (!state.currentBattle) return;
  const controller = new AbortController();
  state.battleStreamAbort = controller;
  state.battleStreaming = true;
  readBattleStream(state.currentBattle.id, state.currentBattle.logId, controller)
    .catch(error => {
      if (controller.signal.aborted) return;
      state.battleStreamAbort = null;
      state.battleStreaming = false;
      showMessage(`实时推送不可用，已切回轮询：${error.message}`);
      scheduleBattleNext(initialResult);
    });
}

async function readBattleStream(battleId, logId, controller) {
  const response = await fetch(`/api/battles/${battleId}/stream`, {
    headers: { Authorization: `Bearer ${state.token}` },
    signal: controller.signal
  });
  if (!response.ok || !response.body) {
    throw new Error(`请求失败：${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split("\n\n");
    buffer = parts.pop() || "";
    for (const part of parts) {
      await handleBattleStreamEvent(part, logId);
    }
  }
  if (buffer.trim()) {
    await handleBattleStreamEvent(buffer, logId);
  }
}

async function handleBattleStreamEvent(rawEvent, logId) {
  const dataLine = rawEvent.split("\n").find(line => line.startsWith("data:"));
  if (!dataLine) return;
  const result = JSON.parse(dataLine.substring("data:".length).trim());
  renderBattle(result, logId);
  if (result.status === "finished") {
    if (state.battleStreamAbort) {
      state.battleStreamAbort.abort();
      state.battleStreamAbort = null;
    }
    state.battleStreaming = false;
    await loadCharacter();
    if (state.view === "maps" && state.currentSceneMapId) await loadMapScene(state.currentSceneMapId);
    if (state.view === "maps") await loadMapTaskTracker();
    if (state.view === "tasks") await loadTasks();
    if (state.view === "bosses") await loadBosses();
    if (state.view === "worldBosses") await loadWorldBosses();
    scheduleNextEncounter();
  }
}

function hpPercent(unit) {
  if (!unit || !unit.maxHp) return 0;
  return Math.max(0, Math.min(100, Math.round(unit.hp * 100 / unit.maxHp)));
}

function firstAliveEnemyId(result) {
  const enemy = (result.enemies || []).find(item => item.selectable) || (result.enemies || []).find(item => item.alive);
  return enemy ? enemy.id : null;
}

function selectedEnemyAlive(result) {
  return !!(state.selectedBattleTargetId
    && (result.enemies || []).some(item => item.id === state.selectedBattleTargetId && item.alive && item.selectable !== false));
}

function renderUnit(unit) {
  const hp = hpPercent(unit);
  const className = state.character && state.character.className ? state.character.className : "warrior";
  const portrait = unit && unit.monsterId ? monsterImagePath(unit.monsterId) : classImagePath(className);
  return `
    <div class="battle-unit media-item compact-media">
      <img class="asset-thumb" src="${portrait}" alt="${unit.name}" onerror="assetFallback(this)">
      <div class="media-body">
        <strong>${unit.name}</strong>
        <div class="hp-track"><div class="hp-fill" style="--hp:${hp}%"></div></div>
        <small>生命 ${unit.hp}/${unit.maxHp} / 攻击 ${unit.attack} / 防御 ${unit.defense} / 攻速 ${unit.attackSpeed}</small>
      </div>
    </div>
  `;
}

function targetTypeText(type) {
  if (type === "all") return "群体";
  if (type === "random3") return "随机多体";
  if (type === "front_row") return "前排";
  if (type === "back_row") return "后排";
  return "单体";
}

function renderEnemyUnit(enemy) {
  const selected = state.selectedBattleTargetId === enemy.id;
  const hp = hpPercent(enemy);
  const disabled = !enemy.alive || enemy.selectable === false;
  const badge = selected ? "已选" : (enemy.guarded ? "受保护" : (enemy.row === "back" ? "后排" : "前排"));
  return `
    <button class="enemy-unit ${enemy.alive ? "" : "defeated"} ${enemy.guarded ? "guarded" : ""} ${enemy.currentTarget ? "current" : ""} ${selected ? "selected" : ""}"
      type="button"
      onclick="selectBattleTarget('${enemy.id}')"
      ${disabled ? "disabled" : ""}>
      <img class="enemy-avatar" src="${monsterImagePath(enemy.monsterId)}" alt="${enemy.name}" onerror="assetFallback(this)">
      <div class="enemy-head">
        <strong>${enemy.name}</strong>
        <span>${badge}</span>
      </div>
      <div class="hp-track"><div class="hp-fill" style="--hp:${hp}%"></div></div>
      <small>生命 ${enemy.hp}/${enemy.maxHp}</small>
      <small>攻 ${enemy.attack} / 防 ${enemy.defense} / 速 ${enemy.attackSpeed}</small>
    </button>
  `;
}

function selectBattleTarget(enemyId) {
  state.selectedBattleTargetId = enemyId;
  if (state.currentBattle && state.currentBattle.lastResult) {
    renderBattle(state.currentBattle.lastResult, state.currentBattle.logId);
  }
}

function renderEnemyBoard(result) {
  const enemies = result.enemies && result.enemies.length ? result.enemies : [{
    id: result.monster.id,
    monsterId: result.monster.id,
    name: result.monster.name,
    row: "front",
    alive: result.monster.hp > 0,
    currentTarget: true,
    hp: result.monster.hp,
    maxHp: result.monster.maxHp,
    attack: result.monster.attack,
    defense: result.monster.defense,
    attackSpeed: result.monster.attackSpeed
  }];
  return `
    <div class="enemy-board">
      <div class="battle-board-title">
        <strong>敌方阵列</strong>
        <small>${enemies.filter(item => item.alive).length}/${enemies.length} 存活</small>
      </div>
      <div class="enemy-grid">${enemies.map(renderEnemyUnit).join("")}</div>
    </div>
  `;
}

function renderBattleBoard(result) {
  return `
    <div class="battle-board">
      <div class="player-board">
        <div class="battle-board-title"><strong>我方</strong><small>${result.nextActor === "player" ? "行动" : "等待"}</small></div>
        ${renderUnit(result.player)}
      </div>
      ${renderEnemyBoard(result)}
    </div>
  `;
}

function renderBattleAction(action) {
  const targets = action.targets || [];
  const targetText = targets.length
    ? targets.map(target => `${target.name} -${target.damage}`).join("，")
    : action.message;
  return `
    <div class="log-entry battle-action ${action.actor || "system"}">
      <div class="battle-action-meta">
        <span>第 ${action.round} 轮</span>
        <strong>${targetTypeText(action.targetType)}</strong>
      </div>
      <div>${action.message}</div>
      ${targets.length ? `<small>${targetText}</small>` : ""}
    </div>
  `;
}

function battleBonusText(result) {
  const parts = [];
  if (result.bonusExp) parts.push(`经验 +${result.bonusExp}`);
  if (result.bonusGold) parts.push(`金币 +${result.bonusGold}`);
  return parts.length ? `（活动加成：${parts.join("，")}）` : "";
}

function renderBattle(result, logId) {
  const node = $(logId);
  if (!node) return;
  if (!selectedEnemyAlive(result)) {
    state.selectedBattleTargetId = firstAliveEnemyId(result);
  }
  if (state.currentBattle && state.currentBattle.id === result.battleId) {
    state.currentBattle.lastResult = result;
  }
  const title = result.status === "finished"
    ? (result.win ? `战斗胜利，获得经验 ${result.expGained}，金币 ${result.goldGained}${battleBonusText(result)}` : "战斗失败")
    : `第 ${result.round} 轮，等待 ${result.nextActor === "monster" ? "怪物" : "玩家"} 行动`;
  const actions = result.actions || [];
  node.innerHTML = `
    <div class="battle-summary">
      ${renderBattleBoard(result)}
      <div class="item">
        <div class="item-header"><strong>${title}</strong><small>${textOf(battleStatusText, result.status, "战斗中")}</small></div>
        <div class="item-actions">
          ${state.autoEncounter ? `<button class="secondary" onclick="stopAutoEncounter('Auto hunt stopped')">Stop auto hunt</button>` : ""}
          ${renderBattleSkillBar(result)}
          ${result.status === "running" && !state.battleStreaming ? `<button class="secondary" onclick="nextBattle()">立即推进</button>` : ""}
        </div>
      </div>
    </div>
    ${actions.map(renderBattleAction).join("")}
  `;
  if (logId === "battleLog") syncMobileMapLogs();
}

function renderBattleSkillBar(result) {
  const skills = (result.skills || []).sort((a, b) => (a.skillSlot || 0) - (b.skillSlot || 0));
  if (!skills.length || result.status !== "running") return "";
  return `
    <div class="battle-skill-bar">
      ${skills.map(skill => `
        <button class="secondary skill-cast-button" type="button"
          onclick="castBattleSkill('${skill.id}')"
          ${skill.ready ? "" : "disabled"}
          title="${skill.description || skill.name}">
          <img src="${skillImagePath(skill.id)}" alt="" onerror="assetFallback(this)">
          ${skill.skillSlot}.${skill.name} · ${targetTypeText(skill.targetType)}${skill.cooldownRemaining ? ` (${skill.cooldownRemaining})` : ""}
        </button>
      `).join("")}
    </div>
  `;
}

async function castBattleSkill(skillId) {
  if (!state.currentBattle) return;
  stopBattleTimer();
  const result = await api(`/api/battles/${state.currentBattle.id}/skill`, {
    method: "POST",
    body: JSON.stringify({ skillId, targetId: state.selectedBattleTargetId })
  });
  renderBattle(result, state.currentBattle.logId);
  if (result.status === "finished") {
    await loadCharacter();
    if (state.view === "maps" && state.currentSceneMapId) await loadMapScene(state.currentSceneMapId);
    if (state.view === "maps") await loadMapTaskTracker();
    if (state.view === "tasks") await loadTasks();
    if (state.view === "bosses") await loadBosses();
    if (state.view === "worldBosses") await loadWorldBosses();
    scheduleNextEncounter();
    return;
  }
  scheduleBattleNext(result);
}

function taskProgressText(task) {
  if (task.type === "level_reach") {
    return `等级 ${task.currentCount}/${task.targetLevel}`;
  }
  if (task.type === "kill_monster") {
    return `${task.targetName} ${task.currentCount}/${task.targetCount}`;
  }
  if (task.type === "talk_npc") {
    return `${task.targetName} ${task.currentCount}/${task.targetCount || 1}`;
  }
  if (task.type === "explore_event") {
    return `探索 ${task.targetName} ${task.currentCount}/${task.targetCount || 1}`;
  }
  return `${task.currentCount}/${task.targetCount || 1}`;
}

function taskRewardText(task) {
  const rewards = task.rewards || {};
  const parts = [];
  if (rewards.exp) parts.push(`经验 ${rewards.exp}`);
  if (rewards.gold) parts.push(`金币 ${rewards.gold}`);
  if (rewards.items && rewards.items.length) {
    rewards.items.forEach(item => parts.push(`${item.name} x${item.quantity}`));
  }
  return parts.length ? parts.join(" / ") : "无";
}

function taskMapName(task) {
  return taskTargetMapName(task) || (state.currentScene ? state.currentScene.name : "");
}

function taskStatusClass(task) {
  if (task.locked) return "locked";
  return task.status === 1 ? "ready" : "";
}

function taskStatusGuide(task, taskMap) {
  if (task.status === 1 && !task.locked) return "奖励已达成，可直接领取。";
  if (task.locked) {
    const names = missingPreTaskNames(task, taskMap);
    return names.length ? `先完成：${names.join("、")}` : "完成前置任务后解锁。";
  }
  if (isTaskTargetOnAnotherMap(task)) return `目标在${taskTargetMapName(task)}，可先切换地图查看。`;
  if (task.targetMapId && state.currentScene && task.targetMapId === state.currentScene.id) return "目标就在当前地图。";
  if (task.targetMapId) return `目标在${taskTargetMapName(task)}。`;
  if (task.type === "level_reach") return `继续升级到 ${task.targetLevel} 级。`;
  return "按目标推进任务进度。";
}

function missingPreTaskNames(task, taskMap) {
  const map = taskMap || {};
  return (task.preTaskIds || [])
    .map(id => map[id])
    .filter(item => !item || item.status !== 2)
    .map(item => item ? item.name : "前置任务");
}

function taskRecommendationScore(task) {
  let score = 0;
  if (task.status === 1 && !task.locked) score += 1000;
  if (!task.locked) score += 300;
  if (task.targetMapId && state.currentScene && task.targetMapId === state.currentScene.id) score += 180;
  if (!task.targetMapId && task.type === "level_reach") score += 80;
  if (task.preTaskIds && task.preTaskIds.length) score += 40;
  if (task.locked) score -= 500;
  if (isTaskTargetOnAnotherMap(task)) score -= 30;
  const remaining = Math.max(0, (task.targetCount || task.targetLevel || 1) - (task.currentCount || 0));
  score -= Math.min(remaining, 20);
  return score;
}

function compareTrackedTasks(a, b) {
  const scoreDiff = taskRecommendationScore(b) - taskRecommendationScore(a);
  if (scoreDiff !== 0) return scoreDiff;
  return String(a.name).localeCompare(String(b.name), "zh-CN");
}

function mapTaskFilterOptions() {
  return [
    { id: "all", label: "全部" },
    { id: "ready", label: "可领取" },
    { id: "active", label: "进行中" },
    { id: "current", label: "当前地图" },
    { id: "remote", label: "跨地图" }
  ];
}

function filterTrackedTasks(tasks, filter) {
  const candidates = (tasks || []).filter(task => task.status !== 2);
  return candidates.filter(task => {
    if (filter === "ready") return task.status === 1 && !task.locked;
    if (filter === "active") return task.status === 0 && !task.locked;
    if (filter === "current") return !!(task.targetMapId && state.currentScene && task.targetMapId === state.currentScene.id);
    if (filter === "remote") return isTaskTargetOnAnotherMap(task);
    return true;
  }).sort(compareTrackedTasks);
}

function pickTrackedTasks(tasks) {
  return filterTrackedTasks(tasks, state.mapTaskFilter).slice(0, 3);
}

function setMapTaskFilter(filter) {
  state.mapTaskFilter = filter || "all";
  loadMapTaskTracker().catch(error => showMessage(error.message));
}

function renderMapTaskFilters(tasks) {
  const available = (tasks || []).filter(task => task.status !== 2);
  const counts = {
    all: available.length,
    ready: filterTrackedTasks(tasks, "ready").length,
    active: filterTrackedTasks(tasks, "active").length,
    current: filterTrackedTasks(tasks, "current").length,
    remote: filterTrackedTasks(tasks, "remote").length
  };
  return `
    <div class="task-filter-bar" role="tablist" aria-label="任务筛选">
      ${mapTaskFilterOptions().map(option => `
        <button class="${state.mapTaskFilter === option.id ? "active" : ""}" type="button" onclick="setMapTaskFilter('${option.id}')" aria-pressed="${state.mapTaskFilter === option.id}">
          ${option.label}<span>${counts[option.id] || 0}</span>
        </button>
      `).join("")}
    </div>
  `;
}

async function loadMapTaskTracker() {
  const box = $("mapTaskTracker");
  if (!box) return;
  await ensureMapNames().catch(() => {});
  const tasks = await api("/api/tasks");
  const taskMap = Object.fromEntries(tasks.map(task => [task.id, task]));
  const tracked = pickTrackedTasks(tasks);
  state.tasks = tasks;
  state.mapTrackedTasks = tracked;
  updateMapTargetPointIds(tracked);
  if (state.currentScene && $("mapCanvas")) renderMapScene(state.currentScene);
  const emptyText = state.mapTaskFilter === "all" ? "暂无可追踪任务" : "当前筛选暂无任务";
  box.innerHTML = `
    ${renderMapTaskFilters(tasks)}
    ${tracked.length ? tracked.map(task => renderMapTaskCard(task, taskMap)).join("") : `<div class="map-task-card"><small>${emptyText}</small></div>`}
  `;
}

function renderMapTaskCard(task, taskMap) {
  const targetPoint = findTaskTargetPoint(task);
  const targetOnAnotherMap = isTaskTargetOnAnotherMap(task);
  const targetMap = taskTargetMapName(task);
  const canFocus = !task.locked && targetPoint;
  const canGoMap = !task.locked && targetOnAnotherMap;
  return `
    <div class="map-task-card ${targetOnAnotherMap ? "remote-target" : ""}">
      <div class="item-header">
        <strong>${task.name}</strong>
        <span class="status-badge ${taskStatusClass(task)}">${task.statusText}</span>
      </div>
      <small>目标：${taskProgressText(task)}</small>
      ${task.guide ? `<small class="task-guide">${task.guide}</small>` : ""}
      <small class="task-guide">${taskStatusGuide(task, taskMap)}</small>
      ${targetOnAnotherMap ? `<small class="task-target-map">目标地图：${targetMap}</small>` : ""}
      <small>奖励：${taskRewardText(task)}</small>
      <div class="item-actions">
        ${canGoMap ? `<button class="secondary" type="button" onclick="goTaskTargetMap('${task.id}')">前往目标地图</button>` : ""}
        ${canFocus ? `<button class="secondary" type="button" onclick="focusTaskTarget('${task.id}')">查看目标</button>` : ""}
        ${task.status === 1 ? `<button type="button" onclick="claimTask('${task.id}')">领取奖励</button>` : ""}
      </div>
    </div>
  `;
}

function pickRecommendedTask(tasks) {
  return (tasks || [])
    .filter(task => task.status !== 2 && !task.locked)
    .sort(compareTrackedTasks)[0] || null;
}

function taskActionTitle(task) {
  if (!task) return "暂无推荐任务";
  if (task.status === 1 && !task.locked) return `领取 ${task.name}`;
  if (task.type === "level_reach") return `提升到 ${task.targetLevel} 级`;
  return `推进 ${task.name}`;
}

function taskActionDetail(task, taskMap) {
  if (!task) return "当前任务链已完成。";
  const parts = [taskProgressText(task), taskStatusGuide(task, taskMap)];
  if (task.guide) parts.push(task.guide);
  return parts.filter(Boolean).join(" / ");
}

function skillRecommendation(skills) {
  const list = Array.isArray(skills) ? skills : [];
  const learnable = list.find(skill => skill.canLearn);
  if (learnable) {
    return {
      title: `学习 ${learnable.name}`,
      detail: `Lv.${learnable.requiredLevel} 可学 / ${learnable.description || "提升战斗能力"}`,
      action: "skills"
    };
  }
  const unslotted = list.find(skill => skill.learned && skill.type === "active" && !skill.skillSlot);
  if (unslotted) {
    return {
      title: `装配 ${unslotted.name}`,
      detail: "主动技能放入技能栏后，在线战斗可手动释放。",
      action: "skills"
    };
  }
  const upgradable = list.find(skill => skill.canUpgrade);
  if (upgradable) {
    return {
      title: `升级 ${upgradable.name}`,
      detail: `消耗 ${upgradable.upgradeGold} 金币提升技能等级。`,
      action: "skills"
    };
  }
  return null;
}

function renderTaskGuidePanel(tasks, skills, taskMap) {
  const task = pickRecommendedTask(tasks);
  const skillTip = skillRecommendation(skills);
  return `
    <div class="task-guide-next">
      <div>
        <span class="status-badge ready">推荐</span>
        <strong>${taskActionTitle(task)}</strong>
        <small>${taskActionDetail(task, taskMap)}</small>
      </div>
      <div class="item-actions">
        ${task && task.status === 1 && !task.locked ? `<button onclick="claimTask('${task.id}')">领取奖励</button>` : ""}
        ${task && !task.locked && task.status !== 1 && task.targetMapId ? `<button class="secondary" onclick="openTaskTarget('${task.id}')">前往目标</button>` : ""}
      </div>
    </div>
    ${skillTip ? `
      <div class="task-guide-skill">
        <span>技能建议</span>
        <strong>${skillTip.title}</strong>
        <small>${skillTip.detail}</small>
        <button class="secondary" onclick="switchView('${skillTip.action}')">查看技能</button>
      </div>
    ` : ""}
  `;
}

function taskChainNodeClass(task) {
  if (task.id === state.selectedStoryTaskId) return "selected";
  if (task.locked) return "locked";
  if (task.status === 2) return "done";
  if (task.status === 1) return "ready";
  return "active";
}

function renderTaskChainOverview(tasks) {
  const list = tasks || [];
  const done = list.filter(task => task.status === 2).length;
  const ready = list.filter(task => task.status === 1 && !task.locked).length;
  return `
    <div class="task-section-header">
      <strong>任务链</strong>
      <small>${done}/${list.length} 已完成${ready ? ` / ${ready} 可领取` : ""}</small>
    </div>
    <div class="task-chain-strip">
      ${list.map((task, index) => `
        <button class="task-chain-node ${taskChainNodeClass(task)}" type="button" onclick="selectTaskStory('${task.id}')">
          <span>${index + 1}</span>
          <strong>${task.name}</strong>
        </button>
      `).join("")}
    </div>
  `;
}

function replayableTasks(tasks) {
  return (tasks || []).filter(task => !task.locked || task.status === 2);
}

function pickStoryTask(tasks) {
  const list = replayableTasks(tasks);
  const selected = list.find(task => task.id === state.selectedStoryTaskId);
  if (selected) return selected;
  const latestDone = [...list].reverse().find(task => task.status === 2);
  const next = pickRecommendedTask(list);
  const fallback = latestDone || next || list[0] || null;
  state.selectedStoryTaskId = fallback ? fallback.id : null;
  return fallback;
}

function renderTaskStoryReplay(tasks, taskMap) {
  const list = replayableTasks(tasks);
  const task = pickStoryTask(tasks);
  if (!task) {
    return `<div class="task-section-header"><strong>剧情回看</strong><small>暂无可回看的任务</small></div>`;
  }
  return `
    <div class="task-section-header">
      <strong>剧情回看</strong>
      <small>${task.name} / ${task.statusText}</small>
    </div>
    <div class="task-story-body">
      <div class="task-story-tabs">
        ${list.map(item => `
          <button class="${item.id === task.id ? "active" : ""}" type="button" onclick="selectTaskStory('${item.id}')">${item.name}</button>
        `).join("")}
      </div>
      <div class="task-story-content">
        <strong>${task.name}</strong>
        ${task.story ? `<p>${task.story}</p>` : ""}
        <small>${taskActionDetail(task, taskMap)}</small>
        ${task.targetMapId ? `<small>目标地图：${taskMapName(task)}</small>` : ""}
        <small>奖励：${taskRewardText(task)}</small>
      </div>
    </div>
  `;
}

function renderTaskPanels(tasks, skills, taskMap) {
  $("taskGuidePanel").innerHTML = renderTaskGuidePanel(tasks, skills, taskMap);
  $("taskChainOverview").innerHTML = renderTaskChainOverview(tasks);
  $("taskStoryReplay").innerHTML = renderTaskStoryReplay(tasks, taskMap);
}

function selectTaskStory(taskId) {
  state.selectedStoryTaskId = taskId;
  const taskMap = Object.fromEntries((state.tasks || []).map(task => [task.id, task]));
  renderTaskPanels(state.tasks || [], state.skills || [], taskMap);
}

async function loadTasks() {
  await ensureMapNames().catch(() => {});
  const [tasks, skills] = await Promise.all([
    api("/api/tasks"),
    api("/api/skills").catch(() => [])
  ]);
  state.tasks = tasks;
  state.skills = skills;
  const taskMap = Object.fromEntries(tasks.map(task => [task.id, task]));
  const sorted = [...tasks].sort((a, b) => {
    if (a.status === 2 && b.status !== 2) return 1;
    if (a.status !== 2 && b.status === 2) return -1;
    return compareTrackedTasks(a, b);
  });
  renderTaskPanels(tasks, skills, taskMap);
  $("taskList").innerHTML = sorted.length ? sorted.map(task => `
    <div class="item">
      <div class="item-header">
        <strong>${task.name}</strong>
        <span class="status-badge ${taskStatusClass(task)}">${task.statusText}</span>
      </div>
      ${task.story ? `<small>${task.story}</small>` : ""}
      <small>目标：${taskProgressText(task)}</small>
      ${task.guide ? `<small>${task.guide}</small>` : ""}
      <small>${taskStatusGuide(task, taskMap)}</small>
      ${task.targetMapId ? `<small>目标地图：${taskMapName(task)}</small>` : ""}
      <small>奖励：${taskRewardText(task)}</small>
      <div class="item-actions">
        ${task.status === 1 && !task.locked ? `<button onclick="claimTask('${task.id}')">领取奖励</button>` : ""}
        ${!task.locked && task.status !== 2 && task.targetMapId ? `<button class="secondary" onclick="openTaskTarget('${task.id}')">前往目标</button>` : ""}
        ${!task.locked ? `<button class="secondary" onclick="selectTaskStory('${task.id}')">回看剧情</button>` : ""}
      </div>
    </div>
  `).join("") : `<div class="item"><small>暂无任务</small></div>`;
}

async function claimTask(taskId) {
  const result = await api(`/api/tasks/${taskId}/claim`, { method: "POST" });
  const itemText = result.items && result.items.length
    ? `，获得 ${result.items.map(item => `${item.name} x${item.quantity}`).join("，")}`
    : "";
  showMessage(`领取成功：经验 ${result.expGained}，金币 ${result.goldGained}${itemText}`);
  await loadCharacter();
  if (state.view === "maps") await loadMapTaskTracker();
  await loadTasks();
}

async function loadInventory() {
  const [inventoryPayload, equipment] = await Promise.all([
    api("/api/inventory"),
    api("/api/equipment")
  ]);
  const inventory = normalizeInventoryPayload(inventoryPayload);
  state.inventoryItems = inventory.items;
  state.equippedItemsBySlot = indexEquippedItems(equipment.items || []);
  const items = inventory.items;
  if (!items.some(item => item.id === state.selectedInventoryItemId)) {
    state.selectedInventoryItemId = items.length ? items[0].id : null;
  }
  const selected = items.find(item => item.id === state.selectedInventoryItemId) || null;
  $("inventoryList").innerHTML = `
    <div class="inventory-shell">
      <div class="inventory-header">
        <div>
          <strong>背包</strong>
          <small>${inventory.usedSlots}/${inventory.capacity} 格，剩余 ${inventory.remainingSlots} 格</small>
        </div>
        <small>点击格子查看详情</small>
      </div>
      <div class="inventory-grid">
        ${renderInventorySlots(items, inventory.capacity)}
      </div>
      <div class="inventory-detail">
        ${selected ? renderInventoryDetail(selected) : `<small>背包为空，去地图里打怪或探索获得物品。</small>`}
      </div>
    </div>
  `;
}

function normalizeInventoryPayload(payload) {
  if (Array.isArray(payload)) {
    const capacity = 40;
    return {
      capacity,
      usedSlots: payload.length,
      remainingSlots: Math.max(0, capacity - payload.length),
      items: payload
    };
  }
  const items = payload && Array.isArray(payload.items) ? payload.items : [];
  const capacity = payload && payload.capacity ? payload.capacity : 40;
  return {
    capacity,
    usedSlots: payload && typeof payload.usedSlots === "number" ? payload.usedSlots : items.length,
    remainingSlots: payload && typeof payload.remainingSlots === "number" ? payload.remainingSlots : Math.max(0, capacity - items.length),
    items
  };
}

function itemIconPath(item) {
  return generatedAssetPath("items", item.itemId);
}

function itemIconFallback(img) {
  const itemId = img.dataset.itemId || "item_default";
  img.onerror = () => assetFallback(img, "/assets/items/item_default.svg");
  img.src = `/assets/items/${itemId}.svg`;
}

function inventoryStatsText(item) {
  const parts = [];
  if (item.slot) parts.push(`部位 ${textOf(slotText, item.slot, "-")}`);
  if (item.requiredLevel) parts.push(`Lv.${item.requiredLevel}`);
  if (item.attack) parts.push(`攻击 ${item.attack}`);
  if (item.defense) parts.push(`防御 ${item.defense}`);
  if (item.attackSpeed) parts.push(`攻速 ${item.attackSpeed}`);
  if (item.hp) parts.push(`生命 ${item.hp}`);
  if (item.skillTriggerBonus) parts.push(`技能触发 +${Math.round(item.skillTriggerBonus * 100)}%`);
  if (item.buffHp) parts.push(`战前生命 +${item.buffHp}`);
  if (item.buffAttack) parts.push(`战前攻击 +${item.buffAttack}`);
  if (item.buffDefense) parts.push(`战前防御 +${item.buffDefense}`);
  if (item.buffAttackSpeed) parts.push(`战前攻速 +${item.buffAttackSpeed}`);
  parts.push(`售价 ${item.sellGold}`);
  return parts.join(" / ");
}

function consumableBuffText(item) {
  const parts = [];
  if (item.buffHp) parts.push(`生命 +${item.buffHp}`);
  if (item.buffAttack) parts.push(`攻击 +${item.buffAttack}`);
  if (item.buffDefense) parts.push(`防御 +${item.buffDefense}`);
  if (item.buffAttackSpeed) parts.push(`攻速 +${item.buffAttackSpeed}`);
  return parts.join("，");
}

function formatAffixValue(affix) {
  if (!affix || !affix.stat) return "";
  const label = textOf(affixStatText, affix.stat, affix.stat);
  const value = Number(affix.value || 0);
  if (affix.stat === "skillTriggerBonus") {
    return `${label} +${Math.round(value * 100)}%`;
  }
  return `${label} +${Math.round(value)}`;
}

function affixSummaryText(item) {
  const affixes = item && Array.isArray(item.affixes) ? item.affixes : [];
  return affixes.map(formatAffixValue).filter(Boolean).join(" / ");
}

function renderAffixList(affixes) {
  const values = (Array.isArray(affixes) ? affixes : []).map(formatAffixValue).filter(Boolean);
  if (!values.length) return "";
  return `
    <div class="affix-list">
      ${values.map(value => `<span>${value}</span>`).join("")}
    </div>
  `;
}

function renderInventorySlots(items, size) {
  const slots = [];
  for (let index = 0; index < size; index++) {
    const item = items[index];
    if (!item) {
      slots.push(`<div class="inventory-slot empty" aria-label="空格"></div>`);
      continue;
    }
    const active = state.selectedInventoryItemId === item.id ? "active" : "";
    slots.push(`
      <button class="inventory-slot filled ${active} quality-${item.quality || "common"}" type="button"
        onclick="selectInventoryItem(${item.id})"
        onmouseenter="showInventoryTooltip(event, ${item.id})"
        onmousemove="moveInventoryTooltip(event)"
        onmouseleave="hideInventoryTooltip()"
        onfocus="showInventoryTooltip(event, ${item.id})"
        onblur="hideInventoryTooltip()"
        aria-label="${item.name}">
        ${item.enhanceLevel ? `<span class="slot-enhance">+${item.enhanceLevel}</span>` : ""}
        <img src="${itemIconPath(item)}" data-item-id="${item.itemId}" alt="${item.name}" loading="lazy" onerror="itemIconFallback(this)">
        ${item.quantity > 1 ? `<span class="slot-quantity">${item.quantity}</span>` : ""}
      </button>
    `);
  }
  return slots.join("");
}

function indexEquippedItems(items) {
  const result = {};
  (items || []).forEach(item => {
    if (item.slot) result[item.slot] = item;
  });
  return result;
}

function showInventoryTooltip(event, id) {
  const item = state.inventoryItems.find(entry => entry.id === id);
  if (!item) return;
  const tooltip = ensureInventoryTooltip();
  tooltip.innerHTML = renderInventoryTooltip(item);
  tooltip.classList.remove("hidden");
  moveInventoryTooltip(event);
}

function moveInventoryTooltip(event) {
  const tooltip = $("inventoryTooltip");
  if (!tooltip || tooltip.classList.contains("hidden")) return;
  const padding = 14;
  const offset = 16;
  const width = tooltip.offsetWidth || 280;
  const height = tooltip.offsetHeight || 180;
  const anchor = event.currentTarget && event.currentTarget.getBoundingClientRect
    ? event.currentTarget.getBoundingClientRect()
    : null;
  const pointerX = typeof event.clientX === "number" && event.clientX > 0
    ? event.clientX
    : (anchor ? anchor.right : padding);
  const pointerY = typeof event.clientY === "number" && event.clientY > 0
    ? event.clientY
    : (anchor ? anchor.top : padding);
  let x = pointerX + offset;
  let y = pointerY + offset;
  if (x + width + padding > window.innerWidth) {
    x = pointerX - width - offset;
  }
  if (y + height + padding > window.innerHeight) {
    y = pointerY - height - offset;
  }
  tooltip.style.left = `${Math.max(padding, x)}px`;
  tooltip.style.top = `${Math.max(padding, y)}px`;
}

function hideInventoryTooltip() {
  const tooltip = $("inventoryTooltip");
  if (tooltip) tooltip.classList.add("hidden");
}

function ensureInventoryTooltip() {
  let tooltip = $("inventoryTooltip");
  if (!tooltip) {
    tooltip = document.createElement("div");
    tooltip.id = "inventoryTooltip";
    tooltip.className = "inventory-tooltip hidden";
    document.body.appendChild(tooltip);
  }
  return tooltip;
}

function renderInventoryTooltip(item) {
  const equipped = item.type === "equipment" && item.slot ? state.equippedItemsBySlot[item.slot] : null;
  return `
    <div class="tooltip-title">
      <strong>${item.name}${item.quantity > 1 ? ` x${item.quantity}` : ""}</strong>
      <span class="status-badge">${textOf(qualityText, item.quality, "普通")}</span>
    </div>
    <small>${textOf(itemTypeText, item.type, "物品")}${item.slot ? ` / ${textOf(slotText, item.slot, "装备")}` : ""}${item.enhanceLevel ? ` / 强化 +${item.enhanceLevel}` : ""}</small>
    ${item.setName ? `<small>${item.setName}</small>` : ""}
    ${renderAffixList(item.affixes)}
    <div class="tooltip-lines">
      ${renderTooltipStat("等级需求", item.requiredLevel)}
      ${renderTooltipStat("生命", item.hp, equipped && equipped.inventoryItemId !== item.id ? equipped.hp : null)}
      ${renderTooltipStat("攻击", item.attack, equipped && equipped.inventoryItemId !== item.id ? equipped.attack : null)}
      ${renderTooltipStat("防御", item.defense, equipped && equipped.inventoryItemId !== item.id ? equipped.defense : null)}
      ${renderTooltipStat("攻速", item.attackSpeed, equipped && equipped.inventoryItemId !== item.id ? equipped.attackSpeed : null)}
      ${renderTooltipStat("技能触发", item.skillTriggerBonus ? `${Math.round(item.skillTriggerBonus * 100)}%` : "")}
      ${renderTooltipStat("战前生命", item.buffHp)}
      ${renderTooltipStat("战前攻击", item.buffAttack)}
      ${renderTooltipStat("战前防御", item.buffDefense)}
      ${renderTooltipStat("战前攻速", item.buffAttackSpeed)}
      ${renderTooltipStat("售价", item.sellGold)}
    </div>
    ${renderEquipmentCompare(item, equipped)}
  `;
}

function renderTooltipStat(label, value, compareValue = null) {
  if (!value && compareValue === null) return "";
  const diff = compareValue === null ? "" : renderStatDiff(value - compareValue);
  return `<div><span>${label}</span><strong>${value || 0}${diff}</strong></div>`;
}

function renderStatDiff(diff) {
  if (!diff) return `<em class="diff equal">0</em>`;
  const className = diff > 0 ? "up" : "down";
  const sign = diff > 0 ? "+" : "";
  return `<em class="diff ${className}">${sign}${diff}</em>`;
}

function renderEquipmentCompare(item, equipped) {
  if (item.type === "consumable") {
    return `<div class="tooltip-note">使用后在下一场战斗开始时生效，可叠加多瓶效果。</div>`;
  }
  if (item.type !== "equipment") {
    return `<div class="tooltip-note">材料可用于强化或出售。</div>`;
  }
  if (!equipped) {
    return `<div class="tooltip-note">当前部位未穿戴装备。</div>`;
  }
  return `
    <div class="tooltip-compare">
      <small>对比已穿戴：${equipped.name}${equipped.enhanceLevel ? ` +${equipped.enhanceLevel}` : ""}</small>
    </div>
  `;
}

function renderInventoryDetail(item) {
  return `
    <div class="inventory-detail-card quality-${item.quality || "common"}">
      <div class="inventory-detail-icon">
        <img src="${itemIconPath(item)}" data-item-id="${item.itemId}" alt="${item.name}" onerror="itemIconFallback(this)">
      </div>
      <div class="inventory-detail-body">
        <div class="item-header">
          <strong>${item.name}${item.quantity > 1 ? ` x${item.quantity}` : ""}</strong>
          <span class="status-badge">${textOf(qualityText, item.quality, "普通")}</span>
        </div>
        <small>${textOf(itemTypeText, item.type, "物品")}${item.type === "equipment" ? ` / 强化 +${item.enhanceLevel || 0}` : ""}</small>
        ${item.setName ? `<small>套装：${item.setName}</small>` : ""}
        <small>${inventoryStatsText(item)}</small>
        ${renderAffixList(item.affixes)}
        ${item.type === "consumable" ? `<small>使用效果：下一场战斗 ${consumableBuffText(item)}</small>` : ""}
        <div class="item-actions">
          ${item.type === "equipment" ? `<button onclick="equip(${item.id})">穿戴</button>` : ""}
          ${item.type === "equipment" ? `<button class="secondary" onclick="enhanceItem(${item.id})">强化</button>` : ""}
          ${item.type === "equipment" ? `<button class="secondary" onclick="rerollAffixes(${item.id})">重铸</button>` : ""}
          ${item.type === "equipment" ? `<button class="secondary" onclick="decomposeItem(${item.id})">分解</button>` : ""}
          ${item.type === "consumable" ? `<button onclick="useItem(${item.id})">使用</button>` : ""}
          <button class="secondary" onclick="sellItem(${item.id})">出售 1 个</button>
          ${item.type === "material" && item.quantity > 1 ? `<button class="secondary" onclick="sellAllMaterial(${item.id}, ${item.quantity})">出售全部</button>` : ""}
          <button class="secondary" onclick="discardItem(${item.id}, ${item.quantity})">丢弃</button>
        </div>
      </div>
    </div>
  `;
}

function selectInventoryItem(id) {
  state.selectedInventoryItemId = id;
  loadInventory().catch(error => showMessage(error.message));
}

async function sellItem(id) {
  await api(`/api/inventory/${id}/sell`, {
    method: "POST",
    body: JSON.stringify({ quantity: 1 })
  });
  showMessage("出售成功");
  await loadCharacter();
  await loadInventory();
}

async function useItem(id) {
  const result = await api(`/api/inventory/${id}/use`, { method: "POST" });
  const parts = [];
  if (result.bonusHp) parts.push(`生命 +${result.bonusHp}`);
  if (result.bonusAttack) parts.push(`攻击 +${result.bonusAttack}`);
  if (result.bonusDefense) parts.push(`防御 +${result.bonusDefense}`);
  if (result.bonusAttackSpeed) parts.push(`攻速 +${result.bonusAttackSpeed}`);
  showMessage(`已使用 ${result.name}，下一场战斗准备生效：${parts.join("，")}`);
  await loadInventory();
}

async function sellAllMaterial(id, quantity) {
  if (!window.confirm(`确认出售全部 ${quantity} 个该材料？`)) return;
  const result = await api("/api/inventory/materials/sell", {
    method: "POST",
    body: JSON.stringify({ items: [{ inventoryItemId: id, quantity }] })
  });
  showMessage(`出售成功，获得 ${result.goldGained} 金币`);
  await loadCharacter();
  await loadInventory();
}

async function discardItem(id, quantity) {
  const discardQuantity = quantity > 1
    ? Number(window.prompt(`输入要丢弃的数量，最多 ${quantity} 个`, "1"))
    : 1;
  if (!Number.isInteger(discardQuantity) || discardQuantity <= 0 || discardQuantity > quantity) {
    showMessage("丢弃数量不正确");
    return;
  }
  if (!window.confirm(`确认丢弃 ${discardQuantity} 个该物品？丢弃后无法找回。`)) return;
  const result = await api(`/api/inventory/${id}/discard`, {
    method: "POST",
    body: JSON.stringify({ quantity: discardQuantity })
  });
  showMessage(`已丢弃 ${result.name} x${result.discardedQuantity}`);
  await loadInventory();
}

async function equip(id) {
  await api("/api/equipment/equip", {
    method: "POST",
    body: JSON.stringify({ inventoryItemId: id })
  });
  state.selectedInventoryItemId = id;
  await loadCharacter();
  await loadInventory();
  await loadEquipment();
}

async function enhanceItem(id) {
  const result = await api("/api/equipment/enhance", {
    method: "POST",
    body: JSON.stringify({ inventoryItemId: id })
  });
  const materialText = result.materialCosts && result.materialCosts.length
    ? `，材料 ${result.materialCosts.map(item => `${item.name} x${item.quantity}`).join("，")}`
    : "";
  showMessage(`强化成功：+${result.enhanceLevel}，消耗 ${result.goldCost} 金币${materialText}`);
  await loadCharacter();
  if (state.view === "inventory") await loadInventory();
  if (state.view === "equipment") await loadEquipment();
}

async function rerollAffixes(id) {
  if (!window.confirm("确认消耗对应品质精华重铸这件装备的随机词条？")) return;
  const result = await api("/api/equipment/reroll-affixes", {
    method: "POST",
    body: JSON.stringify({ inventoryItemId: id })
  });
  const cost = result.materialCost ? `，消耗 ${result.materialCost.name} x${result.materialCost.quantity}` : "";
  const affixes = result.affixes && result.affixes.length
    ? result.affixes.map(formatAffixValue).join("，")
    : "无词条";
  showMessage(`重铸成功：${affixes}${cost}`);
  await loadCharacter();
  if (state.view === "inventory") await loadInventory();
  if (state.view === "equipment") await loadEquipment();
}

async function decomposeItem(id) {
  if (!window.confirm("确认分解这件装备？分解后装备会消失，并返还对应品质的装备精华。")) return;
  const result = await api("/api/equipment/decompose", {
    method: "POST",
    body: JSON.stringify({ inventoryItemId: id })
  });
  const materials = result.materials && result.materials.length
    ? result.materials.map(item => `${item.name} x${item.quantity}`).join("，")
    : "无材料";
  showMessage(`已分解 ${result.name}${result.enhanceLevel ? ` +${result.enhanceLevel}` : ""}，获得 ${materials}`);
  state.selectedInventoryItemId = null;
  await loadCharacter();
  await loadInventory();
  if (state.view === "equipment") await loadEquipment();
}

function setBonusStatText(bonus) {
  const parts = [];
  if (bonus.attack) parts.push(`攻击 +${bonus.attack}`);
  if (bonus.defense) parts.push(`防御 +${bonus.defense}`);
  if (bonus.attackSpeed) parts.push(`攻速 +${bonus.attackSpeed}`);
  if (bonus.hp) parts.push(`生命 +${bonus.hp}`);
  return parts.length ? parts.join(" / ") : "无属性";
}

function renderEquipmentSetBonuses(bonuses) {
  if (!bonuses || !bonuses.length) return "";
  return `
    <div class="equipment-set-list">
      ${bonuses.map(bonus => `
        <div class="set-bonus ${bonus.active ? "active" : ""}">
          <span>${bonus.setName} ${bonus.pieces}/${bonus.requiredPieces}</span>
          <strong>${setBonusStatText(bonus)}</strong>
        </div>
      `).join("")}
    </div>
  `;
}

async function loadEquipment() {
  const data = await api("/api/equipment");
  $("equipmentStats").innerHTML = [
    ["生命", data.hp],
    ["攻击", data.attack],
    ["防御", data.defense],
    ["攻速", data.attackSpeed],
    ["战力", data.power]
  ].map(([label, value]) => `<div class="stat"><span>${label}</span><strong>${value}</strong></div>`).join("");
  $("equipmentList").innerHTML = data.items.length ? data.items.map(item => `
    <div class="item media-item">
      <img class="asset-thumb" src="${generatedAssetPath("items", item.itemId)}" data-item-id="${item.itemId}" alt="${item.name}" onerror="itemIconFallback(this)">
      <div class="media-body">
        <div class="item-header"><strong>${item.name}</strong><small>${textOf(slotText, item.slot, "装备")}</small></div>
        ${item.setName ? `<small>套装：${item.setName}</small>` : ""}
        <small>强化 +${item.enhanceLevel || 0} / 攻击 ${item.attack} / 防御 ${item.defense} / 攻速 ${item.attackSpeed} / 生命 ${item.hp}${item.skillTriggerBonus ? ` / 技能触发 +${Math.round(item.skillTriggerBonus * 100)}%` : ""}</small>
        ${renderAffixList(item.affixes)}
        <div class="item-actions">
          <button class="secondary" onclick="enhanceItem(${item.inventoryItemId})">强化</button>
          <button class="secondary" onclick="rerollAffixes(${item.inventoryItemId})">重铸</button>
          <button class="secondary" onclick="unequip('${item.slot}')">卸下</button>
        </div>
      </div>
    </div>
  `).join("") : `<div class="item"><small>未穿戴装备</small></div>`;
  $("equipmentList").insertAdjacentHTML("beforeend", renderEquipmentSetBonuses(data.setBonuses));
}

function skillTypeText(type) {
  return type === "passive" ? "被动" : "主动";
}

function percentText(value) {
  return `${Math.round((value || 0) * 100)}%`;
}

async function loadSkills() {
  const skills = await api("/api/skills");
  const activeSkills = skills.filter(skill => skill.type === "active" && skill.learned);
  $("skillList").innerHTML = skills.length ? skills.map(skill => `
    <div class="item media-item skill-card">
      <img class="asset-thumb" src="${skillImagePath(skill.id)}" alt="${skill.name}" onerror="assetFallback(this)">
      <div class="media-body">
        <div class="item-header">
          <strong>${skill.name}</strong>
          <span class="status-badge ${skill.learned ? "ready" : ""}">${skill.learned ? `Lv.${skill.level}` : `Lv.${skill.requiredLevel} 可学`}</span>
        </div>
        <small>${skill.description}</small>
        <div class="skill-meta">
          <span>${skillTypeText(skill.type)}</span>
          ${skill.type === "active" ? `<span>目标 ${targetTypeText(skill.targetType)}</span><span>触发 ${percentText(skill.triggerChance)}</span><span>冷却 ${skill.cooldownRounds} 回合</span><span>系数 ${skill.damageMultiplier.toFixed(2)}</span><span>固定 ${skill.flatDamage}</span>` : ""}
          ${skill.dotRounds ? `<span>持续 ${skill.dotRounds} 回合 / ${skill.dotDamage} 伤害</span>` : ""}
          ${skill.passiveHp ? `<span>生命 +${skill.passiveHp}</span>` : ""}
          ${skill.passiveAttack ? `<span>攻击 +${skill.passiveAttack}</span>` : ""}
          ${skill.passiveDefense ? `<span>防御 +${skill.passiveDefense}</span>` : ""}
          ${skill.passiveAttackSpeed ? `<span>攻速 +${skill.passiveAttackSpeed}</span>` : ""}
        </div>
        <small>消耗金币：${skill.upgradeGold}${skill.materialCosts && skill.materialCosts.length ? ` / 材料 ${skill.materialCosts.map(item => `${item.name} x${item.quantity}`).join("，")}` : ""} / 上限 Lv.${skill.maxLevel}</small>
        <div class="item-actions">
          ${!skill.learned ? `<button onclick="learnSkill('${skill.id}')" ${skill.canLearn ? "" : "disabled"}>学习</button>` : ""}
          ${skill.learned ? `<button onclick="upgradeSkill('${skill.id}')" ${skill.canUpgrade ? "" : "disabled"}>升级</button>` : ""}
          ${skill.learned && skill.type === "active" ? renderSkillSlotSelect(skill) : ""}
        </div>
      </div>
    </div>
  `).join("") : `<div class="item"><small>当前职业暂无技能</small></div>`;
  if (activeSkills.length) {
    $("skillList").insertAdjacentHTML("afterbegin", `
      <div class="item skill-bar-preview">
        <div class="item-header"><strong>技能栏</strong><small>战斗中可手动释放</small></div>
        <div class="battle-skill-bar">
          ${[1, 2, 3, 4].map(slot => {
            const skill = activeSkills.find(item => item.skillSlot === slot);
            return `<span class="skill-slot-chip">${skill ? `<img src="${skillImagePath(skill.id)}" alt="" onerror="assetFallback(this)">` : ""}${slot}. ${skill ? skill.name : "空"}</span>`;
          }).join("")}
        </div>
      </div>
    `);
  }
}

function renderSkillSlotSelect(skill) {
  return `
    <label class="compact-select">
      技能栏
      <select onchange="setSkillSlot('${skill.id}', this.value)">
        <option value="0" ${skill.skillSlot === 0 ? "selected" : ""}>不上栏</option>
        ${[1, 2, 3, 4].map(slot => `<option value="${slot}" ${skill.skillSlot === slot ? "selected" : ""}>${slot}</option>`).join("")}
      </select>
    </label>
  `;
}

async function learnSkill(skillId) {
  await api("/api/skills/learn", {
    method: "POST",
    body: JSON.stringify({ skillId })
  });
  showMessage("技能学习成功");
  await loadCharacter();
  await loadSkills();
}

async function upgradeSkill(skillId) {
  await api("/api/skills/upgrade", {
    method: "POST",
    body: JSON.stringify({ skillId })
  });
  showMessage("技能升级成功");
  await loadCharacter();
  await loadSkills();
}

async function setSkillSlot(skillId, value) {
  await api("/api/skills/slot", {
    method: "POST",
    body: JSON.stringify({ skillId, skillSlot: Number(value) || 0 })
  });
  showMessage("技能栏已更新");
  await loadSkills();
}

async function loadTalents() {
  const data = await api("/api/talents");
  $("talentSummary").innerHTML = [
    ["总点数", data.totalPoints],
    ["已用", data.usedPoints],
    ["可用", data.availablePoints],
    ["重置金币", data.resetGoldCost]
  ].map(([label, value]) => `<div class="stat"><span>${label}</span><strong>${value}</strong></div>`).join("");
  $("talentList").innerHTML = data.talents.length ? renderTalentBranches(data.talents) : `<div class="item"><small>暂无天赋配置</small></div>`;
}

function talentBranchText(branch) {
  const names = {
    survival: "生存",
    offense: "输出",
    mobility: "机动"
  };
  return names[branch] || "通用";
}

function renderTalentBranches(talents) {
  const branches = [];
  (talents || []).forEach(talent => {
    const branch = talent.branch || "general";
    if (!branches.includes(branch)) branches.push(branch);
  });
  return branches.map(branch => `
    <div class="talent-branch">
      <div class="item-header">
        <strong>${talentBranchText(branch)}分支</strong>
        <small>${(talents || []).filter(talent => (talent.branch || "general") === branch).reduce((sum, talent) => sum + (talent.level || 0), 0)} 点</small>
      </div>
      ${(talents || []).filter(talent => (talent.branch || "general") === branch).map(renderTalentItem).join("")}
    </div>
  `).join("");
}

function renderTalentItem(talent) {
  return `
    <div class="item">
      <div class="item-header">
        <strong>${talent.name}</strong>
        <span class="status-badge ${talent.canUpgrade ? "ready" : ""}">Lv.${talent.level}/${talent.maxLevel}</span>
      </div>
      <small>${talent.description}</small>
      <div class="talent-meta">
        <span>需求 Lv.${talent.requiredLevel}</span>
        ${talent.preTalentId ? `<span>前置 ${talent.preTalentId} Lv.${talent.preTalentLevel}</span>` : ""}
        ${talent.hp ? `<span>生命 +${talent.hp}/级</span>` : ""}
        ${talent.attack ? `<span>攻击 +${talent.attack}/级</span>` : ""}
        ${talent.defense ? `<span>防御 +${talent.defense}/级</span>` : ""}
        ${talent.attackSpeed ? `<span>攻速 +${talent.attackSpeed}/级</span>` : ""}
        ${talent.skillTriggerBonus ? `<span>技能触发 +${percentText(talent.skillTriggerBonus)}/级</span>` : ""}
        ${talent.goldBonusPercent ? `<span>金币 +${talent.goldBonusPercent}%/级</span>` : ""}
      </div>
      <div class="item-actions">
        <button onclick="upgradeTalent('${talent.id}')" ${talent.canUpgrade ? "" : "disabled"}>加点</button>
      </div>
    </div>
  `;
}

async function upgradeTalent(talentId) {
  await api("/api/talents/upgrade", {
    method: "POST",
    body: JSON.stringify({ talentId })
  });
  showMessage("天赋加点成功");
  await loadCharacter();
  await loadTalents();
}

async function resetTalents() {
  if (!window.confirm("确认重置全部天赋？会消耗金币。")) return;
  await api("/api/talents/reset", { method: "POST" });
  showMessage("天赋已重置");
  await loadCharacter();
  await loadTalents();
}

async function unequip(slot) {
  await api("/api/equipment/unequip", {
    method: "POST",
    body: JSON.stringify({ slot })
  });
  await loadCharacter();
  await loadInventory();
  await loadEquipment();
}

function rankingSourceText(source) {
  return source === "cache" ? "缓存快照" : "实时查询";
}

async function loadRankings() {
  const snapshot = await api(`/api/rankings/${state.ranking}/snapshot?limit=20`);
  const entries = snapshot.entries || [];
  $("rankingMeta").innerHTML = `
    <span>${snapshot.title || "排行榜"}</span>
    <span>${rankingSourceText(snapshot.source)} / 生成 ${formatDateTime(snapshot.generatedAt)}</span>
    <span>刷新间隔 ${snapshot.refreshIntervalSeconds || 0}s / 下次 ${formatDateTime(snapshot.nextRefreshAt)}</span>
  `;
  $("rankingList").innerHTML = entries.length ? entries.map(row => `
    <div class="item">
      <div class="item-header"><strong>#${row.rank} ${row.nickname}</strong><small>${row.value}</small></div>
      <small>等级 ${row.level} / 战力 ${row.power} / 金币 ${row.gold}</small>
    </div>
  `).join("") : `<div class="item"><small>暂无排行数据</small></div>`;
}

function mailAttachmentText(mail) {
  const parts = [];
  if (mail.attachmentGold) parts.push(`金币 ${mail.attachmentGold}`);
  if (mail.attachmentQuantity) parts.push(`${mail.attachmentItemName || mail.attachmentItemId} x${mail.attachmentQuantity}`);
  return parts.length ? parts.join(" / ") : "无附件";
}

function mailStatusText(mail) {
  if (mail.expired) return "已过期";
  if (mail.status === 1) return "已领取";
  return mail.read ? "已读" : "未读";
}

async function loadMails() {
  const mails = await api("/api/mails?limit=30");
  $("mailList").innerHTML = mails.length ? mails.map(mail => `
    <div class="item">
      <div class="item-header">
        <strong>${mail.title}</strong>
        <span class="status-badge ${mail.claimable ? "ready" : ""}">${mailStatusText(mail)}</span>
      </div>
      <small>${mail.content || ""}</small>
      <small>附件：${mailAttachmentText(mail)}</small>
      <small>${textOf(mailSourceText, mail.sourceType, "系统邮件")} / 收到：${formatDateTime(mail.createdAt)}${mail.expiresAt ? ` / 过期：${formatDateTime(mail.expiresAt)}` : ""}</small>
      <div class="item-actions">
        ${!mail.read ? `<button class="secondary" onclick="readMail(${mail.id})">标记已读</button>` : ""}
        ${mail.claimable ? `<button onclick="claimMail(${mail.id})">领取附件</button>` : ""}
        ${!mail.claimable ? `<button class="secondary" onclick="deleteMail(${mail.id})">删除</button>` : ""}
      </div>
    </div>
  `).join("") : `<div class="item"><small>暂无邮件</small></div>`;
}

function activityRewardText(activity) {
  const parts = [];
  if (activity.rewardGold) parts.push(`金币 ${activity.rewardGold}`);
  (activity.rewardItems || []).forEach(item => parts.push(`${item.name || item.itemId} x${item.quantity}`));
  return parts.length ? parts.join(" / ") : "暂无直接奖励";
}

function activityEffectText(activity) {
  const effects = activity.effects || [];
  if (!effects.length) return "暂无玩法加成";
  return effects.map(effect => effect.description || `${effect.type} +${effect.percent}%`).join(" / ");
}

function rankingRewardText(activity) {
  const rewards = activity.rankingRewards || [];
  if (!rewards.length) return "";
  return rewards.map(reward => {
    const parts = [];
    if (reward.rewardGold) parts.push(`金币 ${reward.rewardGold}`);
    (reward.rewardItems || []).forEach(item => parts.push(`${item.name || item.itemId} x${item.quantity}`));
    const rankText = reward.currentRank ? `当前第 ${reward.currentRank} 名` : "暂无名次";
    const status = reward.eligible ? "已达成" : "未达成";
    const title = reward.description || `${textOf(rankingTypeText, reward.rankingType, reward.rankingType)}前 ${reward.maxRank}`;
    return `${title}：${rankText} / ${status} / ${parts.join(" / ") || "无奖励"}`;
  }).join("；");
}

async function loadActivities() {
  const activities = await api("/api/activities");
  $("activityList").innerHTML = activities.length ? activities.map(activity => `
    <div class="item activity-card">
      <div class="item-header">
        <strong>${activity.name}</strong>
        <span class="status-badge ${activity.status || ""}">${textOf(activityStatusText, activity.status, "活动")}</span>
      </div>
      <small>${textOf(activityTypeText, activity.type, activity.type)}${activity.tag ? ` / ${activity.tag}` : ""}</small>
      <p>${activity.summary || ""}</p>
      <small>${activity.description || ""}</small>
      <small>时间：${formatDateTime(activity.startAt)} - ${formatDateTime(activity.endAt)}</small>
      <small>奖励预览：${activityRewardText(activity)}</small>
      <small>玩法加成：${activityEffectText(activity)}</small>
      ${rankingRewardText(activity) ? `<small>榜单奖励：${rankingRewardText(activity)}</small>` : ""}
      <div class="item-actions">
        ${activity.claimable ? `<button type="button" onclick="claimActivity('${activity.id}')">领取奖励</button>` : ""}
        ${activity.claimed ? `<span class="status-badge ready">已领取</span>` : ""}
        ${activity.targetView ? `<button class="secondary" type="button" onclick="switchView('${activity.targetView}')">前往</button>` : ""}
      </div>
    </div>
  `).join("") : `<div class="item"><small>暂无活动</small></div>`;
}

async function claimActivity(activityId) {
  const result = await api(`/api/activities/${activityId}/claim`, { method: "POST" });
  const itemText = result.items && result.items.length
    ? `，${result.items.map(item => `${item.name || item.itemId} x${item.quantity}`).join(" / ")}`
    : "";
  showMessage(`活动奖励领取成功：金币 ${result.goldGained || 0}${itemText}`);
  await loadCharacter();
  await loadActivities();
}

function guildRoleText(role) {
  return role === "leader" ? "会长" : "成员";
}

function renderGuildDonations(guild) {
  const options = guild.donationOptions || [];
  return `
    <div class="panel guild-donations">
      <div class="item-header">
        <strong>每日捐献</strong>
        <small>我的贡献 ${guild.myContribution || 0}</small>
      </div>
      <div class="list compact-list">
        ${options.length ? options.map(option => `
          <div class="item">
            <div class="item-header">
              <strong>${option.name}</strong>
              <span class="status-badge ${option.remainingTimes > 0 ? "ready" : "locked"}">${option.dailyUsed}/${option.dailyLimit}</span>
            </div>
            <small>${option.description || `金币 ${option.goldCost} / 贡献 +${option.contribution}`}</small>
            <small>消耗金币 ${option.goldCost}，获得贡献 ${option.contribution}</small>
            <div class="item-actions">
              <button type="button" onclick="donateGuild('${option.id}')" ${option.remainingTimes <= 0 ? "disabled" : ""}>捐献</button>
            </div>
          </div>
        `).join("") : `<div class="item"><small>暂无捐献配置</small></div>`}
      </div>
    </div>
  `;
}

function renderGuildShop(guild) {
  const items = guild.shopItems || [];
  return `
    <div class="panel guild-shop">
      <div class="item-header">
        <strong>公会商店</strong>
        <small>贡献可兑换每日补给</small>
      </div>
      <div class="list compact-list">
        ${items.length ? items.map(item => `
          <div class="item">
            <div class="item-header">
              <strong>${item.itemName} x${item.quantity}</strong>
              <span class="status-badge ${item.canBuy ? "ready" : "locked"}">${item.tag || "商品"}</span>
            </div>
            <small>${item.description || ""}</small>
            <small>贡献 ${item.contributionCost} / 金币 ${item.goldCost} / 今日 ${item.dailyUsed}/${item.dailyLimit}</small>
            ${item.minContribution > 0 ? `<small>购买条件：累计贡献 ${item.minContribution}</small>` : ""}
            <div class="item-actions">
              <button type="button" onclick="buyGuildShopItem('${item.id}')" ${item.canBuy ? "" : "disabled"}>购买</button>
            </div>
          </div>
        `).join("") : `<div class="item"><small>暂无商店商品</small></div>`}
      </div>
    </div>
  `;
}

function renderGuildActivities(guild) {
  const activities = guild.activities || [];
  return `
    <div class="panel guild-activities">
      <div class="item-header">
        <strong>公会活动</strong>
        <small>公会总贡献 ${guild.totalContribution || 0}</small>
      </div>
      <div class="list compact-list">
        ${activities.length ? activities.map(activity => {
          const rewards = [
            activity.rewardGold > 0 ? `金币 ${activity.rewardGold}` : "",
            ...(activity.rewardItems || []).map(item => `${item.name || item.itemId} x${item.quantity}`)
          ].filter(Boolean).join(" / ");
          return `
            <div class="item">
              <div class="item-header">
                <strong>${activity.name}</strong>
                <span class="status-badge ${activity.claimable ? "ready" : activity.claimed ? "" : "locked"}">${activity.claimed ? "已领取" : activity.achieved ? "可领取" : `${activity.progressPercent || 0}%`}</span>
              </div>
              <small>${activity.description || ""}</small>
              <small>目标贡献 ${activity.targetContribution} / 当前 ${activity.currentContribution}</small>
              <small>奖励：${rewards || "无"}</small>
              <div class="item-actions">
                <button type="button" onclick="claimGuildActivity('${activity.id}')" ${activity.claimable ? "" : "disabled"}>领取奖励</button>
              </div>
            </div>
          `;
        }).join("") : `<div class="item"><small>暂无公会活动配置</small></div>`}
      </div>
    </div>
  `;
}

function renderGuildRankings(rankings) {
  return `
    <div class="panel guild-rankings">
      <div class="item-header">
        <strong>公会贡献排行</strong>
        <small>按总贡献排序</small>
      </div>
      <div class="list compact-list">
        ${rankings.length ? rankings.map(entry => `
          <div class="item ${entry.mine ? "selected" : ""}">
            <div class="item-header">
              <strong>#${entry.rank} ${entry.name}</strong>
              <span class="status-badge ${entry.mine ? "ready" : ""}">${entry.mine ? "我的公会" : `${entry.memberCount} 人`}</span>
            </div>
            <small>会长：${entry.leaderNickname || "-"}</small>
            <small>总贡献：${entry.totalContribution || 0}</small>
          </div>
        `).join("") : `<div class="item"><small>暂无排行数据</small></div>`}
      </div>
    </div>
  `;
}

function renderGuildMine(guild) {
  if (!guild || !guild.inGuild) {
    return `
      <div class="panel guild-empty">
        <div>
          <strong>尚未加入公会</strong>
          <small>创建自己的公会，或从下方列表选择一个加入。</small>
        </div>
      </div>
    `;
  }
  const isLeader = guild.myRole === "leader";
  return `
    <div class="panel guild-card">
      <div class="item-header">
        <div>
          <strong>${guild.name}</strong>
          <small>${guild.notice || "暂无公告"}</small>
        </div>
        <span class="status-badge ready">${guildRoleText(guild.myRole)}</span>
      </div>
      <div class="guild-stats">
        <span>会长：${guild.leaderNickname || "-"}</span>
        <span>成员：${guild.memberCount}</span>
        <span>总贡献：${guild.totalContribution || 0}</span>
        <span>我的贡献：${guild.myContribution || 0}</span>
        <span>创建：${formatDateTime(guild.createdAt)}</span>
      </div>
      <div class="item-actions">
        <button class="secondary" type="button" onclick="leaveGuild()">${isLeader && guild.memberCount <= 1 ? "解散公会" : "退出公会"}</button>
      </div>
    </div>
    ${renderGuildDonations(guild)}
    ${renderGuildActivities(guild)}
    ${renderGuildShop(guild)}
    <div class="panel guild-members">
      <div class="item-header"><strong>成员列表</strong><small>${guild.members.length} 人</small></div>
      <div class="list compact-list">
        ${guild.members.map(member => `
          <div class="item">
            <div class="item-header">
              <strong>${member.nickname}</strong>
              <span class="status-badge ${member.role === "leader" ? "ready" : ""}">${member.roleText || guildRoleText(member.role)}</span>
            </div>
            <small>Lv.${member.level} / 战力 ${member.power} / 贡献 ${member.contribution}</small>
            <small>加入：${formatDateTime(member.joinedAt)}</small>
            ${isLeader && member.characterId !== state.character.id ? `
              <div class="item-actions">
                <button class="secondary" type="button" onclick="transferGuildLeader(${member.characterId})">转让会长</button>
                <button class="danger" type="button" onclick="kickGuildMember(${member.characterId})">踢出</button>
              </div>
            ` : ""}
          </div>
        `).join("")}
      </div>
    </div>
  `;
}

function renderGuildList(guilds, mine) {
  return guilds.length ? guilds.map(guild => {
    const joinedThis = mine && mine.inGuild && mine.id === guild.id;
    const alreadyJoined = mine && mine.inGuild;
    return `
      <div class="item">
        <div class="item-header">
          <strong>${guild.name}</strong>
          <small>${guild.memberCount} 人</small>
        </div>
        <small>会长：${guild.leaderNickname || "-"}</small>
        <small>总贡献：${guild.totalContribution || 0}</small>
        <small>${guild.notice || "暂无公告"}</small>
        <div class="item-actions">
          ${joinedThis
            ? `<span class="status-badge ready">已加入</span>`
            : `<button type="button" onclick="joinGuild(${guild.id})" ${alreadyJoined ? "disabled" : ""}>加入</button>`}
        </div>
      </div>
    `;
  }).join("") : `<div class="item"><small>暂无公会</small></div>`;
}

async function loadGuilds() {
  const [mine, guilds, rankings] = await Promise.all([
    api("/api/guilds/me"),
    api("/api/guilds?limit=30"),
    api("/api/guilds/rankings?limit=10")
  ]);
  state.guild = mine;
  state.guilds = guilds;
  $("guildMine").innerHTML = renderGuildMine(mine);
  $("guildList").innerHTML = renderGuildList(guilds, mine);
  $("guildRankings").innerHTML = renderGuildRankings(rankings);
  const createPanel = document.querySelector(".guild-create-panel");
  if (createPanel) {
    createPanel.classList.toggle("hidden", mine && mine.inGuild);
  }
}

async function createGuild() {
  const name = $("guildName").value.trim();
  const notice = $("guildNotice").value.trim();
  const result = await api("/api/guilds", {
    method: "POST",
    body: JSON.stringify({ name, notice })
  });
  showMessage(result.message || "公会创建成功");
  $("guildName").value = "";
  $("guildNotice").value = "";
  await loadGuilds();
}

async function joinGuild(guildId) {
  const result = await api(`/api/guilds/${guildId}/join`, { method: "POST" });
  showMessage(result.message || "已加入公会");
  await loadGuilds();
}

async function leaveGuild() {
  const mine = state.guild;
  const text = mine && mine.myRole === "leader" && mine.memberCount <= 1 ? "确认解散公会？" : "确认退出公会？";
  if (!window.confirm(text)) return;
  const result = await api("/api/guilds/leave", { method: "POST" });
  showMessage(result.message || "已退出公会");
  await loadGuilds();
}

async function kickGuildMember(characterId) {
  if (!window.confirm("确认踢出该成员？")) return;
  const result = await api(`/api/guilds/members/${characterId}/kick`, { method: "POST" });
  showMessage(result.message || "成员已踢出");
  await loadGuilds();
}

async function transferGuildLeader(characterId) {
  if (!window.confirm("确认转让会长？")) return;
  const result = await api(`/api/guilds/members/${characterId}/transfer`, { method: "POST" });
  showMessage(result.message || "会长已转让");
  await loadGuilds();
}

async function donateGuild(donationId) {
  const result = await api("/api/guilds/donate", {
    method: "POST",
    body: JSON.stringify({ donationId })
  });
  showMessage(result.message || "捐献成功");
  await loadCharacter();
  await loadGuilds();
}

async function buyGuildShopItem(shopItemId) {
  const result = await api("/api/guilds/shop/buy", {
    method: "POST",
    body: JSON.stringify({ shopItemId })
  });
  showMessage(`${result.message || "购买成功"}：${result.itemName} x${result.quantity}`);
  await loadCharacter();
  await loadGuilds();
}

async function claimGuildActivity(activityId) {
  const result = await api(`/api/guilds/activities/${activityId}/claim`, { method: "POST" });
  const itemText = result.items && result.items.length
    ? `，${result.items.map(item => `${item.name || item.itemId} x${item.quantity}`).join(" / ")}`
    : "";
  showMessage(`${result.message || "公会活动奖励领取成功"}：金币 ${result.goldGained || 0}${itemText}`);
  await loadCharacter();
  await loadGuilds();
}

async function readMail(mailId) {
  await api(`/api/mails/${mailId}/read`, { method: "POST" });
  await loadMails();
}

async function claimMail(mailId) {
  const result = await api(`/api/mails/${mailId}/claim`, { method: "POST" });
  const itemText = result.quantity ? `，${result.itemName} x${result.quantity}` : "";
  showMessage(`邮件领取成功：金币 ${result.goldGained || 0}${itemText}`);
  await loadCharacter();
  await loadMails();
}

async function deleteMail(mailId) {
  await api(`/api/mails/${mailId}/delete`, { method: "POST" });
  await loadMails();
}

function formatSeconds(seconds) {
  const minutes = Math.floor((seconds || 0) / 60);
  const hours = Math.floor(minutes / 60);
  const restMinutes = minutes % 60;
  return `${hours}小时${restMinutes}分钟`;
}

async function loadIdle() {
  const status = await api("/api/idle/status");
  const idleBonus = [];
  if (status.estimatedBonusExp) idleBonus.push(`经验 +${status.estimatedBonusExp}`);
  if (status.estimatedBonusGold) idleBonus.push(`金币 +${status.estimatedBonusGold}`);
  $("idleStatus").innerHTML = status.active ? `
    <h3>${status.mapName} / ${status.monsterName}</h3>
    <small>累计 ${formatSeconds(status.cappedSeconds)}，预计经验 ${status.estimatedExp}，金币 ${status.estimatedGold}</small>
    ${idleBonus.length ? `<small>活动加成：${idleBonus.join("，")}</small>` : ""}
    <div class="item-actions"><button onclick="claimIdle()">领取收益</button></div>
  ` : `<h3>尚未挂机</h3><small>选择一个地图怪物开始挂机。</small>`;

  const maps = await api("/api/maps");
  const blocks = [];
  for (const map of maps) {
    const detail = await api(`/api/maps/${map.id}`);
    detail.monsters.forEach(monster => {
      blocks.push(`
        <div class="item">
          <div class="item-header"><strong>${detail.name} / ${monster.name}</strong><small>Lv.${detail.requiredLevel}</small></div>
          <small>挂机收益低于手动战斗，最多累计 8 小时。</small>
          <div class="item-actions"><button onclick="startIdle('${detail.id}', '${monster.id}')">开始挂机</button></div>
        </div>
      `);
    });
  }
  $("idleOptions").innerHTML = blocks.join("");
}

async function startIdle(mapId, monsterId) {
  await api("/api/idle/start", {
    method: "POST",
    body: JSON.stringify({ mapId, monsterId })
  });
  showMessage("挂机已开始");
  await loadIdle();
}

async function claimIdle() {
  const result = await api("/api/idle/claim", { method: "POST" });
  const bonus = [];
  if (result.bonusExp) bonus.push(`经验 +${result.bonusExp}`);
  if (result.bonusGold) bonus.push(`金币 +${result.bonusGold}`);
  showMessage(`领取挂机收益：经验 ${result.expGained}，金币 ${result.goldGained}${bonus.length ? `（活动加成：${bonus.join("，")}）` : ""}`);
  await loadCharacter();
  await loadIdle();
}

async function loadBosses() {
  const bosses = await api("/api/bosses");
  $("bossList").innerHTML = bosses.map(boss => `
    <div class="item">
      <div class="item-header"><strong>${boss.name}</strong><span class="status-badge ${boss.available ? "ready" : ""}">${boss.available ? "可挑战" : "冷却中"}</span></div>
      <small>${boss.mapName} / ${boss.monsterName} / 奖励 x${boss.rewardMultiplier}</small>
      <small>刷新时间：${boss.availableAt}</small>
      <div class="item-actions">${boss.available ? `<button onclick="startBossBattle('${boss.id}')">挑战</button>` : ""}</div>
    </div>
  `).join("");
}

async function startBossBattle(bossId) {
  stopBattleTimer();
  const result = await api(`/api/bosses/${bossId}/start`, { method: "POST" });
  state.currentBattle = { id: result.battleId, logId: "bossBattleLog" };
  state.selectedBattleTargetId = firstAliveEnemyId(result);
  renderBattle(result, "bossBattleLog");
  startBattleStream(result);
}

function worldBossHpPercent(boss) {
  if (!boss || !boss.maxHp) return 0;
  return Math.max(0, Math.min(100, Math.round(boss.currentHp * 100 / boss.maxHp)));
}

function renderWorldBossRanks(ranks) {
  if (!ranks || !ranks.length) {
    return `<small>暂无伤害排行</small>`;
  }
  return `
    <div class="rank-list">
      ${ranks.map(row => `
        <small>#${row.rank} ${row.nickname} / 伤害 ${row.damage}${row.rewarded ? " / 已发奖" : ""}</small>
      `).join("")}
    </div>
  `;
}

async function loadWorldBosses() {
  const bosses = await api("/api/world-bosses");
  $("worldBossList").innerHTML = bosses.length ? bosses.map(boss => {
    const hp = worldBossHpPercent(boss);
    return `
      <div class="item">
        <div class="item-header">
          <strong>${boss.name}</strong>
          <span class="status-badge ${boss.available ? "ready" : ""}">${boss.available ? "可挑战" : "冷却中"}</span>
        </div>
        <small>${boss.mapName} / ${boss.monsterName} / 奖励 x${boss.rewardMultiplier}${boss.activityRewardBonusPercent ? ` / 活动金币 +${boss.activityRewardBonusPercent}%` : ""}</small>
        <small>门槛：Lv.${boss.requiredLevel} / 战力 ${boss.requiredPower} / 刷新：${boss.availableAt}</small>
        <div class="hp-track"><div class="hp-fill" style="--hp:${hp}%"></div></div>
        <small>生命 ${boss.currentHp}/${boss.maxHp}</small>
        ${renderWorldBossRanks(boss.ranks)}
        <div class="item-actions">
          ${boss.available ? `<button onclick="startWorldBossBattle('${boss.id}')">挑战</button>` : ""}
          <button class="secondary" onclick="loadWorldBosses()">刷新</button>
        </div>
      </div>
    `;
  }).join("") : `<div class="item"><small>暂无世界 BOSS</small></div>`;
}

async function startWorldBossBattle(bossId) {
  stopBattleTimer();
  const result = await api(`/api/world-bosses/${bossId}/start`, { method: "POST" });
  state.currentBattle = { id: result.battleId, logId: "worldBossBattleLog" };
  state.selectedBattleTargetId = firstAliveEnemyId(result);
  renderBattle(result, "worldBossBattleLog");
  startBattleStream(result);
}

async function loadAdmin() {
  const data = await api("/api/admin/characters?limit=20");
  $("adminCharacterList").innerHTML = data.map(row => `
    <div class="item">
      <div class="item-header">
        <strong>${row.id} / ${row.nickname}</strong>
        <span class="status-badge ${row.online ? "ready" : ""}">${row.online ? "在线" : "离线"}</span>
      </div>
      <small>账号 ${row.accountId} / ${row.accountStatusText || "正常"} / 等级 ${row.level} / 经验 ${row.exp} / 金币 ${row.gold} / 战力 ${row.power}</small>
      <small>最后活跃：${row.lastActiveAt || "-"}</small>
      <div class="item-actions">
        ${row.accountStatus === 0
          ? `<button class="danger" onclick="gmSetAccountStatus(${row.accountId}, true)">封禁账号</button>`
          : `<button class="secondary" onclick="gmSetAccountStatus(${row.accountId}, false)">解除封禁</button>`}
      </div>
    </div>
  `).join("");
  await loadAdminActivities();
}

function formatActivityDateInput(value) {
  if (!value) return "";
  return String(value).replace(" ", "T").slice(0, 16);
}

function activityDateFromInput(value) {
  return value ? String(value).replace("T", " ") : "";
}

function prettyJson(value) {
  return JSON.stringify(value || [], null, 2);
}

function parseJsonArrayField(id) {
  const raw = $(id).value.trim();
  if (!raw) return [];
  const parsed = JSON.parse(raw);
  if (!Array.isArray(parsed)) {
    throw new Error(`${$(id).placeholder} 必须是 JSON 数组`);
  }
  return parsed;
}

function selectedAdminActivity() {
  const selectedId = $("gmActivitySelect").value || state.selectedAdminActivityId;
  return state.adminActivities.find(activity => activity.id === selectedId) || null;
}

function renderAdminActivityList() {
  $("adminActivityList").innerHTML = state.adminActivities.length ? state.adminActivities.map(activity => `
    <div class="item">
      <div class="item-header">
        <strong>${activity.id} / ${activity.name}</strong>
        <span class="status-badge ${activity.status || ""}">${textOf(activityStatusText, activity.status, "活动")}</span>
      </div>
      <small>${textOf(activityTypeText, activity.type, activity.type)}${activity.tag ? ` / ${activity.tag}` : ""} / 优先级 ${activity.priority || 0}</small>
      <small>${formatDateTime(activity.startAt)} - ${formatDateTime(activity.endAt)} / 目标 ${activity.targetView || "-"}</small>
      <div class="item-actions">
        <button class="secondary" type="button" onclick="selectAdminActivity('${activity.id}')">编辑</button>
      </div>
    </div>
  `).join("") : `<div class="item"><small>暂无活动配置</small></div>`;
}

function fillAdminActivityForm(activity) {
  if (!activity) {
    ["gmActivityName", "gmActivityTag", "gmActivityPriority", "gmActivityRewardGold",
      "gmActivityStartAt", "gmActivityEndAt", "gmActivitySummary", "gmActivityDescription",
      "gmActivityRewardItems", "gmActivityEffects", "gmActivityRankingRewards"].forEach(id => $(id).value = "");
    return;
  }
  $("gmActivitySelect").value = activity.id;
  $("gmActivityStatus").value = activity.status || "upcoming";
  $("gmActivityType").value = activity.type || "growth";
  $("gmActivityTargetView").value = activity.targetView || "activities";
  $("gmActivityName").value = activity.name || "";
  $("gmActivityTag").value = activity.tag || "";
  $("gmActivityPriority").value = activity.priority || 0;
  $("gmActivityRewardGold").value = activity.rewardGold || 0;
  $("gmActivityStartAt").value = formatActivityDateInput(activity.startAt);
  $("gmActivityEndAt").value = formatActivityDateInput(activity.endAt);
  $("gmActivitySummary").value = activity.summary || "";
  $("gmActivityDescription").value = activity.description || "";
  $("gmActivityRewardItems").value = prettyJson(activity.rewardItems);
  $("gmActivityEffects").value = prettyJson(activity.effects);
  $("gmActivityRankingRewards").value = prettyJson(activity.rankingRewards);
}

function selectAdminActivity(activityId) {
  state.selectedAdminActivityId = activityId;
  fillAdminActivityForm(selectedAdminActivity());
}

async function loadAdminActivities() {
  state.adminActivities = await api("/api/admin/activities");
  $("gmActivitySelect").innerHTML = state.adminActivities.map(activity =>
    `<option value="${activity.id}">${activity.name || activity.id}</option>`
  ).join("");
  if (!state.selectedAdminActivityId || !state.adminActivities.some(activity => activity.id === state.selectedAdminActivityId)) {
    state.selectedAdminActivityId = state.adminActivities.length ? state.adminActivities[0].id : null;
  }
  renderAdminActivityList();
  fillAdminActivityForm(selectedAdminActivity());
}

async function gmSaveActivity() {
  const current = selectedAdminActivity();
  if (!current) {
    throw new Error("请选择活动");
  }
  const body = {
    id: current.id,
    name: $("gmActivityName").value.trim(),
    type: $("gmActivityType").value,
    status: $("gmActivityStatus").value,
    tag: $("gmActivityTag").value.trim(),
    summary: $("gmActivitySummary").value.trim(),
    description: $("gmActivityDescription").value.trim(),
    startAt: activityDateFromInput($("gmActivityStartAt").value),
    endAt: activityDateFromInput($("gmActivityEndAt").value),
    priority: Number($("gmActivityPriority").value || 0),
    targetView: $("gmActivityTargetView").value,
    rewardGold: Number($("gmActivityRewardGold").value || 0),
    rewardItems: parseJsonArrayField("gmActivityRewardItems"),
    effects: parseJsonArrayField("gmActivityEffects"),
    rankingRewards: parseJsonArrayField("gmActivityRankingRewards")
  };
  const result = await api(`/api/admin/activities/${current.id}`, {
    method: "POST",
    body: JSON.stringify(body)
  });
  $("gmConfigSummary").textContent = `${result.message}：${result.summary || ""}`;
  if (!result.success) {
    throw new Error(result.summary || result.message);
  }
  showMessage("活动配置已保存");
  await loadAdminActivities();
  if (state.view === "activities") {
    await loadActivities();
  }
}

function mapEventStateText(row) {
  const parts = [];
  if (row.completed) parts.push("已完成");
  if (row.nextAvailableAt) parts.push(`下次 ${row.nextAvailableAt}`);
  if (row.resetType) parts.push(row.resetType);
  if (row.cooldownSeconds) parts.push(`${row.cooldownSeconds}s`);
  return parts.length ? parts.join(" / ") : "可重置";
}

async function gmLoadEventStates() {
  const characterId = Number($("gmEventCharacterId").value);
  const rows = await api(`/api/admin/map-event-states?characterId=${characterId}`);
  $("adminEventStateList").innerHTML = rows.length ? rows.map(row => `
    <div class="item">
      <div class="item-header">
        <strong>${row.mapName} / ${row.eventName}</strong>
        <span class="status-badge">${row.triggerCount} 次</span>
      </div>
      <small>${row.type} / ${mapEventStateText(row)}</small>
      <small>最后触发：${row.lastTriggeredAt || "-"}</small>
      <div class="item-actions">
        <button class="secondary" onclick="gmResetEventState('${row.eventId}')">重置</button>
      </div>
    </div>
  `).join("") : `<div class="item"><small>该角色暂无地图事件状态</small></div>`;
}

async function gmResetEventState(eventId) {
  await api("/api/admin/map-event-states/reset", {
    method: "POST",
    body: JSON.stringify({ characterId: Number($("gmEventCharacterId").value), eventId })
  });
  showMessage("地图事件状态已重置");
  await gmLoadEventStates();
}

async function gmResetAllEventStates() {
  await api("/api/admin/map-event-states/reset-all", {
    method: "POST",
    body: JSON.stringify({ characterId: Number($("gmEventCharacterId").value) })
  });
  showMessage("地图事件状态已全部重置");
  await gmLoadEventStates();
}

async function gmCleanupEventStates() {
  await api("/api/admin/map-event-states/cleanup", {
    method: "POST",
    body: JSON.stringify({
      characterId: Number($("gmEventCharacterId").value),
      keepDays: Number($("gmEventKeepDays").value || 7)
    })
  });
  showMessage("地图事件过期状态已清理");
  await gmLoadEventStates();
}

async function gmGrantGold() {
  await api("/api/admin/grant-gold", {
    method: "POST",
    body: JSON.stringify({ characterId: Number($("gmCharacterId").value), gold: Number($("gmGold").value) })
  });
  showMessage("GM 金币发放成功");
  await loadAdmin();
}

async function gmGrantItem() {
  await api("/api/admin/grant-item", {
    method: "POST",
    body: JSON.stringify({
      characterId: Number($("gmItemCharacterId").value),
      itemId: $("gmItemId").value.trim(),
      quantity: Number($("gmItemQuantity").value)
    })
  });
  showMessage("GM 物品发放成功");
  await loadAdmin();
}

async function gmSendMail() {
  const characterIds = $("gmMailCharacterIds").value
    .split(",")
    .map(value => Number(value.trim()))
    .filter(value => value > 0);
  await api("/api/admin/send-mail", {
    method: "POST",
    body: JSON.stringify({
      characterId: Number($("gmMailCharacterId").value),
      characterIds,
      all: $("gmMailAll").checked,
      title: $("gmMailTitle").value.trim(),
      content: $("gmMailContent").value.trim(),
      gold: Number($("gmMailGold").value || 0),
      itemId: $("gmMailItemId").value.trim(),
      quantity: Number($("gmMailQuantity").value || 0),
      expiresAt: $("gmMailExpiresAt").value
    })
  });
  showMessage("GM 邮件发送成功");
  await loadAdmin();
}

async function gmSetAccountStatus(accountId, disabled) {
  await api("/api/admin/account-status", {
    method: "POST",
    body: JSON.stringify({ accountId, disabled })
  });
  showMessage(disabled ? "账号已封禁" : "账号已解除封禁");
  await loadAdmin();
}

async function gmSetAccountStatusFromInput(disabled) {
  const accountId = Number($("gmAccountId").value);
  if (!accountId) {
    throw new Error("请填写账号 ID");
  }
  await gmSetAccountStatus(accountId, disabled);
}

async function gmReloadConfig() {
  const result = await api("/api/admin/config/reload", { method: "POST" });
  $("gmConfigSummary").textContent = `${result.message}：${result.summary || ""}`;
  if (!result.success) {
    throw new Error(result.message);
  }
  showMessage("配置重载成功");
}

async function refreshView() {
  await loadCharacter();
  if (state.view === "maps") await loadMaps();
  if (state.view === "tasks") await loadTasks();
  if (state.view === "idle") await loadIdle();
  if (state.view === "bosses") await loadBosses();
  if (state.view === "worldBosses") await loadWorldBosses();
  if (state.view === "inventory") await loadInventory();
  if (state.view === "equipment") await loadEquipment();
  if (state.view === "skills") await loadSkills();
  if (state.view === "talents") await loadTalents();
  if (state.view === "mails") await loadMails();
  if (state.view === "activities") await loadActivities();
  if (state.view === "guilds") await loadGuilds();
  if (state.view === "rankings") await loadRankings();
  if (state.view === "admin") await loadAdmin();
}

function switchView(view) {
  if (state.view !== view) {
    stopBattleTimer();
  }
  state.view = view;
  document.querySelectorAll(".tab").forEach(btn => btn.classList.toggle("active", btn.dataset.view === view));
  document.querySelectorAll(".view").forEach(viewNode => viewNode.classList.add("hidden"));
  $(`${view}View`).classList.remove("hidden");
  $("viewTitle").textContent = document.querySelector(`.tab[data-view="${view}"]`).textContent;
  if (view !== "maps") {
    state.mapLogDrawerOpen = false;
    stopAutoEncounter();
  }
  renderMapLogDrawer();
  refreshView().catch(error => showMessage(error.message));
}

document.querySelectorAll(".tab").forEach(btn => btn.addEventListener("click", () => switchView(btn.dataset.view)));
document.querySelectorAll("[data-ranking]").forEach(btn => btn.addEventListener("click", () => {
  state.ranking = btn.dataset.ranking;
  document.querySelectorAll("[data-ranking]").forEach(node => node.classList.toggle("active", node === btn));
  loadRankings().catch(error => showMessage(error.message));
}));

const authThemeToggle = $("authThemeToggle");
if (authThemeToggle) authThemeToggle.addEventListener("click", toggleTheme);
$("themeToggle").addEventListener("click", toggleTheme);
$("changePasswordBtn").addEventListener("click", openPasswordModal);
$("passwordCloseBtn").addEventListener("click", closePasswordModal);
$("passwordCancelBtn").addEventListener("click", closePasswordModal);
$("passwordSubmitBtn").addEventListener("click", () => changePassword().catch(error => {
  $("passwordMessage").textContent = error.message;
}));
$("passwordModal").addEventListener("click", event => {
  if (event.target === $("passwordModal")) {
    closePasswordModal();
  }
});
$("loginBtn").addEventListener("click", () => login().catch(error => showMessage(error.message)));
$("registerBtn").addEventListener("click", () => register().catch(error => showMessage(error.message)));
$("createCharacterBtn").addEventListener("click", () => createCharacter().catch(error => showMessage(error.message)));
$("classSelect").addEventListener("change", renderClassPreview);
$("refreshBtn").addEventListener("click", () => refreshView().catch(error => showMessage(error.message)));
$("talentResetBtn").addEventListener("click", () => resetTalents().catch(error => showMessage(error.message)));
$("gmGrantGoldBtn").addEventListener("click", () => gmGrantGold().catch(error => showMessage(error.message)));
$("gmGrantItemBtn").addEventListener("click", () => gmGrantItem().catch(error => showMessage(error.message)));
$("gmSendMailBtn").addEventListener("click", () => gmSendMail().catch(error => showMessage(error.message)));
$("gmDisableAccountBtn").addEventListener("click", () => gmSetAccountStatusFromInput(true).catch(error => showMessage(error.message)));
$("gmEnableAccountBtn").addEventListener("click", () => gmSetAccountStatusFromInput(false).catch(error => showMessage(error.message)));
$("gmReloadConfigBtn").addEventListener("click", () => gmReloadConfig().catch(error => showMessage(error.message)));
$("gmLoadActivitiesBtn").addEventListener("click", () => loadAdminActivities().catch(error => showMessage(error.message)));
$("gmActivitySelect").addEventListener("change", event => selectAdminActivity(event.target.value));
$("gmSaveActivityBtn").addEventListener("click", () => gmSaveActivity().catch(error => showMessage(error.message)));
$("guildCreateBtn").addEventListener("click", () => createGuild().catch(error => showMessage(error.message)));
$("gmLoadEventStatesBtn").addEventListener("click", () => gmLoadEventStates().catch(error => showMessage(error.message)));
$("gmCleanupEventStatesBtn").addEventListener("click", () => gmCleanupEventStates().catch(error => showMessage(error.message)));
$("gmResetAllEventStatesBtn").addEventListener("click", () => gmResetAllEventStates().catch(error => showMessage(error.message)));
$("logoutBtn").addEventListener("click", () => {
  stopBattleTimer();
  localStorage.removeItem("legend_token");
  state.token = "";
  setAuthed(false);
});

applyTheme(state.theme);
boot();
