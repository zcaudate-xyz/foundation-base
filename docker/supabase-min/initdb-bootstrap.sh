#!/bin/sh
set -eu

psql_base='psql -v ON_ERROR_STOP=1 -U postgres -d postgres'
$psql_base <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticator') THEN
    CREATE ROLE authenticator NOINHERIT LOGIN PASSWORD 'postgres';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'supabase_auth_admin') THEN
    CREATE ROLE supabase_auth_admin NOINHERIT LOGIN PASSWORD 'postgres';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'supabase_admin') THEN
    CREATE ROLE supabase_admin NOINHERIT LOGIN PASSWORD 'postgres';
  END IF;
END
$$;
SQL

roles_sql=/docker-entrypoint-initdb.d/migrations/99999999999999_roles.sql
if [ -f "$roles_sql" ]; then
  echo "running $roles_sql"
  $psql_base -f "$roles_sql"
fi

for sql in /docker-entrypoint-initdb.d/migrations/*.sql; do
  [ -e "$sql" ] || continue
  [ "$sql" = "$roles_sql" ] && continue
  echo "running $sql"
  $psql_base -f "$sql"
done
