const listeners = new Set();
let socket = null;

export function addMessageListener(callback) {
    listeners.add(callback);
    return () => listeners.delete(callback);
}

export function send(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(typeof message === 'string' ? message : JSON.stringify(message));
    } else {
        console.warn('REPL not connected');
    }
}

export function connect(host, port, options = {}) {
    const protocol = options.secured ? 'wss' : 'ws';
    const url = `${protocol}://${host}:${port}/${options.path || ''}`;

    console.log(`Connecting to REPL at ${url}...`);

    const ws = new WebSocket(url);
    socket = ws;

    ws.onopen = () => {
        console.log('REPL Connected');
        // Keep alive if needed
        setInterval(() => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send('ping');
            }
        }, 30000);
    };

    ws.onmessage = (event) => {
        if (event.data === 'pong') return;
        let message = event.data;
        try {
            message = JSON.parse(event.data);
        } catch (e) {
            // raw string
        }
        console.log('REPL Message:', message);
        listeners.forEach(listener => listener(message));
    };

    ws.onerror = (error) => {
        console.warn('REPL Connection Error:', error);
    };

    ws.onclose = () => {
        console.log('REPL Disconnected');
        socket = null;
    };

    return ws;
}
