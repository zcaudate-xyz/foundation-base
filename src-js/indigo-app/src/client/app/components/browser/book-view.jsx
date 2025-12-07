import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'

export function BookView({ toolbar }) {
    const {
        selectedNamespace,
        selectedVar,
        setSelectedVar,
        namespaceEntries: entries,
        namespaceEntriesLoading: loading,
    } = useAppState();

    // Group entries by type
    const groupedEntries = React.useMemo(() => {
        const groups = {
            code: [],
            fragment: [],
            other: []
        };

        entries.forEach(entry => {
            if (entry.type === ':fragment') {
                groups.fragment.push(entry);
            } else if (entry.type === ':code') {
                groups.code.push(entry);
            } else {
                groups.other.push(entry);
            }
        });

        return groups;
    }, [entries]);

    if (!selectedNamespace) {
        return (
            <div className="flex flex-col h-full bg-[#1e1e1e] items-center justify-center text-gray-500 text-xs">
                Select a module
            </div>
        );
    }

    const renderEntry = (entry) => (
        <div
            key={entry.var}
            className={`flex items-center gap-2 px-4 py-1.5 cursor-pointer hover:bg-[#2a2d2e] ${selectedVar === entry.var ? "bg-[#37373d] text-white" : "text-gray-400"}`}
            onClick={() => setSelectedVar(entry.var)}
        >
            <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${entry.type === ':fragment' ? 'bg-green-400' :
                entry.op === 'defn' || entry.type === ':code' ? 'bg-blue-400' :
                    'bg-yellow-400'
                }`} />
            <span className="truncate font-mono text-[11px]" title={entry.var}>{entry.var}</span>
            {entry.op && <span className="text-[10px] text-gray-600 ml-auto">{entry.op}</span>}
        </div>
    );

    return (
        <div className="flex flex-col h-full bg-[#1e1e1e] text-gray-300 text-xs">
            <div className="h-8 bg-[#252526] flex items-center px-4 font-bold border-b border-[#323232] justify-between shrink-0">
                <div className="flex items-center gap-2">
                    <Lucide.Book size={14} className="text-gray-400" />
                    <span>Book View</span>
                </div>
                <span className="text-[10px] text-gray-500 font-mono">{selectedNamespace}</span>
            </div>
            {toolbar && (
                <div className="border-b border-[#323232] bg-[#252526] py-1">
                    {toolbar}
                </div>
            )}

            <div className="flex-1 overflow-y-auto">
                {loading ? (
                    <div className="p-4 text-gray-500 text-center">Loading entries...</div>
                ) : (
                    <div className="flex flex-col py-2">
                        {/* Fragments Section */}
                        {groupedEntries.fragment.length > 0 && (
                            <div className="mb-4">
                                <div className="px-4 py-1 text-[10px] uppercase tracking-wider text-gray-500 font-bold flex items-center gap-2">
                                    <Lucide.Puzzle size={12} />
                                    Fragments
                                </div>
                                {groupedEntries.fragment.map(renderEntry)}
                            </div>
                        )}

                        {/* Code Section */}
                        {groupedEntries.code.length > 0 && (
                            <div className="mb-4">
                                <div className="px-4 py-1 text-[10px] uppercase tracking-wider text-gray-500 font-bold flex items-center gap-2">
                                    <Lucide.Code2 size={12} />
                                    Code
                                </div>
                                {groupedEntries.code.map(renderEntry)}
                            </div>
                        )}

                        {/* Other Section */}
                        {groupedEntries.other.length > 0 && (
                            <div className="mb-4">
                                <div className="px-4 py-1 text-[10px] uppercase tracking-wider text-gray-500 font-bold flex items-center gap-2">
                                    <Lucide.Box size={12} />
                                    Other
                                </div>
                                {groupedEntries.other.map(renderEntry)}
                            </div>
                        )}

                        {entries.length === 0 && (
                            <div className="p-4 text-gray-500 text-center">No entries found.</div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
