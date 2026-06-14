#!/usr/bin/env node
// Standalone Node.js script that calls realtime.send() in Postgres and listens
// for the resulting broadcast on a local Supabase Realtime WebSocket.
//
// Works with both the `docker/compose-min` stack and the new
// `scaffold.supabase.local-min` (`supabase start --workdir docker/local-min`)
// setup because both expose the same ports and use the same default JWT anon
// token that Realtime validates.
//
// Requires:
//   - a local Supabase stack running on 127.0.0.1:55121 (gateway) and :55122 (db)
//   - node_modules/pg and node_modules/ws installed
//
// Usage:
//   node scripts/realtime-send-and-listen.js [room-suffix]

const WebSocket = require("ws");
const { Client } = require("pg");

const HOST = process.env.SUPABASE_HOST || "127.0.0.1";
const KONG_PORT = process.env.SUPABASE_API_PORT || 55121;
const PG_PORT = process.env.SUPABASE_DB_PORT || 55122;
const PG_PASSWORD = process.env.SUPABASE_DB_PASSWORD || "postgres";

// Default anon JWT from config/scaffold/supabase-min.edn.  In the local-min
// setup Kong also accepts the publishable key, but the JWT falls through and
// is accepted directly by Realtime as both apikey and access_token.
const ANON_KEY =
  process.env.SUPABASE_ANON_KEY ||
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0";

const suffix = process.argv[2] || Date.now();
const SUB_TOPIC = `room:js-test-${suffix}`;
const FULL_TOPIC = `realtime:${SUB_TOPIC}`;
const EVENT = "my-event";
const PAYLOAD = { hello: "from postgres", sentAt: new Date().toISOString() };

function encodeTextFrame(frame) {
  const [joinRef, ref, topic, event, payload] = frame;
  return JSON.stringify([joinRef, ref, topic, event, payload]);
}

function decodeTextFrame(data) {
  const arr = JSON.parse(data);
  return {
    join_ref: arr[0],
    ref: arr[1],
    topic: arr[2],
    event: arr[3],
    payload: arr[4] ?? {},
  };
}

// Server-sent broadcast binary frame (type 0x04) per the Realtime protocol.
// See: https://supabase.com/docs/guides/realtime/protocol
function decodeBinaryFrame(buf) {
  const type = buf[0];
  if (type !== 0x04) {
    throw new Error(
      `Unsupported binary frame type 0x${type.toString(16).padStart(2, "0")}`
    );
  }

  let i = 1;
  const topicLen = buf[i++];
  const eventLen = buf[i++];
  const metaLen = buf[i++];
  const payloadEnc = buf[i++]; // 0 = binary, 1 = JSON

  const topic = buf.toString("utf8", i, i + topicLen);
  i += topicLen;

  const userEvent = buf.toString("utf8", i, i + eventLen);
  i += eventLen;

  const metadata = metaLen > 0
    ? JSON.parse(buf.toString("utf8", i, i + metaLen))
    : {};
  i += metaLen;

  const userPayloadRaw = buf.slice(i);
  const userPayload =
    payloadEnc === 1
      ? JSON.parse(userPayloadRaw.toString("utf8"))
      : userPayloadRaw;

  return {
    join_ref: null,
    ref: null,
    topic,
    event: "broadcast",
    payload: {
      event: userEvent,
      type: "broadcast",
      meta: metadata,
      payload: userPayload,
    },
  };
}

function decodeMessage(data, isBinary) {
  if (isBinary) {
    return decodeBinaryFrame(Buffer.from(data));
  }
  return decodeTextFrame(String(data));
}

async function main() {
  const ws = new WebSocket(
    `ws://${HOST}:${KONG_PORT}/realtime/v1/websocket?vsn=2.0.0&apikey=${ANON_KEY}`,
    [],
    { headers: { Host: "realtime-dev.supabase-realtime" } }
  );

  let joined = false;

  ws.on("open", () => {
    console.log("[ws] connected");
    ws.send(
      encodeTextFrame([
        "join-1",
        "join-1",
        FULL_TOPIC,
        "phx_join",
        {
          access_token: ANON_KEY,
          config: { broadcast: { ack: false, self: false } },
        },
      ])
    );
  });

  ws.on("message", async (data, isBinary) => {
    const frame = decodeMessage(data, isBinary);
    console.log("[ws] message:", JSON.stringify(frame));

    if (frame.event === "phx_reply" && frame.ref === "join-1") {
      if (frame.payload.status === "ok") {
        joined = true;
        console.log("[ws] joined", FULL_TOPIC);
        await new Promise((r) => setTimeout(r, 2000));
        await sendViaPostgres();
      } else {
        console.error("[ws] join failed:", frame.payload);
        ws.close();
      }
    }

    if (frame.event === "broadcast" && frame.payload.event === EVENT) {
      console.log(
        "[ws] received broadcast:",
        JSON.stringify(frame.payload, null, 2)
      );
      ws.close();
      process.exit(0);
    }
  });

  ws.on("error", (err) => {
    console.error("[ws] error:", err.message || err);
  });

  ws.on("close", (code) => {
    console.log("[ws] closed", code);
    if (!joined) {
      process.exit(1);
    }
  });

  async function sendViaPostgres() {
    const pg = new Client({
      host: HOST,
      port: PG_PORT,
      user: "postgres",
      password: PG_PASSWORD,
      database: "postgres",
    });

    try {
      await pg.connect();
      const sql =
        "SELECT realtime.send(cast($1 as jsonb), $2, $3, $4) AS message_id";
      const params = [JSON.stringify(PAYLOAD), EVENT, SUB_TOPIC, false];
      const res = await pg.query(sql, params);
      console.log("[pg] realtime.send returned:", res.rows[0]);
    } catch (err) {
      console.error("[pg] error:", err.message || err);
      ws.close();
    } finally {
      await pg.end();
    }
  }

  setTimeout(() => {
    console.error("[timeout] did not receive broadcast within 10s");
    ws.close();
    process.exit(1);
  }, 10000);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
