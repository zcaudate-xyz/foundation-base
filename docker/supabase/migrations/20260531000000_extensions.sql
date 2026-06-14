create schema if not exists _realtime;

create table if not exists _realtime.extensions (
  id uuid primary key,
  type text not null,
  settings jsonb,
  tenant_external_id text not null,
  inserted_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists extensions_tenant_external_id_idx on _realtime.extensions (tenant_external_id);

insert into _realtime.extensions (
  id,
  type,
  settings,
  tenant_external_id
)
values (
  '00000000-0000-0000-0000-000000000002',
  'postgres_cdc_rls',
  '{}'::jsonb,
  'realtime'
)
on conflict (tenant_external_id) do update
set type = excluded.type,
    settings = excluded.settings,
    updated_at = now();
