#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOST="${DEPLOY_HOST:-pi-server}"
SERVICE_NAME="${DEPLOY_SERVICE_NAME:-foodhelper}"
JAR_PATH="${DEPLOY_JAR_PATH:-$ROOT_DIR/target/foodhelper-api-0.0.1-SNAPSHOT.jar}"
REMOTE_TMP_PATH="${DEPLOY_REMOTE_TMP_PATH:-/tmp/foodhelper-api.jar}"
REMOTE_APP_PATH="${DEPLOY_REMOTE_APP_PATH:-/opt/foodhelper/foodhelper-api.jar}"
AUTH_REGISTRATION_CODE="${DEPLOY_AUTH_REGISTRATION_CODE:-}"

if [[ ! -f "$JAR_PATH" ]]; then
  echo "Build artifact not found: $JAR_PATH" >&2
  echo "Run: mvn -q -DskipTests package" >&2
  exit 1
fi

echo "Building local artifact..."
mvn -q -DskipTests package

echo "Copying jar to $HOST..."
scp "$JAR_PATH" "$HOST:$REMOTE_TMP_PATH"

echo "Installing jar and restarting $SERVICE_NAME..."
ssh "$HOST" "REMOTE_TMP_PATH='$REMOTE_TMP_PATH' REMOTE_APP_PATH='$REMOTE_APP_PATH' AUTH_REGISTRATION_CODE='$AUTH_REGISTRATION_CODE' SERVICE_NAME='$SERVICE_NAME' bash -s" <<'EOF'
set -euo pipefail
sudo install -d -o elias -g elias -m 0755 /opt/foodhelper
sudo install -o elias -g elias -m 0644 "$REMOTE_TMP_PATH" "$REMOTE_APP_PATH"
if [[ -n "${AUTH_REGISTRATION_CODE:-}" ]]; then
  sudo AUTH_REGISTRATION_CODE="$AUTH_REGISTRATION_CODE" python3 - <<'PY'
from pathlib import Path
import os

env_path = Path("/etc/foodhelper.env")
code = os.environ["AUTH_REGISTRATION_CODE"]
lines = env_path.read_text().splitlines()
updated = False
for index, line in enumerate(lines):
    if line.startswith("APP_AUTH_REGISTRATION_CODE="):
        lines[index] = f"APP_AUTH_REGISTRATION_CODE={code}"
        updated = True
        break
if not updated:
    lines.append(f"APP_AUTH_REGISTRATION_CODE={code}")
env_path.write_text("\n".join(lines) + "\n")
PY
fi
sudo systemctl restart "$SERVICE_NAME"
sudo systemctl is-active "$SERVICE_NAME"
for _ in {1..30}; do
  if curl -fsS http://localhost:8080/actuator/health; then
    exit 0
  fi
  sleep 2
done
echo "Health check failed after waiting for the service to start" >&2
exit 1
EOF

echo "Deployment finished."
