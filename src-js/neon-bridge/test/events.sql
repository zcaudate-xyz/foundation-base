BEGIN;
\i events.sql
DO $$
DECLARE n bigint; p jsonb;
BEGIN
  n := xt_neon.publish('foundation-neon-test', '{"db/sync":{"T":[{"id":1}]}}');
  IF n <> 1 THEN RAISE EXCEPTION 'first sequence'; END IF;
  n := xt_neon.publish('foundation-neon-test', '{"db/remove":{"T":[1]}}');
  IF n <> 2 THEN RAISE EXCEPTION 'second sequence'; END IF;
  SELECT payload INTO p FROM xt_neon.events WHERE topic='foundation-neon-test' AND seq=2;
  IF p <> '{"db/remove":{"T":[1]}}'::jsonb THEN RAISE EXCEPTION 'payload'; END IF;
  PERFORM xt_neon.prune('foundation-neon-test',1);
  IF (SELECT floor FROM xt_neon.topics WHERE topic='foundation-neon-test') <> 1 THEN RAISE EXCEPTION 'floor'; END IF;
  IF (SELECT count(*) FROM xt_neon.events WHERE topic='foundation-neon-test') <> 1 THEN RAISE EXCEPTION 'retention'; END IF;
END $$;
ROLLBACK;
