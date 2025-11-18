#!/usr/bin/env python3
"""
Simple helper to migrate legacy sqlite rogue_data.db into MySQL/MariaDB.

Usage:
    1. Install deps: pip install mysql-connector-python
    2. Update the MYSQL_* constants or read from environment variables.
    3. Run: python sqlite_to_mysql.py /path/to/rogue_data.db
"""
import json
import os
import sqlite3
import sys
from datetime import datetime

import mysql.connector

MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_DB = os.getenv("MYSQL_DB", "arknights_bot")
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "password")

INSERT_SQL = """
INSERT INTO roguelike_run (id, uid, theme_id, start_ts, record_json, created_at, updated_at)
VALUES (%s, %s, %s, %s, %s, %s, %s)
ON DUPLICATE KEY UPDATE
  uid = VALUES(uid),
  theme_id = VALUES(theme_id),
  start_ts = VALUES(start_ts),
  record_json = VALUES(record_json),
  updated_at = VALUES(updated_at);
"""


def migrate(sqlite_path: str):
    conn_sqlite = sqlite3.connect(sqlite_path)
    cur_sqlite = conn_sqlite.cursor()
    cur_sqlite.execute("SELECT id, uid, theme, start_ts, record_data FROM rogue_runs")

    mysql_conn = mysql.connector.connect(
        host=MYSQL_HOST,
        port=MYSQL_PORT,
        database=MYSQL_DB,
        user=MYSQL_USER,
        password=MYSQL_PASSWORD,
    )
    mysql_cur = mysql_conn.cursor()

    rows = cur_sqlite.fetchall()
    now = datetime.utcnow()
    for row in rows:
        run_id, uid, theme, start_ts, data = row
        mysql_cur.execute(
            INSERT_SQL,
            (run_id, uid, theme, start_ts, data, now, now),
        )

    mysql_conn.commit()
    mysql_cur.close()
    mysql_conn.close()
    cur_sqlite.close()
    conn_sqlite.close()
    print(f"Migrated {len(rows)} runs from {sqlite_path} to MySQL.")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python sqlite_to_mysql.py /path/to/rogue_data.db")
        sys.exit(1)
    migrate(sys.argv[1])

