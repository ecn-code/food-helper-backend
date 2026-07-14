import sys
import unittest
from datetime import datetime
from pathlib import Path
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parent))

from dump_foodhelper_database import (  # noqa: E402
    dump_filename,
    remote_dump_command,
    remote_env_probe_command,
    resolve_connection_uri,
)


class DumpFoodHelperDatabaseTest(unittest.TestCase):
    def test_dump_filename_uses_timestamp(self):
        self.assertEqual(dump_filename(datetime(2026, 7, 12, 13, 45, 59)), "foodhelper-20260712-134559.dump")

    def test_remote_env_probe_command_reads_env_file_and_prints_json(self):
        command = remote_env_probe_command()

        self.assertIn("sudo bash -lc", command)
        self.assertIn("source /etc/foodhelper.env", command)
        self.assertIn("printf", command)
        self.assertIn("SPRING_DATASOURCE_URL", command)
        self.assertIn("SPRING_DATASOURCE_USERNAME", command)
        self.assertIn("SPRING_DATASOURCE_PASSWORD", command)

    def test_remote_dump_command_uses_pg_dump_custom_format(self):
        command = remote_dump_command(
            "postgresql://foodhelper:secret%20password@localhost:5432/foodhelper",
            "/tmp/foodhelper-20260712-134559.dump",
        )

        self.assertIn("pg_dump -Fc --no-owner --no-acl", command)
        self.assertIn("--file /tmp/foodhelper-20260712-134559.dump", command)
        self.assertIn("--dbname", command)
        self.assertIn("postgresql://foodhelper:secret%20password@localhost:5432/foodhelper", command)

    def test_resolve_connection_uri_prefers_cli_over_remote_env(self):
        with patch("dump_foodhelper_database.read_remote_datasource_settings") as read_remote:
            read_remote.return_value = {
                "SPRING_DATASOURCE_URL": "jdbc:postgresql://server.internal:5432/foodhelper",
                "SPRING_DATASOURCE_USERNAME": "server-user",
                "SPRING_DATASOURCE_PASSWORD": "server secret",
            }

            connection_uri = resolve_connection_uri(
                "pi-server",
                "jdbc:postgresql://override.internal:5432/foodhelper",
                "override-user",
                "override secret",
            )

        self.assertEqual(
            connection_uri,
            "postgresql://override-user:override%20secret@override.internal:5432/foodhelper",
        )
        read_remote.assert_not_called()


if __name__ == "__main__":
    unittest.main()
