const net = require('net');
const vscode = require('vscode');

let server = null;
let connections = [];

function safeStringify(value) {
  try {
    return JSON.stringify(value);
  } catch (e) {
    return JSON.stringify(String(value));
  }
}

async function evaluateCode(code) {
  try {
    const result = eval(code);
    const value = (result && typeof result.then === 'function')
      ? await result
      : result;
    return { status: 'ok', value };
  } catch (e) {
    return { status: 'error', error: String(e && e.message ? e.message : e) };
  }
}

async function handleLine(line, socket) {
  let request;
  try {
    request = JSON.parse(line);
  } catch (e) {
    socket.write(JSON.stringify({ id: null, status: 'error', error: 'Invalid JSON: ' + e.message }) + '\n');
    return;
  }
  const id = request.id;
  const code = request.code;
  if (typeof code !== 'string') {
    socket.write(JSON.stringify({ id, status: 'error', error: 'Missing or non-string "code" field' }) + '\n');
    return;
  }
  const result = await evaluateCode(code);
  socket.write(JSON.stringify(Object.assign({ id }, result)) + '\n');
}

function activate(context) {
  const portEnv = process.env.HARA_VSCODE_PORT;
  const port = portEnv ? parseInt(portEnv, 10) : 0;
  if (isNaN(port)) {
    console.error('[hara-runtime] HARA_VSCODE_PORT is not a valid number');
    return;
  }

  // Expose the vscode API to evaluated code.
  globalThis.vscode = vscode;

  server = net.createServer((socket) => {
    connections.push(socket);
    let buffer = '';
    socket.on('data', (data) => {
      buffer += data.toString('utf8');
      let idx;
      while ((idx = buffer.indexOf('\n')) !== -1) {
        const line = buffer.substring(0, idx);
        buffer = buffer.substring(idx + 1);
        if (line.length > 0) {
          handleLine(line, socket);
        }
      }
    });
    socket.on('close', () => {
      connections = connections.filter((c) => c !== socket);
    });
    socket.on('error', (err) => {
      console.error('[hara-runtime] socket error:', err.message);
    });
  });

  server.listen(port, '127.0.0.1', () => {
    const actualPort = server.address().port;
    console.log('[hara-runtime] listening on 127.0.0.1:' + actualPort);
  });

  server.on('error', (err) => {
    console.error('[hara-runtime] server error:', err.message);
  });

  context.subscriptions.push({
    dispose: () => {
      connections.forEach((socket) => socket.destroy());
      connections = [];
      if (server) {
        server.close();
        server = null;
      }
    }
  });
}

function deactivate() {
  connections.forEach((socket) => socket.destroy());
  connections = [];
  if (server) {
    server.close();
    server = null;
  }
}

module.exports = { activate, deactivate };
