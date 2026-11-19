

CREATE SCHEMA IF NOT EXISTS "dupasupa_type";

DROP TABLE IF EXISTS "dupasupa_type"."User" CASCADE;
CREATE TABLE IF NOT EXISTS "dupasupa_type"."User" (
  "id" UUID PRIMARY KEY,
  "handle" TEXT NOT NULL,
  "display_name" TEXT NOT NULL DEFAULT '',
  "bio" TEXT NOT NULL DEFAULT ''
);

DROP TABLE IF EXISTS "dupasupa_type"."Campaign" CASCADE;
CREATE TABLE IF NOT EXISTS "dupasupa_type"."Campaign" (
  "id" UUID PRIMARY KEY,
  "owner_id" UUID NOT NULL REFERENCES "dupasupa_type"."User"("id"),
  "title" TEXT NOT NULL
);

CREATE SCHEMA IF NOT EXISTS "dupasupa_rpc";

CREATE OR REPLACE FUNCTION "dupasupa_rpc".ping() RETURNS TEXT AS $$

  SELECT 'pong';

$$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".list_campaigns() RETURNS JSONB AS $$

  DECLARE
    o_rows JSONB;
  BEGIN
    WITH j_ret AS (  
      SELECT "id","owner_id","title" FROM "dupasupa_type"."Campaign"
      ORDER BY "id" ASC)
    SELECT jsonb_agg(j_ret) FROM j_ret INTO o_rows;
    RETURN coalesce(o_rows,('[]')::JSONB);
  END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".validate_campaign_title(
  i_title TEXT
) RETURNS TEXT AS $$
BEGIN
  IF i_title IS NULL OR (btrim(i_title) = '') THEN
    RAISE EXCEPTION 'Campaign title is required';
  END IF;
  IF length(btrim(i_title)) > 120 THEN
    RAISE EXCEPTION 'Campaign title must be 120 characters or fewer';
  END IF;
  RETURN btrim(i_title);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".create_owned_campaign(
  i_user UUID,
  i_title TEXT
) RETURNS JSONB AS $$

  DECLARE
    o_id UUID;
    o_title TEXT;
  BEGIN
    o_id := gen_random_uuid();
    o_title := "dupasupa_rpc".validate_campaign_title(i_title);
    DECLARE
      o_insert JSONB;
      o_row JSONB;
    BEGIN
      WITH j_ret AS (INSERT INTO "dupasupa_type"."Campaign" ("id","owner_id","title") VALUES ((o_id)::UUID,(i_user)::UUID,(o_title)::TEXT) RETURNING "id","owner_id","title")
      SELECT to_jsonb(j_ret) FROM j_ret INTO o_insert;
      WITH j_ret AS (  
        SELECT "id","owner_id","title" FROM "dupasupa_type"."Campaign"
        WHERE "id" = o_id AND "owner_id" = i_user
        LIMIT 1)
      SELECT to_jsonb(j_ret) FROM j_ret INTO o_row;
      RETURN o_row;
    END;
  END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".save_campaign(
  i_id UUID,
  i_title TEXT
) RETURNS JSONB AS $$
BEGIN
  IF (i_title = null) OR (btrim(i_title) = '') THEN
    RAISE EXCEPTION 'Campaign title is required';
  END IF;
  DECLARE
    result JSONB;
  BEGIN
    WITH j_ret AS (  
      UPDATE "dupasupa_type"."Campaign" SET
        "title" = ("dupasupa_rpc".validate_campaign_title(i_title))::TEXT
      WHERE "id" = i_id
      RETURNING "id","owner_id","title")
    SELECT jsonb_agg(j_ret) FROM j_ret INTO result;
    RETURN result;
  END;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".ret_profile(
  i_user UUID
) RETURNS JSONB AS $$

  DECLARE
    o_user JSONB;
  BEGIN
    WITH j_ret AS (  
      SELECT "id","handle","display_name","bio" FROM "dupasupa_type"."User"
      WHERE "id" = i_user
      LIMIT 1)
    SELECT to_jsonb(j_ret) FROM j_ret INTO o_user;
    RETURN o_user;
  END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".save_profile(
  i_user UUID,
  i_handle TEXT,
  i_name TEXT,
  i_bio TEXT
) RETURNS JSONB AS $$
BEGIN
  IF (i_handle = null) OR (length(btrim(i_handle)) < 2) OR (length(i_handle) > 32) THEN
    RAISE EXCEPTION 'Handle must contain 2 to 32 characters';
  END IF;
  IF (i_name = null) OR (btrim(i_name) = '') OR (length(i_name) > 80) THEN
    RAISE EXCEPTION 'Display name must contain 1 to 80 characters';
  END IF;
  IF length(coalesce(i_bio,'')) > 500 THEN
    RAISE EXCEPTION 'Bio must be 500 characters or fewer';
  END IF;
  DECLARE
    o_user JSONB;
  BEGIN
    WITH j_ret AS (  
      UPDATE "dupasupa_type"."User" SET
        "handle" = (btrim(i_handle))::TEXT,
        "display_name" = (btrim(i_name))::TEXT,
        "bio" = (coalesce(i_bio,''))::TEXT
      WHERE "id" = i_user
      RETURNING "id","handle","display_name","bio")
    SELECT jsonb_agg(j_ret) FROM j_ret INTO o_user;
    RETURN "dupasupa_rpc".ret_profile(i_user);
  END;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".list_owned_campaigns(
  i_user UUID
) RETURNS JSONB AS $$

  DECLARE
    o_rows JSONB;
  BEGIN
    WITH j_ret AS (  
      SELECT "id","owner_id","title" FROM "dupasupa_type"."Campaign"
      WHERE "owner_id" = i_user
      ORDER BY "id" ASC)
    SELECT jsonb_agg(j_ret) FROM j_ret INTO o_rows;
    RETURN coalesce(o_rows,('[]')::JSONB);
  END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION "dupasupa_rpc".save_owned_campaign(
  i_user UUID,
  i_id UUID,
  i_title TEXT
) RETURNS JSONB AS $$

  DECLARE
    o_row JSONB;
  BEGIN
    WITH j_ret AS (  
      SELECT "id","owner_id","title" FROM "dupasupa_type"."Campaign"
      WHERE "id" = i_id AND "owner_id" = i_user
      LIMIT 1)
    SELECT to_jsonb(j_ret) FROM j_ret INTO o_row;
    IF o_row is null  THEN
      RAISE EXCEPTION 'Campaign is not available to this account';
    END IF;
    RETURN "dupasupa_rpc".save_campaign(i_id,i_title);
  END;

$$ LANGUAGE 'plpgsql';

