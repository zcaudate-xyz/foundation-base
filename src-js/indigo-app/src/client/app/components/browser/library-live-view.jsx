import React from 'react'
import { useAppState } from '../../state'

export function LibraryLiveView() {
    const { selectedNamespace } = useAppState();

    return (
        <div className="flex flex-col h-full bg-[#1e1e1e]">
            <div className="h-8 bg-[#252526] border-b border-[#323232] flex items-center px-3 justify-between shrink-0">
                <span className="text-xs font-medium text-gray-300 uppercase tracking-wide">Live View</span>
                <span className="text-xs text-gray-500">{selectedNamespace}</span>
            </div>
            <div className="flex-1 flex items-center justify-center text-gray-500 text-xs">
                {selectedNamespace ? (
                    <span>Preview for {selectedNamespace} coming soon...</span>
                ) : (
                    <span>Select a namespace to preview</span>
                )}
            </div>
        </div>
    );
}
