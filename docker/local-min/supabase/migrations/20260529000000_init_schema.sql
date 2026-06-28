create schema if not exists scratch;
create schema if not exists scratch_v0;
create schema if not exists scratch_v3;

grant usage on schema scratch_v3 to anon, authenticated, service_role;
alter default privileges for role postgres in schema scratch_v3
  grant all on tables to anon, authenticated, service_role;
alter default privileges for role postgres in schema scratch_v3
  grant all on functions to anon, authenticated, service_role;
