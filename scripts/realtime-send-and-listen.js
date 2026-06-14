#!/usr/bin/env node
// Standalone Node.js script that calls realtime.send() in Postgres and listens
// for the resulting broadcast on a local Supabase Realtime WebSocket.
//
// Requires:
//   - docker/supabase-min stack running (Postgres on 55122, Kong on 55121)
//   - node_modules/pg and node_modules/ws installed
//
// Usage:
//   node scripts/realtime-send-and-listen.js [room-suffix]

const WebSocket = require("ws");
const { Client } = require("pg");

const HOST = "127.0.0.1";
const KONG_PORT = 55121;
const PG_PORT = 55122;
const ANON_KEY =
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0";

const suffix = process.argv[2] || Date.now();
const SUB_TOPIC = `room:js-test-${suffix}`;
const FULL_TOPIC = `realtime:${SUB_TOPIC}`;
const EVENT = "my-event";
const PAYLOAD = { hello: "from postgres", sentAt: new Date().toISOString() };

function sendFrame(ws, frame) {
  const [joinRef, ref, topic, event, payload] = frame;
  ws.send(JSON.stringify([joinRef, ref, topic, event, payload]));
}

function decodeFrame(data) {
  const arr = JSON.parse(data);
  return {
    join_ref: arr[0],
    ref: arr[1],
    topic: arr[2],
    event: arr[3],
    payload: arr[4] ?? {},
  };
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
    sendFrame(ws, [
      "join-1",
      "join-1",
      FULL_TOPIC,
      "phx_join",
      {
        access_token: ANON_KEY,
        config: { broadcast: { ack: false, self: false } },
      },
    ]);
  });

  ws.on("message", async (data) => {
    const frame = decodeFrame(data);
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
      console.log("[ws] received broadcast:", JSON.stringify(frame.payload, null, 2));
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
      password: "postgres",
      database: "postgres",
    });

    try {
      await pg.connect();
      const sql =
        "SELECT realtime.send($1::jsonb, $2, $3, $4) AS message_id";
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

  // Safety timeout
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
