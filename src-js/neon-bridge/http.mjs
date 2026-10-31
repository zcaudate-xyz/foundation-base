import { createServer } from 'node:http';

export function serve(bridge, { port = 8787, host = '127.0.0.1', health } = {}) {
  const server = createServer(async (req, res) => {
    if (health && req.method === 'GET' && req.url === '/live') {
      res.writeHead(200, { 'Content-Type': 'application/json' }).end('{"status":"alive"}');
      return;
    }
    if (health && req.method === 'GET' && req.url === '/health') {
      try { await health(); res.writeHead(200, { 'Content-Type': 'application/json' }).end('{"status":"ready"}'); }
      catch { res.writeHead(503, { 'Content-Type': 'application/json' }).end('{"status":"unavailable"}'); }
      return;
    }
    const controller = new AbortController();
    req.once('aborted', () => controller.abort());
    res.once('close', () => { if (!res.writableEnded) controller.abort(); });
    try {
      let size = 0; const chunks = [];
      for await (const chunk of req) { size += chunk.length; if (size > 65536) { res.writeHead(413).end(); return; } chunks.push(chunk); }
      const request = new Request(`http://localhost${req.url}`, { method: req.method, headers: req.headers,
        body: ['GET','HEAD'].includes(req.method) ? undefined : Buffer.concat(chunks), signal: controller.signal });
      const response = await bridge.handle(request);
      res.writeHead(response.status, Object.fromEntries(response.headers));
      res.end(Buffer.from(await response.arrayBuffer()));
    } catch { if (!res.headersSent) res.writeHead(500); res.end(); }
  });
  server.listen(port, host);
  return server;
}
