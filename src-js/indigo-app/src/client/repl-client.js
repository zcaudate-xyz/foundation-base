const listeners = new Set();
const logListeners = new Set();
const statusListeners = new Set();
let socket = null;
let status = 'disconnected'; // disconnected, connecting, connected

export function addMessageListener(callback) {
    listeners.add(callback);
    return () => listeners.delete(callback);
}

export function addLogListener(callback) {
    logListeners.add(callback);
    return () => logListeners.delete(callback);
}

function broadcastLog(direction, message) {
    const entry = {
        timestamp: Date.now(),
        direction,
        message
    };
    logListeners.forEach(l => l(entry));
}

export function addStatusListener(callback) {
    statusListeners.add(callback);
    callback(status); // Immediate update
    return () => statusListeners.delete(callback);
}

function setStatus(newStatus) {
    status = newStatus;
    statusListeners.forEach(l => l(status));
}

export function getStatus() {
    return status;
}

export function send(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        const payload = typeof message === 'string' ? message : JSON.stringify(message);
        socket.send(payload);
        broadcastLog('out', typeof message === 'string' ? message : message);
    } else {
        console.warn('REPL not connected');
    }
}

export function disconnect() {
    if (socket) {
        socket.close();
        socket = null;
        setStatus('disconnected');
    }
}

export function connect(host, port, options = {}) {
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
        console.log("REPL already connecting/connected");
        return socket;
    }

    const protocol = options.secured ? 'wss' : 'ws';
    const url = `${protocol}://${host}:${port}/${options.path || ''}`;

    console.log(`Connecting to REPL at ${url}...`);
    setStatus('connecting');

    const ws = new WebSocket(url);
    socket = ws;

    ws.onopen = () => {
        console.log('REPL Connected');
        setStatus('connected');
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
        broadcastLog('in', message);
        listeners.forEach(listener => listener(message));
    };

    ws.onerror = (error) => {
        console.warn('REPL Connection Error:', error);
        // Status update might be handled in onclose, but good to know
    };

    ws.onclose = () => {
        console.log('REPL Disconnected');
        socket = null;
        setStatus('disconnected');
    };

    return ws;
}
