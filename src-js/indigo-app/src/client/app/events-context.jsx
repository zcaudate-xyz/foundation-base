import React from 'react'
import { get, set } from 'idb-keyval'

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

    const subscribersRef = React.useRef({});

    const subscribe = React.useCallback((event, callback) => {
        if (!subscribersRef.current[event]) {
            subscribersRef.current[event] = new Set();
        }
        subscribersRef.current[event].add(callback);
        return () => {
            if (subscribersRef.current[event]) {
                subscribersRef.current[event].delete(callback);
            }
        };
    }, []);

    const emit = React.useCallback((event, data) => {
        if (subscribersRef.current[event]) {
            subscribersRef.current[event].forEach(cb => {
                try {
                    cb(data);
                } catch (e) {
                    console.error(`Error in event handler for ${event}:`, e);
                }
            });
        }
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

    // Save to DB on change (debounced or immediate? immediate for now, maybe optimize later)
    React.useEffect(() => {
        if (!loading) {
            set(STORAGE_KEY, { sessions, activeSessionId, logs }).catch(err => console.error("Failed to save sessions to DB", err));
        }
    }, [sessions, activeSessionId, logs, loading]);

    const addMessage = React.useCallback((sessionId, message) => {
        setSessions(prev => {
            const session = prev[sessionId];
            if (!session) return prev;

            const newMsgs = session.messages.concat([message]);
            // Limit history per session to prevent DB bloat
            const limitedMsgs = newMsgs.length > 200 ? newMsgs.slice(newMsgs.length - 200) : newMsgs;

            return {
                ...prev,
                [sessionId]: {
                    ...session,
                    messages: limitedMsgs
                }
            };
        });
    }, []);

    const createSession = React.useCallback((type, name, id = null) => {
        const newId = id || `${type}-${Date.now()}`;
        setSessions(prev => {
            if (prev[newId]) return prev;
            return {
                ...prev,
                [newId]: { id: newId, type, name, messages: [] }
            };
        });
        return newId;
    }, []);

    const clearSession = React.useCallback((sessionId) => {
        setSessions(prev => {
            if (!prev[sessionId]) return prev;
            return {
                ...prev,
                [sessionId]: { ...prev[sessionId], messages: [] }
            };
        });
    }, []);

    const ensureNamespaceSession = React.useCallback((ns) => {
        if (!ns) return;
        const id = `ns-${ns}`;
        setSessions(prev => {
            if (prev[id]) return prev;
            return {
                ...prev,
                [id]: { id, type: 'namespace', name: ns, messages: [] }
            };
        });
        return id;
    }, []);

    const renameSession = React.useCallback((sessionId, newName) => {
        setSessions(prev => {
            if (!prev[sessionId]) return prev;
            return {
                ...prev,
                [sessionId]: { ...prev[sessionId], name: newName }
            };
        });
    }, []);

    const addLog = React.useCallback((namespace, entry) => {
        setLogs(prev => {
            const prevLogs = prev[namespace] || [];
            const newLogs = prevLogs.concat([entry]);
            const limitedLogs = newLogs.length > 200 ? newLogs.slice(newLogs.length - 200) : newLogs;
            return {
                ...prev,
                [namespace]: limitedLogs
            };
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
        renameSession,
        logs,
        addLog,
        loading,
        emit,
        subscribe
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
