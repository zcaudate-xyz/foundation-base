create schema if not exists _realtime;

create table if not exists _realtime.tenants (
  id uuid primary key,
  name text not null,
  external_id text not null unique,
  jwt_secret text not null,
  jwt_jwks jsonb,
  postgres_cdc_default text not null default 'postgres_cdc_rls',
  max_concurrent_users integer not null default 0,
  max_events_per_second integer not null default 0,
  max_bytes_per_second integer not null default 0,
  max_channels_per_client integer not null default 0,
  max_joins_per_second integer not null default 0,
  suspend boolean not null default false,
  enable_authorization boolean not null default false,
  inserted_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

insert into _realtime.tenants (
  id,
  name,
  external_id,
  jwt_secret,
  jwt_jwks,
  postgres_cdc_default,
  max_concurrent_users,
  max_events_per_second,
  max_bytes_per_second,
  max_channels_per_client,
  max_joins_per_second,
  suspend,
  enable_authorization
)
values (
  '00000000-0000-0000-0000-000000000001',
  'realtime',
  'realtime',
  'jFoU4OCv48jr8rciJPYC/dIqREu2bfTki5kxAalbZOxNdvG1dbagBa+WghEURyP4AyfclHFDDYn2+vY1HQ/OJA==',
  null,
  'postgres_cdc_rls',
  0,
  0,
  0,
  0,
  0,
  false,
  false
)
on conflict (external_id) do update
set name = excluded.name,
    jwt_secret = excluded.jwt_secret,
    jwt_jwks = excluded.jwt_jwks,
    postgres_cdc_default = excluded.postgres_cdc_default,
    max_concurrent_users = excluded.max_concurrent_users,
    max_events_per_second = excluded.max_events_per_second,
    max_bytes_per_second = excluded.max_bytes_per_second,
    max_channels_per_client = excluded.max_channels_per_client,
    max_joins_per_second = excluded.max_joins_per_second,
    suspend = excluded.suspend,
    enable_authorization = excluded.enable_authorization,
    updated_at = now();
