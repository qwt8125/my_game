#!/usr/bin/env bash
set -euo pipefail

DB_PATH="${1:-/opt/text-legend/data/game.db}"
BACKUP_DIR="${2:-/opt/text-legend/backup}"

if [ ! -f "$DB_PATH" ]; then
  echo "Database file not found: $DB_PATH" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_FILE="$BACKUP_DIR/game-$STAMP.db"

if command -v sqlite3 >/dev/null 2>&1; then
  sqlite3 "$DB_PATH" ".backup '$BACKUP_FILE'"
else
  cp "$DB_PATH" "$BACKUP_FILE"
fi

gzip -f "$BACKUP_FILE"
echo "Backup created: $BACKUP_FILE.gz"

