-- Official Supabase Realtime self-host bootstrap.
-- Must run before the realtime container starts so that /app/bin/migrate
-- can create _realtime.tenants / _realtime.extensions and the realtime
-- schema_migrations table in the `realtime` schema.
\set pguser `echo "supabase_admin"`

create schema if not exists _realtime;
alter schema _realtime owner to :pguser;

create schema if not exists realtime;
alter schema realtime owner to :pguser;
