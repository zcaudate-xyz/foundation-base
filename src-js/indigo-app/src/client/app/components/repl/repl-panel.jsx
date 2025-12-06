import React from 'react'
import * as repl from '@/client/repl-client'

export function ReplPanel() {
    const [messages, setMessages] = React.useState([]);
    const scrollRef = React.useRef(null);

    React.useEffect(() => {
        const unsubscribe = repl.addMessageListener((msg) => {
            setMessages((prev) => {
                const newMsgs = prev.concat([msg]);
                if (newMsgs.length > 100) {
                    return newMsgs.slice(newMsgs.length - 100);
                }
                return newMsgs;
            });
        });
        return unsubscribe;
    }, []);

    React.useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    return (
        <div className="flex flex-col h-full bg-[#1e1e1e] border-t border-[#323232]">
            {/* Header */}
            <div className="h-8 bg-[#252525] border-b border-[#323232] flex items-center px-3 justify-between">
                <span className="text-xs font-medium text-gray-300">REPL Output</span>
                <button
                    onClick={() => setMessages([])}
                    className="text-[10px] text-gray-500 hover:text-gray-300"
                >
                    Clear
                </button>
            </div>

            {/* Output Area */}
            <div
                className="flex-1 overflow-y-auto p-2 font-mono text-xs"
                ref={scrollRef}
            >
                {messages.map((msg, i) => {
                    if (msg && msg.type === 'test-result') {
                        const { status, name, ns, data } = msg.data;
                        const isSuccess = status === 'success';
                        const color = isSuccess ? 'text-green-400' : 'text-red-400';
                        return (
                            <div key={i} className="mb-1 border-b border-[#323232] pb-1 last:border-0">
                                <div className={`flex items-center gap-2 ${color}`}>
                                    <span className="font-bold">{isSuccess ? 'PASS' : 'FAIL'}</span>
                                    <span className="text-gray-400">{ns}/{name}</span>
                                </div>
                                {!isSuccess && (
                                    <pre className="mt-1 text-gray-500 whitespace-pre-wrap">
                                        {JSON.stringify(data, null, 2)}
                                    </pre>
                                )}
                            </div>
                        );
                    }
                    const content = typeof msg === 'object' ? JSON.stringify(msg, null, 2) : msg;
                    return (
                        <div
                            key={i}
                            className="mb-1 whitespace-pre-wrap break-all text-gray-300 border-b border-[#323232] pb-1 last:border-0"
                        >
                            {content}
                        </div>
                    );
                })}
            </div>

            {/* Input Area */}
            <div className="h-8 bg-[#252525] border-t border-[#323232] flex items-center px-2">
                <span className="text-gray-500 mr-2">›</span>
                <input
                    type="text"
                    className="flex-1 bg-transparent border-none outline-none text-xs text-gray-300 font-mono"
                    placeholder="Type command..."
                    onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                            const cmd = e.target.value;
                            if (cmd.trim()) {
                                repl.send(cmd);
                                setMessages(prev => [...prev, `> ${cmd}`]);
                                e.target.value = '';
                            }
                        }
                    }}
                />
            </div>
        </div>
    );
}
