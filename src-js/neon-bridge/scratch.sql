-- Synthetic Foundation fixtures. Managed Auth's schema is never recreated here.
CREATE SCHEMA IF NOT EXISTS neon_scratch;
CREATE SCHEMA IF NOT EXISTS xt_neon_test;
CREATE TABLE IF NOT EXISTS neon_scratch."NeonSmoke" (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id text NOT NULL,
  nickname text NOT NULL
);
ALTER TABLE neon_scratch."NeonSmoke" ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS own_rows ON neon_scratch."NeonSmoke";
CREATE POLICY own_rows ON neon_scratch."NeonSmoke" TO authenticated
  USING (owner_id = auth.user_id()) WITH CHECK (owner_id = auth.user_id());
CREATE TABLE IF NOT EXISTS xt_neon_test.topic_members (
  topic text NOT NULL, user_id text NOT NULL, PRIMARY KEY(topic, user_id)
);
CREATE TABLE IF NOT EXISTS xt_neon_test.fixture_users (
  user_id text PRIMARY KEY, email text NOT NULL UNIQUE, kind text NOT NULL
);
CREATE OR REPLACE FUNCTION neon_scratch.echo(value text)
RETURNS jsonb LANGUAGE sql SECURITY INVOKER AS $$ SELECT jsonb_build_object('value', value) $$;
-- The only privileged write endpoint. Both identity and ownership are checked
-- before changing a row or publishing, independently of the definer's RLS bypass.
CREATE OR REPLACE FUNCTION neon_scratch.set_nickname(row_id uuid, value text, topic text)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path = pg_catalog AS $$
DECLARE result jsonb; subject text := auth.user_id();
BEGIN
  IF subject IS NULL OR NOT EXISTS (
    SELECT 1 FROM xt_neon_test.topic_members m WHERE m.topic = set_nickname.topic AND m.user_id = subject
  ) THEN RAISE EXCEPTION 'Not authorized' USING ERRCODE = '42501'; END IF;
  UPDATE neon_scratch."NeonSmoke" SET nickname = value
    WHERE id = row_id AND owner_id = subject RETURNING to_jsonb("NeonSmoke".*) INTO result;
  IF result IS NULL THEN RAISE EXCEPTION 'Not authorized' USING ERRCODE = '42501'; END IF;
  PERFORM xt_neon.publish(topic, jsonb_build_object('db/sync', jsonb_build_object('NeonSmoke', jsonb_build_array(result))));
  RETURN result;
END $$;
CREATE OR REPLACE FUNCTION neon_scratch.remove_row(row_id uuid, topic text)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path = pg_catalog AS $$
DECLARE removed uuid; subject text := auth.user_id();
BEGIN
  IF subject IS NULL OR NOT EXISTS (
    SELECT 1 FROM xt_neon_test.topic_members m WHERE m.topic = remove_row.topic AND m.user_id = subject
  ) THEN RAISE EXCEPTION 'Not authorized' USING ERRCODE = '42501'; END IF;
  DELETE FROM neon_scratch."NeonSmoke" WHERE id = row_id AND owner_id = subject RETURNING id INTO removed;
  IF removed IS NULL THEN RAISE EXCEPTION 'Not authorized' USING ERRCODE = '42501'; END IF;
  PERFORM xt_neon.publish(topic, jsonb_build_object('db/remove', jsonb_build_object('NeonSmoke', jsonb_build_array(removed))));
  RETURN jsonb_build_object('id', removed);
END $$;
REVOKE ALL ON SCHEMA neon_scratch, xt_neon_test FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA neon_scratch, xt_neon_test FROM PUBLIC;
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA neon_scratch FROM PUBLIC;
GRANT USAGE ON SCHEMA neon_scratch TO authenticated;
GRANT SELECT ON neon_scratch."NeonSmoke" TO authenticated;
GRANT EXECUTE ON FUNCTION neon_scratch.echo(text), neon_scratch.set_nickname(uuid,text,text), neon_scratch.remove_row(uuid,text) TO authenticated;
GRANT USAGE ON SCHEMA xt_neon, xt_neon_test TO neon_bridge;
GRANT SELECT ON xt_neon.topics, xt_neon.events, xt_neon_test.topic_members TO neon_bridge;
