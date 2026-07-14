#!/usr/bin/env python3
"""Download a PostgreSQL dump from the Raspberry Pi FoodHelper server."""

from __future__ import annotations

import argparse
import shlex
import subprocess
from datetime import datetime
from pathlib import Path

from export_legacy_products_recipes_nutrition import postgres_connection_uri


DEFAULT_HOST = "pi-server"
DEFAULT_OUTPUT_DIR = Path("exports/db-dumps")
DEFAULT_REMOTE_ENV_PATH = "/etc/foodhelper.env"
DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost:5432/foodhelper"
DEFAULT_USERNAME = "foodhelper"
DEFAULT_PASSWORD = "foodhelper"


def dump_filename(now: datetime | None = None) -> str:
    current = now or datetime.now()
    return f"foodhelper-{current.strftime('%Y%m%d-%H%M%S')}.dump"


def remote_env_probe_command(env_path: str = DEFAULT_REMOTE_ENV_PATH) -> str:
    return f"""
sudo bash -lc {shlex.quote(f'''
set -euo pipefail
if [[ -f {shlex.quote(env_path)} ]]; then
  set -a
  source {shlex.quote(env_path)}
  set +a
fi
printf '%s\\t%s\\t%s\\n' "${{SPRING_DATASOURCE_URL:-}}" "${{SPRING_DATASOURCE_USERNAME:-}}" "${{SPRING_DATASOURCE_PASSWORD:-}}"
''')}
""".strip()


def remote_dump_command(connection_uri: str, remote_dump_path: str) -> str:
    return (
        "set -euo pipefail; "
        f"pg_dump -Fc --no-owner --no-acl --file {shlex.quote(remote_dump_path)} "
        f"--dbname {shlex.quote(connection_uri)}"
    )


def read_remote_datasource_settings(host: str, env_path: str = DEFAULT_REMOTE_ENV_PATH) -> dict[str, str]:
    completed = subprocess.run(
        ["ssh", host, remote_env_probe_command(env_path)],
        check=True,
        capture_output=True,
        text=True,
    )
    parts = (completed.stdout.strip() or "\t\t").split("\t")
    return {
        "SPRING_DATASOURCE_URL": parts[0] if len(parts) > 0 else "",
        "SPRING_DATASOURCE_USERNAME": parts[1] if len(parts) > 1 else "",
        "SPRING_DATASOURCE_PASSWORD": parts[2] if len(parts) > 2 else "",
    }


def resolve_connection_uri(host: str, database_url: str | None, username: str | None, password: str | None) -> str:
    if database_url and username and password:
        remote_settings = {
            "SPRING_DATASOURCE_URL": "",
            "SPRING_DATASOURCE_USERNAME": "",
            "SPRING_DATASOURCE_PASSWORD": "",
        }
    else:
        remote_settings = read_remote_datasource_settings(host)

    resolved_database_url = database_url or remote_settings["SPRING_DATASOURCE_URL"] or DEFAULT_DATABASE_URL
    resolved_username = username or remote_settings["SPRING_DATASOURCE_USERNAME"] or DEFAULT_USERNAME
    resolved_password = password or remote_settings["SPRING_DATASOURCE_PASSWORD"] or DEFAULT_PASSWORD
    return postgres_connection_uri(resolved_database_url, resolved_username, resolved_password)


def create_dump(
    host: str,
    connection_uri: str,
    local_output_dir: Path,
    remote_tmp_dir: str = "/tmp",
) -> Path:
    local_output_dir.mkdir(parents=True, exist_ok=True)
    filename = dump_filename()
    local_dump_path = local_output_dir / filename
    remote_dump_path = f"{remote_tmp_dir.rstrip('/')}/{filename}"
    remote_command = remote_dump_command(connection_uri, remote_dump_path)

    try:
        subprocess.run(["ssh", host, remote_command], check=True)
        subprocess.run(["ssh", host, f"pg_restore --list {shlex.quote(remote_dump_path)}"], check=True, capture_output=True, text=True)
        subprocess.run(["scp", f"{host}:{remote_dump_path}", str(local_dump_path)], check=True)
        subprocess.run(["file", str(local_dump_path)], check=True, capture_output=True, text=True)
        return local_dump_path
    finally:
        subprocess.run(
            ["ssh", host, f"rm -f {shlex.quote(remote_dump_path)}"],
            check=False,
            capture_output=True,
            text=True,
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Download a PostgreSQL dump from the FoodHelper Raspberry Pi server.")
    parser.add_argument("--host", default=DEFAULT_HOST, help="SSH host for the Raspberry Pi (default: pi-server)")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=DEFAULT_OUTPUT_DIR,
        help="Local directory where the dump will be stored (default: exports/db-dumps)",
    )
    parser.add_argument("--database-url", help="Optional PostgreSQL JDBC URL override.")
    parser.add_argument("--username", help="Optional PostgreSQL username override.")
    parser.add_argument("--password", help="Optional PostgreSQL password override.")
    parser.add_argument(
        "--remote-tmp-dir",
        default="/tmp",
        help="Remote temporary directory for the dump file (default: /tmp)",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    connection_uri = resolve_connection_uri(args.host, args.database_url, args.username, args.password)
    dump_path = create_dump(args.host, connection_uri, args.output_dir, args.remote_tmp_dir)
    print(dump_path)


if __name__ == "__main__":
    main()
