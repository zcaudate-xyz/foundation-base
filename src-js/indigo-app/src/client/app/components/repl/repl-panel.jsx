import React from 'react'
import { useAppState } from '../../state'
import { useEvents } from '../../events-context'
import * as Lucide from 'lucide-react'

export function ReplPanel() {
    const { selectedNamespace } = useAppState();
    const {
        sessions,
        activeSessionId,
        setActiveSessionId,
        addMessage,
        createSession,
        clearSession,
        ensureNamespaceSession,
        renameSession,
        logs: logsMap,
        loading: sessionsLoading,
        connectionStatus: status, // mapped to status for convenience
        connect,
        disconnect,
        sendCommand
    } = useEvents();

    const [activeTab, setActiveTab] = React.useState('console'); // 'console' | 'events'
    const [isRenaming, setIsRenaming] = React.useState(false);
    const [renameValue, setRenameValue] = React.useState("");
    const [showConnectionMenu, setShowConnectionMenu] = React.useState(false);
    const [showSessionMenu, setShowSessionMenu] = React.useState(false);

    const scrollRef = React.useRef(null);
    const logScrollRef = React.useRef(null);
    const menuRef = React.useRef(null);
    const sessionMenuRef = React.useRef(null);

    // Auto-switch to namespace session when selectedNamespace changes
    React.useEffect(() => {
        if (selectedNamespace) {
            const nsSessionId = ensureNamespaceSession(selectedNamespace);
            if (nsSessionId) {
                setActiveSessionId(nsSessionId);
            }
        }
    }, [selectedNamespace, ensureNamespaceSession, setActiveSessionId]);

    // Close menu when clicking outside
    React.useEffect(() => {
        function handleClickOutside(event) {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setShowConnectionMenu(false);
            }
            if (sessionMenuRef.current && !sessionMenuRef.current.contains(event.target)) {
                setShowSessionMenu(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    const toggleConnection = () => {
        if (status === 'connected' || status === 'connecting') {
            disconnect();
        } else {
            connect(window.location.hostname, '1311', { path: 'repl' });
        }
        setShowConnectionMenu(false);
    };

    const activeSession = sessions[activeSessionId];

    React.useEffect(() => {
        if (scrollRef.current && activeTab === 'console') {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [activeSession?.messages, activeTab, activeSessionId]);

    React.useEffect(() => {
        if (logScrollRef.current && activeTab === 'events') {
            logScrollRef.current.scrollTop = logScrollRef.current.scrollHeight;
        }
    }, [logsMap, activeTab, activeSessionId]);

    const handleCreateGlobal = () => {
        const id = createSession('global', `Global ${Object.values(sessions).filter(s => s.type === 'global').length + 1}`);
        setActiveSessionId(id);
    };

    const handleSwitchToGlobal = () => {
        const globalSession = Object.values(sessions).find(s => s.type === 'global');
        if (globalSession) {
            setActiveSessionId(globalSession.id);
        } else {
            handleCreateGlobal();
        }
    };

    const handleRenameStart = () => {
        if (activeSession?.type === 'global') {
            setRenameValue(activeSession.name);
            setIsRenaming(true);
        }
    };

    const handleRenameSubmit = () => {
        if (renameValue.trim()) {
            renameSession(activeSessionId, renameValue.trim());
        }
        setIsRenaming(false);
    };

    const getStatusColor = () => {
        if (status === 'connected') return 'bg-green-500';
        if (status === 'connecting') return 'bg-yellow-500';
        return 'bg-red-500';
    };

    if (sessionsLoading) return <div className="p-2 text-xs text-muted-foreground">Loading sessions...</div>;

    return (
        <div className="flex flex-col h-full bg-background border-t border-border">
            {/* Header */}
            <div className="h-8 bg-muted/30 border-b border-border flex items-center px-3 justify-between">
                <div className="flex items-center gap-4">
                    {/* Connection Status Menu */}
                    <div className="relative" ref={menuRef}>
                        <button
                            onClick={() => setShowConnectionMenu(!showConnectionMenu)}
                            className="flex items-center gap-2 hover:bg-muted px-1.5 py-1 rounded transition-colors"
                            title="Connection Status"
                        >
                            <div className={`w-2 h-2 rounded-full ${getStatusColor()}`}></div>
                        </button>

                        {showConnectionMenu && (
                            <div className="absolute top-full left-0 mt-1 w-32 bg-background border border-border rounded shadow-lg z-50 overflow-hidden">
                                <div className="px-3 py-2 border-b border-border text-[10px] text-muted-foreground uppercase font-bold">
                                    {status}
                                </div>
                                <button
                                    onClick={toggleConnection}
                                    className="w-full text-left px-3 py-2 text-[10px] text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
                                >
                                    {status === 'connected' ? 'Disconnect' : 'Connect'}
                                </button>

                                <div className="border-t border-border my-1"></div>

                                <button
                                    onClick={() => {
                                        clearSession(activeSessionId);
                                        setShowConnectionMenu(false);
                                    }}
                                    className="w-full text-left px-3 py-2 text-[10px] text-muted-foreground hover:bg-muted hover:text-foreground transition-colors flex items-center gap-2"
                                >
                                    <Lucide.Trash2 size={10} />
                                    Clear Console
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Tabs */}
                    <div className="flex gap-1">
                        <button
                            onClick={() => setActiveTab('console')}
                            className={`px-2 py-0.5 text-[10px] rounded transition-colors ${activeTab === 'console' ? 'bg-muted text-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'}`}
                        >
                            Console
                        </button>
                        <button
                            onClick={() => setActiveTab('events')}
                            className={`px-2 py-0.5 text-[10px] rounded transition-colors ${activeTab === 'events' ? 'bg-muted text-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'}`}
                        >
                            Events
                        </button>
                    </div>
                </div>

                {/* Session Selector (Right Aligned) */}
                {activeTab === 'console' && (
                    <div className="flex items-center gap-1">
                        <div className="relative" ref={sessionMenuRef}>
                            {isRenaming && activeSession?.type === 'global' ? (
                                <input
                                    type="text"
                                    value={renameValue}
                                    onChange={(e) => setRenameValue(e.target.value)}
                                    onBlur={handleRenameSubmit}
                                    onKeyDown={(e) => e.key === 'Enter' && handleRenameSubmit()}
                                    className="bg-background text-foreground text-[10px] border border-blue-500 rounded px-1 py-0.5 outline-none w-[100px]"
                                    autoFocus
                                />
                            ) : (
                                <button
                                    onClick={() => setShowSessionMenu(!showSessionMenu)}
                                    className="flex items-center gap-1 px-2 py-0.5 text-[10px] text-muted-foreground hover:text-foreground hover:bg-muted rounded border border-transparent hover:border-border transition-colors"
                                >
                                    {activeSessionId === `console-${selectedNamespace}` ? (
                                        <>
                                            <Lucide.FileCode size={10} />
                                        </>
                                    ) : (
                                        <>
                                            <Lucide.Globe size={10} />
                                        </>
                                    )}
                                    <span className="truncate max-w-[100px]">{activeSession ? (activeSession.type === 'namespace' ? selectedNamespace : activeSession.name) : 'Select Session'}</span>
                                    <Lucide.ChevronDown size={10} />
                                </button>
                            )}


                            {showSessionMenu && (
                                <div className="absolute top-full right-0 mt-1 w-48 bg-background border border-border rounded shadow-lg z-50 overflow-hidden">
                                    {/* Namespace Session */}
                                    <div className="px-2 py-1.5 text-[10px] text-muted-foreground font-bold uppercase border-b border-border">
                                        Namespace
                                    </div>
                                    <button
                                        onClick={() => {
                                            setActiveSessionId(`console-${selectedNamespace}`);
                                            setShowSessionMenu(false);
                                        }}
                                        className={`w-full text-left px-3 py-1.5 text-[10px] flex items-center gap-2 hover:bg-muted transition-colors ${activeSessionId === `console-${selectedNamespace}` ? 'text-foreground bg-muted' : 'text-muted-foreground'}`}
                                    >
                                        <Lucide.FileCode size={10} />
                                        <span className="truncate font-mono">{selectedNamespace}</span>
                                    </button>

                                    {/* Global Sessions */}
                                    <div className="px-2 py-1.5 text-[10px] text-muted-foreground font-bold uppercase border-b border-border border-t mt-1">
                                        Global Consoles
                                    </div>
                                    {Object.entries(sessions).filter(([id]) => id.startsWith('global-')).map(([id, session]) => (
                                        <div key={id} className="flex items-center group hover:bg-muted">
                                            <button
                                                onClick={() => {
                                                    setActiveSessionId(id);
                                                    setShowSessionMenu(false);
                                                }}
                                                className={`flex-1 text-left px-3 py-1.5 text-[10px] flex items-center gap-2 transition-colors ${activeSessionId === id ? 'text-foreground' : 'text-muted-foreground'}`}
                                            >
                                                <Lucide.Globe size={10} />
                                                <span className="truncate">{session.name}</span>
                                            </button>
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setActiveSessionId(id);
                                                    setTimeout(() => handleRenameStart(), 0);
                                                }}
                                                className="p-1.5 text-muted-foreground hover:text-foreground opacity-0 group-hover:opacity-100 transition-opacity"
                                                title="Rename"
                                            >
                                                <Lucide.Edit2 size={8} />
                                            </button>
                                        </div>
                                    ))}

                                    {/* Add Global Session */}
                                    <button
                                        onClick={() => {
                                            handleCreateGlobal(); // Used to be createNewGlobalSession? No, handleCreateGlobal (line 138)
                                            setShowSessionMenu(false);
                                        }}
                                        className="w-full text-left px-3 py-2 text-[10px] text-blue-400 hover:bg-muted hover:text-blue-300 transition-colors border-t border-border flex items-center gap-2"
                                    >
                                        <Lucide.Plus size={10} />
                                        <span>New Global Console</span>
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {/* Output Area */}
            {activeTab === 'console' ? (
                <>
                    <div
                        className="flex-1 overflow-y-auto p-2 font-mono text-xs text-foreground"
                        ref={scrollRef}
                    >
                        {activeSession && activeSession.messages.map((msg, i) => {
                            // Handle structured evaluation results
                            if (msg && msg.result) {
                                const { result, type } = msg;
                                const isError = type === 'exception' || type === 'error';
                                const color = isError ? 'text-red-400' : 'text-green-400';

                                return (
                                    <div key={i} className="mb-1 border-b border-border pb-1 last:border-0 group relative">
                                        <div className={`whitespace-pre-wrap break-all ${color} pr-6`}>
                                            {result}
                                        </div>
                                        <button
                                            onClick={() => navigator.clipboard.writeText(result)}
                                            className="absolute top-0 right-0 p-1 text-muted-foreground hover:text-foreground opacity-0 group-hover:opacity-100 transition-opacity"
                                            title="Copy result"
                                        >
                                            <Lucide.Copy size={10} />
                                        </button>
                                    </div>
                                );
                            }

                            // Handle test results
                            if (msg && msg.type === 'test-result') {
                                const { status, name, ns, data } = msg.data;
                                const isSuccess = status === 'success';
                                const color = isSuccess ? 'text-green-400' : 'text-red-400';
                                return (
                                    <div key={i} className="mb-1 border-b border-border pb-1 last:border-0">
                                        <div className={`flex items-center gap-2 ${color}`}>
                                            <span className="font-bold">{isSuccess ? 'PASS' : 'FAIL'}</span>
                                            <span className="text-muted-foreground">
                                                {name && name.includes('/') ? name : `${ns}/${name}`}
                                            </span>
                                        </div>
                                        {!isSuccess && (
                                            <pre className="mt-1 text-muted-foreground whitespace-pre-wrap">
                                                {JSON.stringify(data, null, 2)}
                                            </pre>
                                        )}
                                    </div>
                                );
                            }

                            // Handle raw strings (e.g. user input echo)
                            if (typeof msg === 'string') {
                                return (
                                    <div
                                        key={i}
                                        className="mb-1 whitespace-pre-wrap break-all text-muted-foreground border-b border-border pb-1 last:border-0"
                                    >
                                        {msg}
                                    </div>
                                );
                            }

                            // Fallback for other objects
                            const content = JSON.stringify(msg, null, 2);
                            return (
                                <div
                                    key={i}
                                    className="mb-1 whitespace-pre-wrap break-all text-muted-foreground border-b border-border pb-1 last:border-0"
                                >
                                    {content}
                                </div>
                            );
                        })}
                    </div>

                    {/* Input Area */}
                    <div className="h-8 bg-muted/30 border-t border-border flex items-center px-2">
                        <span className="text-muted-foreground mr-2">›</span>
                        <input
                            type="text"
                            className="flex-1 bg-transparent border-none outline-none text-xs text-foreground font-mono placeholder:text-muted-foreground"
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    const cmd = e.target.value;
                                    if (cmd.trim()) {
                                        // Use sendCommand from context
                                        const ns = activeSession.type === 'namespace' ? activeSession.name : 'user';
                                        sendCommand(cmd, ns, activeSessionId);

                                        // Add to history (sendCommand sends it, but echo is handled by addMessage in context listener? 
                                        // Wait, ReplPanel original code added echo manually: `addMessage(activeSessionId, '> ' + cmd)`.
                                        // EventsContext handles msg.id filtering, but echo is local.
                                        addMessage(activeSessionId, `> ${cmd}`);

                                        e.target.value = '';
                                    }
                                }
                            }}
                        />
                    </div>
                </>
            ) : (
                <div
                    className="flex-1 overflow-y-auto p-2 font-mono text-xs text-foreground"
                    ref={logScrollRef}
                >
                    {(logsMap[activeSessionId === `console-${selectedNamespace}` ? selectedNamespace : 'user'] || []).map((entry, i) => {
                        const isOut = entry.direction === 'out';
                        const time = new Date(entry.timestamp).toLocaleTimeString();
                        const content = typeof entry.message === 'object' ? JSON.stringify(entry.message, null, 2) : entry.message;

                        return (
                            <div key={i} className="mb-2 border-b border-border pb-2 last:border-0">
                                <div className="flex items-center gap-2 mb-1 opacity-50">
                                    <span className={`text-[10px] uppercase font-bold ${isOut ? 'text-blue-400' : 'text-purple-400'}`}>
                                        {isOut ? 'OUT' : 'IN'}
                                    </span>
                                    <span className="text-[10px] text-muted-foreground">{time}</span>
                                </div>
                                <div className="text-muted-foreground whitespace-pre-wrap break-all pl-4 border-l-2 border-border">
                                    {content}
                                </div>
                            </div>
                        );
                    })}
                    {(!logsMap[activeSessionId === `console-${selectedNamespace}` ? selectedNamespace : 'user'] || logsMap[activeSessionId === `console-${selectedNamespace}` ? selectedNamespace : 'user'].length === 0) && (
                        <div className="text-muted-foreground italic text-center mt-4">No events for this session.</div>
                    )}
                </div>
            )
            }
        </div >
    );
}
