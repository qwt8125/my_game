const { spawnSync } = require("child_process");

const args = new Set(process.argv.slice(2));
const quick = args.has("--quick");
const skipSmoke = args.has("--skip-smoke") || quick;
const skipPackage = args.has("--skip-package") || quick;
const smokePort = process.env.REGRESSION_SMOKE_PORT || process.env.SMOKE_PORT || "18081";

function commandName(base) {
  return process.platform === "win32" ? `${base}.cmd` : base;
}

function run(label, command, commandArgs, options = {}) {
  console.log(`\n[regression] ${label}`);
  console.log(`[regression] > ${command} ${commandArgs.join(" ")}`);
  const result = spawnSync(command, commandArgs, {
    cwd: process.cwd(),
    stdio: "inherit",
    shell: process.platform === "win32",
    env: {
      ...process.env,
      ...(options.env || {})
    }
  });
  if (result.error) {
    console.error(`[regression] ${label} failed to start: ${result.error.message}`);
    process.exit(result.status || 1);
  }
  if (result.status !== 0) {
    console.error(`[regression] ${label} failed with exit code ${result.status}`);
    process.exit(result.status || 1);
  }
}

function packageDiagnostics() {
  if (process.platform === "win32") {
    run("jar lock diagnostics", "powershell", [
      "-NoProfile",
      "-ExecutionPolicy",
      "Bypass",
      "-File",
      "scripts\\package-diagnostics.ps1"
    ]);
    return;
  }
  console.log("\n[regression] jar lock diagnostics skipped outside Windows");
}

run("static check: app.js", "node", ["--check", "src/main/resources/static/app.js"]);
run("static check: smoke-test.js", "node", ["--check", "scripts/smoke-test.js"]);
run("static check: combat-balance-report.js", "node", ["--check", "scripts/combat-balance-report.js"]);
run("config and balance health", "node", ["scripts/combat-balance-report.js"]);
run("maven tests", commandName("mvn"), ["test"]);

if (!skipSmoke) {
  run("self-start smoke test", "node", ["scripts/smoke-test.js"], {
    env: {
      SMOKE_START_SERVER: "1",
      SMOKE_PORT: smokePort
    }
  });
} else {
  console.log("\n[regression] self-start smoke test skipped");
}

if (!skipPackage) {
  packageDiagnostics();
  run("maven package", commandName("mvn"), ["package", "-DskipTests"]);
  packageDiagnostics();
} else {
  console.log("\n[regression] maven package skipped");
}

console.log("\n[regression] PASS");
