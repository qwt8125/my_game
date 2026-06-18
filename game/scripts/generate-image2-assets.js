const fs = require("fs");
const path = require("path");

const MODEL = "gpt-image-2";
const ASSET_VERSION = "20260522-image2-pixel";
const ROOT = path.resolve(__dirname, "..");
const CONFIG_DIR = path.join(ROOT, "src", "main", "resources", "config");
const OUT_ROOT = path.join(ROOT, "src", "main", "resources", "static", "assets", "generated");

function readJson(file) {
  return JSON.parse(fs.readFileSync(path.join(CONFIG_DIR, file), "utf8"));
}

function parseArgs() {
  const args = process.argv.slice(2);
  const options = { force: false, limit: 0, only: "", skipErrors: false };
  args.forEach(arg => {
    if (arg === "--force") options.force = true;
    if (arg.startsWith("--limit=")) options.limit = Number(arg.slice("--limit=".length)) || 0;
    if (arg.startsWith("--only=")) options.only = arg.slice("--only=".length);
    if (arg === "--skip-errors") options.skipErrors = true;
  });
  return options;
}

function readAuthKey() {
  const envKey = normalizeKey(process.env.OPENAI_API_KEY);
  if (isLikelyApiKey(envKey)) return envKey;
  let fallback = envKey;
  const candidates = [
    path.join(process.env.USERPROFILE || "", ".codex", "auth.json"),
    path.join(process.env.HOME || "", ".codex", "auth.json")
  ].filter(Boolean);
  for (const file of candidates) {
    if (!fs.existsSync(file)) continue;
    const raw = fs.readFileSync(file, "utf8");
    const data = JSON.parse(raw);
    const found = normalizeKey(findKey(data));
    if (isLikelyApiKey(found)) return found;
    if (!fallback && found) fallback = found;
  }
  const instructionFiles = instructionFileCandidates();
  for (const file of instructionFiles) {
    if (!fs.existsSync(file)) continue;
    const raw = fs.readFileSync(file, "utf8");
    const match = raw.match(/sk-[A-Za-z0-9_\-]{20,}/);
    if (match) return match[0];
  }
  return fallback || "";
}

function instructionFileCandidates() {
  return [
    path.join(process.env.USERPROFILE || "", ".codex", "AGENTS.md"),
    path.join(process.env.HOME || "", ".codex", "AGENTS.md"),
    path.join(ROOT, "..", "AGENTS.md")
  ].filter(Boolean);
}

function normalizeKey(value) {
  return typeof value === "string" ? value.trim() : "";
}

function isLikelyApiKey(value) {
  return /^sk-[A-Za-z0-9_\-]{20,}$/.test(normalizeKey(value));
}

function readEndpointCandidates(defaultKey) {
  const defaults = [
    { endpoint: "https://openkun.xyz/v1/images/generations", apiKey: defaultKey },
    { endpoint: "http://ai98pro.xyz/v1/images/generations", apiKey: defaultKey }
  ];
  const candidates = [];
  for (const file of instructionFileCandidates()) {
    if (!fs.existsSync(file)) continue;
    const raw = fs.readFileSync(file, "utf8");
    const blocks = raw.split(/如果上面的不行\s*换成/i);
    for (const block of blocks) {
      const endpointMatch = block.match(/https?:\/\/[^\s]+\/v1\/images\/generations/);
      const keyMatch = block.match(/sk-[A-Za-z0-9_\-]{20,}/);
      if (endpointMatch) {
        candidates.push({
          endpoint: endpointMatch[0],
          apiKey: keyMatch ? keyMatch[0] : defaultKey
        });
      }
    }
  }
  const merged = [...candidates, ...defaults].filter(item => item.endpoint && item.apiKey);
  const seen = new Set();
  return merged.filter(item => {
    const key = `${item.endpoint}|${item.apiKey.slice(0, 8)}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function findKey(value) {
  if (!value) return "";
  if (typeof value === "string") {
    return /^sk-[A-Za-z0-9_\-]{20,}$/.test(value.trim()) ? value.trim() : "";
  }
  if (Array.isArray(value)) {
    for (const item of value) {
      const found = findKey(item);
      if (found) return found;
    }
    return "";
  }
  if (typeof value === "object") {
    const preferred = [
      "OPENAI_API_KEY",
      "openai_api_key",
      "openaiApiKey",
      "apiKey",
      "api_key",
      "key"
    ];
    for (const key of preferred) {
      if (typeof value[key] === "string" && value[key].trim()) return value[key].trim();
      const found = findKey(value[key]);
      if (found) return found;
    }
    for (const item of Object.values(value)) {
      const found = findKey(item);
      if (found) return found;
    }
  }
  return "";
}

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function qualityMood(quality) {
  const map = {
    common: "simple humble colors",
    fine: "clean blue-accented fantasy colors",
    rare: "purple magical accent colors",
    epic: "warm orange and crimson heroic glow",
    legendary: "golden legendary shine"
  };
  return map[quality] || "clear readable fantasy colors";
}

function slotDescription(item) {
  const slots = {
    weapon: "weapon icon",
    armor: "armor chestpiece icon",
    helmet: "helmet icon",
    necklace: "necklace icon",
    bracelet: "bracelet icon",
    ring: "ring icon",
    boots: "boots icon",
    shoes: "boots icon"
  };
  if (item.type === "material") return "fantasy crafting material icon";
  if (item.type === "consumable") return "fantasy potion or medicine icon";
  return slots[item.slot] || "fantasy equipment icon";
}

function sharedPrompt(assetType, subject, details) {
  return [
    "Use case: stylized-concept",
    `Asset type: ${assetType} for a browser text RPG`,
    `Primary request: ${subject}`,
    "Style/medium: crisp 32-bit pixel art, cute cartoon fantasy game art, clean blocky silhouettes, saturated but balanced colors",
    "Composition/framing: centered square asset, generous padding, readable at 48px, slight three-quarter view where appropriate",
    "Scene/backdrop: plain warm parchment background, no scenery unless needed for the subject",
    details,
    "Constraints: no text, no letters, no numbers, no watermark, no logo, no photorealism, no blurry painterly edges, no UI frame"
  ].filter(Boolean).join("\n");
}

function classPrompt(item) {
  const details = {
    warrior: "Subject details: a sturdy young fantasy warrior with simple armor and a short sword, brave approachable expression",
    mage: "Subject details: a bright-robed mage holding a glowing charm, clever expression, small magical sparks",
    taoist: "Subject details: a calm taoist adventurer with paper talisman and small charm pouch, balanced healer-warrior feel"
  };
  return sharedPrompt("playable class portrait", `${item.name} playable character portrait`, details[item.id] || item.description);
}

function monsterPrompt(item) {
  return sharedPrompt(
    "monster combat sprite",
    `${item.name} monster sprite`,
    `Subject details: level ${item.level} fantasy RPG enemy, cute but hostile cartoon tone, full body, distinctive silhouette, no gore`
  );
}

function itemPrompt(item) {
  if (item.id === "consumable_hero_elixir") {
    return sharedPrompt(
      "inventory item icon",
      "hero elixir potion bottle icon",
      "Subject details: a single small red and gold fantasy potion bottle with a cork stopper and soft heroic glow, readable as a rare consumable item, no text"
    );
  }
  return sharedPrompt(
    "inventory item icon",
    `${item.name} ${slotDescription(item)}`,
    `Subject details: ${qualityMood(item.quality)}, fantasy item icon matching ${item.type || "item"} category, single object only`
  );
}

function skillPrompt(item) {
  return sharedPrompt(
    "skill icon",
    `${item.name} skill icon`,
    `Subject details: ${item.description || item.name}; abstract magical effect icon for ${item.className || "hero"} ${item.type || "skill"}, no readable symbols`
  );
}

function npcPrompt(item) {
  return sharedPrompt(
    "NPC portrait",
    `${item.name} NPC portrait`,
    `Subject details: ${item.description || item.name}; friendly fantasy villager portrait, waist-up, expressive cartoon face`
  );
}

function brandPrompt() {
  return sharedPrompt(
    "game crest icon",
    "OpenClaw text legend game crest",
    "Subject details: compact fantasy shield crest with crossed sword and small moon, pixel art emblem, no words or letters"
  );
}

function buildAssets() {
  const classes = readJson("classes.json").map(item => ({
    group: "classes",
    id: item.id,
    name: item.name,
    prompt: classPrompt(item)
  }));
  const monsters = readJson("monsters.json").map(item => ({
    group: "monsters",
    id: item.id,
    name: item.name,
    prompt: monsterPrompt(item)
  }));
  const items = readJson("items.json").map(item => ({
    group: "items",
    id: item.id,
    name: item.name,
    prompt: itemPrompt(item)
  }));
  const skills = readJson("skills.json").map(item => ({
    group: "skills",
    id: item.id,
    name: item.name,
    prompt: skillPrompt(item)
  }));
  const npcs = readJson("npcs.json").map(item => ({
    group: "npcs",
    id: item.id,
    name: item.name,
    prompt: npcPrompt(item)
  }));
  const brand = [{
    group: "brand",
    id: "crest",
    name: "游戏徽记",
    prompt: brandPrompt()
  }];
  return [...brand, ...classes, ...monsters, ...items, ...skills, ...npcs];
}

async function fetchImage(asset, candidate) {
  const payload = {
    model: MODEL,
    prompt: asset.prompt,
    size: "1024x1024",
    quality: "low",
    n: 1
  };
  const response = await fetch(candidate.endpoint, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${candidate.apiKey}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  const text = await response.text();
  let body = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch (error) {
    throw new Error(`API returned non-JSON response: HTTP ${response.status}`);
  }
  if (!response.ok) {
    const message = body && body.error && body.error.message ? body.error.message : `HTTP ${response.status}`;
    throw new Error(`API error at ${candidate.endpointHost || new URL(candidate.endpoint).host} for ${asset.group}/${asset.id}: ${message}`);
  }
  const first = body && body.data && body.data[0] ? body.data[0] : null;
  if (!first) throw new Error(`API returned no image data for ${asset.group}/${asset.id}`);
  if (first.b64_json) return Buffer.from(first.b64_json, "base64");
  if (first.url) {
    const imageResponse = await fetch(first.url);
    if (!imageResponse.ok) throw new Error(`image URL download failed for ${asset.group}/${asset.id}: HTTP ${imageResponse.status}`);
    return Buffer.from(await imageResponse.arrayBuffer());
  }
  if (first.image_base64) return Buffer.from(first.image_base64, "base64");
  throw new Error(`API response has unsupported image format for ${asset.group}/${asset.id}`);
}

async function fetchImageWithRetry(asset, candidates) {
  let lastError = null;
  for (const rawCandidate of candidates) {
    const candidate = Object.assign({}, rawCandidate, { endpointHost: new URL(rawCandidate.endpoint).host });
    for (let attempt = 1; attempt <= 4; attempt++) {
      try {
        return await fetchImage(asset, candidate);
      } catch (error) {
        lastError = error;
        if (attempt < 4) {
          const waitMs = 3500 * attempt;
          console.log(`[retry] ${asset.group}/${asset.id} via ${candidate.endpointHost} attempt ${attempt + 1} after ${waitMs}ms`);
          await sleep(waitMs);
        }
      }
    }
    console.log(`[fallback] ${asset.group}/${asset.id} switching endpoint after ${candidate.endpointHost}`);
  }
  throw lastError;
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function main() {
  const options = parseArgs();
  const apiKey = readAuthKey();
  if (!apiKey) {
    throw new Error("key read failed: OPENAI_API_KEY was not found in environment or ~/.codex/auth.json");
  }
  const endpointCandidates = readEndpointCandidates(apiKey);
  if (!endpointCandidates.length) {
    throw new Error("interface unavailable: no image generation endpoint configured");
  }
  ensureDir(OUT_ROOT);
  const allAssets = buildAssets()
    .filter(asset => !options.only || asset.group === options.only);
  const assets = options.limit > 0 ? allAssets.slice(0, options.limit) : allAssets;
  const manifest = {
    version: ASSET_VERSION,
    model: MODEL,
    endpoints: endpointCandidates.map(item => item.endpoint),
    generatedAt: new Date().toISOString(),
    assets: [],
    failures: []
  };
  const existingManifestPath = path.join(OUT_ROOT, "manifest.json");
  if (fs.existsSync(existingManifestPath) && !options.force) {
    try {
      const previous = JSON.parse(fs.readFileSync(existingManifestPath, "utf8"));
      if (previous && Array.isArray(previous.assets)) {
        manifest.assets.push(...previous.assets);
      }
    } catch (error) {
      // Ignore stale manifest parse errors; regeneration can replace it.
    }
  }
  for (const asset of assets) {
    const dir = path.join(OUT_ROOT, asset.group);
    ensureDir(dir);
    const relativePath = `/assets/generated/${asset.group}/${asset.id}.png`;
    const outPath = path.join(dir, `${asset.id}.png`);
    if (!options.force && fs.existsSync(outPath)) {
      console.log(`[skip] ${asset.group}/${asset.id}`);
    } else {
      console.log(`[generate] ${asset.group}/${asset.id} ${asset.name}`);
      try {
        const buffer = await fetchImageWithRetry(asset, endpointCandidates);
        fs.writeFileSync(outPath, buffer);
      } catch (error) {
        if (!options.skipErrors) throw error;
        console.error(`[failed] ${asset.group}/${asset.id}: ${error.message}`);
        manifest.failures.push({
          group: asset.group,
          id: asset.id,
          name: asset.name,
          message: error.message
        });
        fs.writeFileSync(existingManifestPath, JSON.stringify(manifest, null, 2), "utf8");
        continue;
      }
    }
    const index = manifest.assets.findIndex(item => item.group === asset.group && item.id === asset.id);
    const row = {
      group: asset.group,
      id: asset.id,
      name: asset.name,
      path: relativePath,
      prompt: asset.prompt
    };
    if (index >= 0) manifest.assets[index] = row;
    else manifest.assets.push(row);
    fs.writeFileSync(existingManifestPath, JSON.stringify(manifest, null, 2), "utf8");
    await sleep(1200);
  }
  fs.writeFileSync(existingManifestPath, JSON.stringify(manifest, null, 2), "utf8");
  console.log(`[done] ${assets.length}/${allAssets.length} assets processed`);
}

main().catch(error => {
  console.error(error.message);
  process.exit(1);
});
