import React from 'react'
import { get, set } from 'idb-keyval'
import * as repl from '@/client/repl-client'

const EventsContext = React.createContext(null);

const STORAGE_KEY = 'indigo-repl-sessions';

export function EventsProvider({ children }) {
    // { [id]: { id, type: 'global'|'namespace', name, messages: [] } }
    const [sessions, setSessions] = React.useState({
        'global-main': { id: 'global-main', type: 'global', name: 'Main', messages: [] }
    });
    const [activeSessionId, setActiveSessionId] = React.useState('global-main');
    const [logs, setLogs] = React.useState({});
    const [loading, setLoading] = React.useState(true);
    const [connectionStatus, setConnectionStatus] = React.useState('disconnected');

    const internalSubscribersRef = React.useRef({});
    const serverSubscribersRef = React.useRef({});
    const requestSessionMap = React.useRef({});

    // Internal Pub/Sub (Legacy + new hooks)
    const subscribe = React.useCallback((event, callback) => {
        if (!internalSubscribersRef.current[event]) {
            internalSubscribersRef.current[event] = new Set();
        }
        internalSubscribersRef.current[event].add(callback);
        return () => {
            internalSubscribersRef.current[event]?.delete(callback);
        };
    }, []);

    const emit = React.useCallback((event, data) => {
        internalSubscribersRef.current[event]?.forEach(cb => {
            try { cb(data); } catch (e) { console.error(`Error in event handler for ${event}:`, e); }
        });
    }, []);

    // Server Event Pub/Sub
    const subscribeToServer = React.useCallback((type, callback) => {
        if (!serverSubscribersRef.current[type]) {
            serverSubscribersRef.current[type] = new Set();
        }
        serverSubscribersRef.current[type].add(callback);
        return () => {
            serverSubscribersRef.current[type]?.delete(callback);
        };
    }, []);

    const dispatchServerEvent = React.useCallback((type, data) => {
        serverSubscribersRef.current[type]?.forEach(cb => {
            try { cb(data); } catch (e) { console.error(`Error in server event handler for ${type}:`, e); }
        });
    }, []);

    // Load from DB on mount
    React.useEffect(() => {
        get(STORAGE_KEY).then((val) => {
            if (val) {
                if (val.sessions) setSessions(val.sessions);
                if (val.activeSessionId) setActiveSessionId(val.activeSessionId);
                if (val.logs) setLogs(val.logs);
            }
            setLoading(false);
        }).catch(err => {
            console.error("Failed to load sessions from DB", err);
            setLoading(false);
        });
    }, []);

    // Save to DB on change
    React.useEffect(() => {
        if (!loading) {
            set(STORAGE_KEY, { sessions, activeSessionId, logs }).catch(err => console.error("Failed to save sessions to DB", err));
        }
    }, [sessions, activeSessionId, logs, loading]);

    // Helpers to manage state
    const addMessage = React.useCallback((sessionId, message) => {
        setSessions(prev => {
            const session = prev[sessionId];
            if (!session) return prev;
            const newMsgs = session.messages.concat([message]);
            const limitedMsgs = newMsgs.length > 200 ? newMsgs.slice(newMsgs.length - 200) : newMsgs;
            return { ...prev, [sessionId]: { ...session, messages: limitedMsgs } };
        });
    }, []);

    const addLog = React.useCallback((namespace, entry) => {
        setLogs(prev => {
            const prevLogs = prev[namespace] || [];
            const newLogs = prevLogs.concat([entry]);
            const limitedLogs = newLogs.length > 200 ? newLogs.slice(newLogs.length - 200) : newLogs;
            return { ...prev, [namespace]: limitedLogs };
        });
    }, []);

    // Active Session Ref for event routing fallback
    const activeSessionIdRef = React.useRef(activeSessionId);
    React.useEffect(() => { activeSessionIdRef.current = activeSessionId; }, [activeSessionId]);
    const sessionsRef = React.useRef(sessions);
    React.useEffect(() => { sessionsRef.current = sessions; }, [sessions]);


    // Unified Connection & Event Logic
    React.useEffect(() => {
        const handleMsg = (msg) => {
            // 1. Dispatch typed events
            if (msg && msg.type) {
                dispatchServerEvent(msg.type, msg);
            }

            // 2. Chat/Session routing (Legacy ReplPanel Logic)
            if (msg && msg.id && typeof msg.id === 'string' && msg.id.startsWith('eval-')) return;

            const targetId = (msg.id && requestSessionMap.current[msg.id]) || activeSessionIdRef.current;
            addMessage(targetId, msg);
        };

        const handleLog = (entry) => {
            let targetNs = 'user';
            const msg = entry.message;
            if (msg && typeof msg === 'object') {
                if (msg.ns) {
                    targetNs = msg.ns;
                } else if (msg.id && requestSessionMap.current[msg.id]) {
                    const sessionId = requestSessionMap.current[msg.id];
                    const session = sessionsRef.current[sessionId];
                    if (session) {
                        targetNs = session.type === 'namespace' ? session.name : 'user';
                    }
                }
            }
            addLog(targetNs, entry);
        };

        const handleStatus = (s) => setConnectionStatus(s);

        const unsubMsg = repl.addMessageListener(handleMsg);
        const unsubLog = repl.addLogListener(handleLog);
        const unsubStatus = repl.addStatusListener(handleStatus);

        // Auto-connect
        if (repl.getStatus() === 'disconnected') {
            repl.connect(window.location.hostname, '1311', { path: 'repl' });
        }

        return () => {
            unsubMsg();
            unsubLog();
            unsubStatus();
        };
    }, [addMessage, addLog, dispatchServerEvent]);


    const createSession = React.useCallback((type, name, id = null) => {
        const newId = id || `${type}-${Date.now()}`;
        setSessions(prev => {
            if (prev[newId]) return prev;
            return { ...prev, [newId]: { id: newId, type, name, messages: [] } };
        });
        return newId;
    }, []);

    const clearSession = React.useCallback((sessionId) => {
        setSessions(prev => {
            if (!prev[sessionId]) return prev;
            return { ...prev, [sessionId]: { ...prev[sessionId], messages: [] } };
        });
    }, []);

    const ensureNamespaceSession = React.useCallback((ns) => {
        if (!ns) return;
        const id = `ns-${ns}`;
        setSessions(prev => {
            if (prev[id]) return prev;
            return { ...prev, [id]: { id, type: 'namespace', name: ns, messages: [] } };
        });
        return id;
    }, []);

    const renameSession = React.useCallback((sessionId, newName) => {
        setSessions(prev => {
            if (!prev[sessionId]) return prev;
            return { ...prev, [sessionId]: { ...prev[sessionId], name: newName } };
        });
    }, []);

    const sendCommand = React.useCallback((code, ns, targetSessionId) => {
        const id = "console-" + Date.now();
        requestSessionMap.current[id] = targetSessionId || activeSessionId;

        repl.send({
            op: "eval",
            id: id,
            code: code,
            ns: ns || 'user'
        });

        // Return ID if caller needs it
        return id;
    }, [activeSessionId]);

    // Low level send
    const sendRaw = React.useCallback((msg) => repl.send(msg), []);

    const connect = React.useCallback((...args) => repl.connect(...args), []);
    const disconnect = React.useCallback(() => repl.disconnect(), []);

    const evalRequest = React.useCallback((code, ns) => {
        return new Promise((resolve, reject) => {
            const id = "eval-req-" + Date.now();
            const removeListener = repl.addMessageListener((msg) => {
                if (msg.id === id) {
                    removeListener();
                    if (msg.error) {
                        reject(new Error(msg.error));
                    } else {
                        resolve(msg.result);
                    }
                }
            });
            repl.send({ op: "eval", id, code, ns: ns || 'user' });
            // Optional: Timeout to clean up listener
            setTimeout(() => {
                removeListener();
                // We don't necessarily reject on timeout as some evals are long, 
                // but we should clean up if socket dies.
            }, 300000); // 5 min
        });
    }, []);

    const value = {
        sessions,
        activeSessionId,
        setActiveSessionId,
        addMessage,
        createSession,
        clearSession,
        ensureNamespaceSession,
        renameSession,
        logs,
        addLog,
        loading,
        connectionStatus,
        emit, // internal
        subscribe, // internal
        subscribeToServer, // API for hooks
        sendCommand,
        sendRaw,
        connect,
        disconnect,
        evalRequest
    };

    return (
        <EventsContext.Provider value={value}>
            {children}
        </EventsContext.Provider>
    );
}

export function useEvents() {
    const context = React.useContext(EventsContext);
    if (!context) {
        throw new Error("useEvents must be used within an EventsProvider");
    }
    return context;
}

export function useServerEvent(type, handler) {
    const { subscribeToServer } = useEvents();
    React.useEffect(() => {
        return subscribeToServer(type, handler);
    }, [type, handler, subscribeToServer]);
}
