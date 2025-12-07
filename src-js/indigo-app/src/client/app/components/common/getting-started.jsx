import React from 'react';
import * as Lucide from 'lucide-react';

export function GettingStarted() {
    return (
        <div className="flex flex-col items-center justify-center h-full bg-[#1e1e1e] text-gray-400 select-none">
            <div className="flex flex-col items-center gap-4 max-w-md text-center">
                <div className="w-24 h-24 bg-[#252526] rounded-full flex items-center justify-center mb-4 shadow-lg border border-[#323232]">
                    <img src="/favicon.svg" alt="Indigo Logo" className="w-12 h-12" />
                </div>

                <h1 className="text-2xl font-bold text-gray-200">Indigo</h1>
                <p className="text-sm text-gray-500">
                    Select a namespace from the browser to start editing.
                </p>

                <div className="grid grid-cols-2 gap-4 mt-8 w-full text-xs">
                    <div className="flex flex-col gap-2 p-4 bg-[#252526] rounded border border-[#323232]">
                        <div className="flex items-center gap-2 text-gray-300 font-medium">
                            <Lucide.MousePointer2 size={14} />
                            <span>Navigation</span>
                        </div>
                        <div className="flex justify-between">
                            <span>Select Node</span>
                            <span className="font-mono bg-[#323232] px-1 rounded">Click</span>
                        </div>
                        <div className="flex justify-between">
                            <span>Open File</span>
                            <span className="font-mono bg-[#323232] px-1 rounded">Double Click</span>
                        </div>
                    </div>

                    <div className="flex flex-col gap-2 p-4 bg-[#252526] rounded border border-[#323232]">
                        <div className="flex items-center gap-2 text-gray-300 font-medium">
                            <Lucide.Keyboard size={14} />
                            <span>Shortcuts</span>
                        </div>
                        <div className="flex justify-between">
                            <span>Eval Form</span>
                            <span className="font-mono bg-[#323232] px-1 rounded">Ctrl+E</span>
                        </div>
                        <div className="flex justify-between">
                            <span>Eval File</span>
                            <span className="font-mono bg-[#323232] px-1 rounded">Ctrl+Shift+E</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
