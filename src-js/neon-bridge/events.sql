-- Install in a private schema, never an exposed Data API schema.
CREATE SCHEMA IF NOT EXISTS xt_neon;
CREATE TABLE IF NOT EXISTS xt_neon.topics (
  topic text PRIMARY KEY,
  head bigint NOT NULL DEFAULT 0,
  floor bigint NOT NULL DEFAULT 0 CHECK (floor <= head)
);
CREATE TABLE IF NOT EXISTS xt_neon.events (
  topic text NOT NULL REFERENCES xt_neon.topics(topic),
  seq bigint NOT NULL,
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  payload jsonb NOT NULL CHECK (jsonb_typeof(payload) = 'object'
    AND (payload ? 'db/sync' OR payload ? 'db/remove')),
  PRIMARY KEY (topic, seq), UNIQUE (id)
);
-- The topic row lock is held to commit. A sequence cannot overtake an uncommitted predecessor.
CREATE OR REPLACE FUNCTION xt_neon.publish(p_topic text, p_payload jsonb)
RETURNS bigint LANGUAGE plpgsql SECURITY INVOKER AS $$
DECLARE v_seq bigint;
BEGIN
  INSERT INTO xt_neon.topics(topic) VALUES (p_topic) ON CONFLICT DO NOTHING;
  UPDATE xt_neon.topics SET head = head + 1 WHERE topic = p_topic RETURNING head INTO v_seq;
  INSERT INTO xt_neon.events(topic, seq, payload) VALUES (p_topic, v_seq, p_payload);
  RETURN v_seq;
END $$;
CREATE OR REPLACE FUNCTION xt_neon.prune(p_topic text, p_through bigint)
RETURNS void LANGUAGE plpgsql SECURITY INVOKER AS $$
BEGIN
  UPDATE xt_neon.topics SET floor = greatest(floor, least(head, p_through)) WHERE topic = p_topic;
  DELETE FROM xt_neon.events WHERE topic = p_topic
    AND seq <= (SELECT floor FROM xt_neon.topics WHERE topic = p_topic);
END $$;
REVOKE ALL ON SCHEMA xt_neon FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA xt_neon FROM PUBLIC;
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA xt_neon FROM PUBLIC;
-- Grant the bridge role SELECT only; grant publishers EXECUTE plus required table privileges.
-- Only the retention operator may call prune. Publish and application writes share a transaction.
