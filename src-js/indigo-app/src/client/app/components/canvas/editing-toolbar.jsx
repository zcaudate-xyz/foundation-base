import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import { MousePointer2, Hand, Plus, Minus } from '../common/icons'

export function EditingToolbar({ onToolChange, currentTool = 'select' }) {
    return (
        <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-2 gap-1">
            <button
                onClick={() => onToolChange && onToolChange('select')}
                className={`h-8 w-8 flex items-center justify-center rounded hover:bg-[#3a3a3a] ${currentTool === 'select' ? 'bg-[#3a3a3a] text-blue-400' : 'text-gray-400'}`}
            >
                <MousePointer2 className="w-4 h-4" />
            </button>
            <button
                onClick={() => onToolChange && onToolChange('hand')}
                className={`h-8 w-8 flex items-center justify-center rounded hover:bg-[#3a3a3a] ${currentTool === 'hand' ? 'bg-[#3a3a3a] text-blue-400' : 'text-gray-400'}`}
            >
                <Hand className="w-4 h-4" />
            </button>
            <div className="w-[1px] h-4 bg-[#3e3e3e] mx-1" />
            <button
                onClick={() => onToolChange && onToolChange('zoom-in')}
                className={`h-8 w-8 flex items-center justify-center rounded hover:bg-[#3a3a3a] ${currentTool === 'zoom-in' ? 'bg-[#3a3a3a] text-blue-400' : 'text-gray-400'}`}
            >
                <Plus className="w-4 h-4" />
            </button>
            <button
                onClick={() => onToolChange && onToolChange('zoom-out')}
                className={`h-8 w-8 flex items-center justify-center rounded hover:bg-[#3a3a3a] ${currentTool === 'zoom-out' ? 'bg-[#3a3a3a] text-blue-400' : 'text-gray-400'}`}
            >
                <Minus className="w-4 h-4" />
            </button>
        </div>
    )
}
