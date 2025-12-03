CREATE SCHEMA IF NOT EXISTS "scratch";
CREATE EXTENSION IF NOT EXISTS "citext";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- rt.postgres.script.scratch/as-array [16] 
CREATE OR REPLACE FUNCTION "scratch".as_array(
  input JSONB
) RETURNS JSONB AS $$
BEGIN
  IF input = '{}' THEN
    RETURN '[]';
  END IF;
  RETURN input;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/EnumStatus [24] 
DO $$
BEGIN
  CREATE TYPE "scratch"."EnumStatus" AS ENUM ('pending','error','success');
EXCEPTION WHEN OTHERS THEN
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/TaskCache [50] 
DROP TABLE IF EXISTS "scratch"."TaskCache" CASCADE;
CREATE TABLE IF NOT EXISTS "scratch"."TaskCache" (
  "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  "op_created" UUID,
  "op_updated" UUID,
  "time_created" BIGINT,
  "time_updated" BIGINT,
  "__deleted__" BOOLEAN DEFAULT false
);

-- rt.postgres.script.scratch/Task [60] 
DROP TABLE IF EXISTS "scratch"."Task" CASCADE;
CREATE TABLE IF NOT EXISTS "scratch"."Task" (
  "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  "status" "scratch"."EnumStatus" NOT NULL,
  "name" TEXT NOT NULL,
  "cache_id" UUID NOT NULL REFERENCES "scratch"."TaskCache"("id"),
  "op_created" UUID,
  "op_updated" UUID,
  "time_created" BIGINT,
  "time_updated" BIGINT,
  "__deleted__" BOOLEAN DEFAULT false,
  UNIQUE ("name")
);
CREATE INDEX ON "scratch"."Task" USING HASH ("name");

-- rt.postgres.script.scratch/Entry [76] 
DROP TABLE IF EXISTS "scratch"."Entry" CASCADE;
CREATE TABLE IF NOT EXISTS "scratch"."Entry" (
  "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  "name" TEXT NOT NULL,
  "tags" JSONB NOT NULL,
  "op_created" UUID,
  "op_updated" UUID,
  "time_created" BIGINT,
  "time_updated" BIGINT,
  "__deleted__" BOOLEAN DEFAULT false,
  UNIQUE ("name")
);
CREATE INDEX ON "scratch"."Entry" USING HASH ("name");

-- rt.postgres.script.scratch/entry-all [89] 
CREATE OR REPLACE FUNCTION "scratch".entry_all() RETURNS JSONB AS $$

  WITH o AS (SELECT "id" FROM "scratch"."Entry") (SELECT jsonb_agg(o.id) FROM o);

$$ LANGUAGE 'sql';

-- rt.postgres.script.scratch/entry-by-name [94] 
CREATE OR REPLACE FUNCTION "scratch".entry_by_name(
  i_name TEXT
) RETURNS JSONB AS $$

  WITH o AS (  
    SELECT "id" FROM "scratch"."Entry"
    WHERE "name" = i_name) (SELECT jsonb_agg(o.id) FROM o);

$$ LANGUAGE 'sql';

-- rt.postgres.script.scratch/entry-default [100] 
CREATE OR REPLACE FUNCTION "scratch".entry_default(
  i_entry_id UUID
) RETURNS JSONB AS $$

  WITH j_ret AS (  
    SELECT "id","name","tags","time_created","time_updated" FROM "scratch"."Entry"
    WHERE "id" = i_entry_id
    LIMIT 1)
  SELECT to_jsonb(j_ret) FROM j_ret;

$$ LANGUAGE 'sql';

-- rt.postgres.script.scratch/as-upper [107] 
CREATE OR REPLACE FUNCTION "scratch".as_upper(
  input CITEXT
) RETURNS CITEXT AS $$

  SELECT (upper((input)::TEXT))::CITEXT;

$$ LANGUAGE 'sql' IMMUTABLE PARALLEL SAFE;

-- rt.postgres.script.scratch/ping [116] 
CREATE OR REPLACE FUNCTION "scratch".ping() RETURNS TEXT AS $$
BEGIN
  RETURN 'pong';
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/ping-ok [123] 
CREATE OR REPLACE FUNCTION "scratch".ping_ok() RETURNS JSONB AS $$
BEGIN
  RETURN jsonb_build_object('reply','ok');
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/echo [130] 
CREATE OR REPLACE FUNCTION "scratch".echo(
  input JSONB
) RETURNS JSONB AS $$
BEGIN
  RETURN input;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/addf [137] 
CREATE OR REPLACE FUNCTION "scratch".addf(
  x NUMERIC,
  y NUMERIC
) RETURNS NUMERIC AS $$
BEGIN
  RETURN x + y;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/subf [144] 
CREATE OR REPLACE FUNCTION "scratch".subf(
  x NUMERIC,
  y NUMERIC
) RETURNS NUMERIC AS $$
BEGIN
  RETURN x - y;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/mulf [151] 
CREATE OR REPLACE FUNCTION "scratch".mulf(
  x NUMERIC,
  y NUMERIC
) RETURNS NUMERIC AS $$
BEGIN
  RETURN x * y;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/divf [158] 
CREATE OR REPLACE FUNCTION "scratch".divf(
  x NUMERIC,
  y NUMERIC
) RETURNS NUMERIC AS $$
BEGIN
  RETURN x / y;
END;
$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/insert-task [165] 
CREATE OR REPLACE FUNCTION "scratch".insert_task(
  i_name TEXT,
  i_status TEXT,
  o_op JSONB
) RETURNS JSONB AS $$

  DECLARE
    out JSONB;
  BEGIN
    DECLARE
      p_id_00 UUID;
      p_id_01 UUID;
      gid_1 JSONB;
      gid_2 JSONB;
    BEGIN
      p_id_01 := uuid_generate_v4();
      p_id_00 := uuid_generate_v4();
      WITH j_ret AS (  
        INSERT INTO "scratch"."TaskCache" ("id","op_created","op_updated","time_created","time_updated") VALUES (
          (p_id_01)::UUID,
          (o_op ->> 'id')::UUID,
          (o_op ->> 'id')::UUID,
          (o_op ->> 'time')::BIGINT,
          (o_op ->> 'time')::BIGINT
        ) RETURNING *)
      SELECT to_jsonb(j_ret) FROM j_ret INTO gid_2;
      WITH j_ret AS (  
        INSERT INTO "scratch"."Task" (
          "id",
          "status",
          "name",
          "cache_id",
          "op_created",
          "op_updated",
          "time_created",
          "time_updated"
        ) VALUES (
          (p_id_00)::UUID,
          (i_status)::"scratch"."EnumStatus",
          (i_name)::TEXT,
          (p_id_01)::UUID,
          (o_op ->> 'id')::UUID,
          (o_op ->> 'id')::UUID,
          (o_op ->> 'time')::BIGINT,
          (o_op ->> 'time')::BIGINT
        ) RETURNING *)
      SELECT to_jsonb(j_ret) FROM j_ret INTO gid_1;
      gid_1 := (gid_1 || jsonb_build_object('cache',gid_2));
      SELECT gid_1 INTO out;
    END;
    RETURN out;
  END;

$$ LANGUAGE 'plpgsql';

-- rt.postgres.script.scratch/insert-entry [176] 
CREATE OR REPLACE FUNCTION "scratch".insert_entry(
  i_name TEXT,
  i_tags JSONB,
  o_op JSONB
) RETURNS JSONB AS $$

  DECLARE
    out JSONB;
  BEGIN
    DECLARE
      p_id_00 UUID;
      gid_1 JSONB;
    BEGIN
      p_id_00 := uuid_generate_v4();
      WITH j_ret AS (  
        INSERT INTO "scratch"."Entry" (
          "id",
          "name",
          "tags",
          "op_created",
          "op_updated",
          "time_created",
          "time_updated"
        ) VALUES (
          (p_id_00)::UUID,
          (i_name)::TEXT,
          "scratch".as_array((i_tags)::JSONB),
          (o_op ->> 'id')::UUID,
          (o_op ->> 'id')::UUID,
          (o_op ->> 'time')::BIGINT,
          (o_op ->> 'time')::BIGINT
        ) RETURNING *)
      SELECT to_jsonb(j_ret) FROM j_ret INTO gid_1;
      SELECT gid_1 INTO out;
    END;
    RETURN out;
  END;

$$ LANGUAGE 'plpgsql';