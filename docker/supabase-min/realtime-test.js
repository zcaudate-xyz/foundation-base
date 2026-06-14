const WebSocket = require('ws');
const { exec } = require('child_process');

const APIKEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0';
const URL = `ws://127.0.0.1:55121/realtime/v1/websocket?vsn=2.0.0&apikey=${APIKEY}`;

let ref = 0;
let joined = false;
let eventReceived = false;
const ws = new WebSocket(URL);

function send(topic, event, payload = {}) {
  ref++;
  const msg = [ref.toString(), ref.toString(), topic, event, payload];
  console.log('SEND', JSON.stringify(msg));
  ws.send(JSON.stringify(msg));
}

ws.on('open', () => {
  console.log('WS connected');
  setInterval(() => send('phoenix', 'heartbeat', {}), 30000);
  setTimeout(() => {
    send('realtime:public:Entry', 'phx_join', {
      config: {
        postgres_changes: [{ event: '*', schema: 'public', table: 'Entry' }]
      }
    });
  }, 1000);
});

ws.on('message', (data) => {
  const text = data.toString();
  console.log('RECV', text);
  try {
    const [, , topic, event, payload] = JSON.parse(text);
    if (topic === 'realtime:public:Entry' && event === 'phx_reply') {
      if (payload.status === 'ok') {
        joined = true;
        console.log('JOIN OK');
        setTimeout(insertRow, 1000);
      } else {
        console.error('JOIN FAILED', payload);
        setTimeout(() => ws.close(), 1000);
      }
    }
    if (topic === 'realtime:public:Entry' && event === 'postgres_changes') {
      eventReceived = true;
      console.log('EVENT RECEIVED');
      setTimeout(() => ws.close(), 1000);
    }
  } catch (e) {
    // ignore
  }
});

ws.on('error', (err) => {
  console.error('WS error:', err.message);
});

ws.on('close', (code, reason) => {
  console.log('WS closed', code, reason.toString());
  setTimeout(() => {
    if (joined && eventReceived) {
      console.log('\nSUCCESS: joined and received a realtime event');
      process.exit(0);
    } else if (joined) {
      console.log('\nFAILURE: joined but no event received');
      process.exit(1);
    } else {
      console.log('\nFAILURE: never joined');
      process.exit(1);
    }
  }, 500);
});

function insertRow() {
  console.log('INSERTING ROW');
  const env = Object.assign({}, process.env, { PGPASSWORD: 'postgres' });
  const sql = "INSERT INTO public.\"Entry\" (text) VALUES ('hello from kimicode test');\n";
  const child = exec(
    'psql -h 127.0.0.1 -p 55122 -U postgres -d postgres',
    { cwd: __dirname, env },
    (err, stdout, stderr) => {
      if (err) {
        console.error('INSERT ERROR', err, stderr);
      } else {
        console.log('INSERT OK', stdout.trim());
      }
    }
  );
  child.stdin.write(sql);
  child.stdin.end();
}

// Overall timeout
setTimeout(() => {
  console.log('Overall timeout reached');
  ws.close();
}, 15000);
