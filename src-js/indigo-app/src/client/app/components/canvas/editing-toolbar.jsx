import React from 'react'
import { MousePointer2, Hand, Plus, Minus } from '../common/icons'
import * as Lucide from 'lucide-react'
import { MenuToolbar, MenuButton } from '../common/common-menu.jsx'
import { useAppState } from '../../state'

export function EditingToolbar({ onToolChange, currentTool = 'select' }) {
    const { history, historyIndex, undo, redo } = useAppState();

    const canUndo = historyIndex > 0;
    const canRedo = historyIndex < history.length - 1;
    const hasHistory = history.length > 0;

    return (
        <MenuToolbar className="gap-1 px-2 h-10">
            {/* Tools */}
            <MenuButton
                onClick={() => onToolChange && onToolChange('select')}
                active={currentTool === 'select'}
                icon={MousePointer2}
                title="Select"
            />
            <MenuButton
                onClick={() => onToolChange && onToolChange('hand')}
                active={currentTool === 'hand'}
                icon={Hand}
                title="Hand Tool"
            />

            <div className="w-[1px] h-4 bg-border mx-1" />

            {/* Zoom Controls */}
            <MenuButton
                onClick={() => onToolChange && onToolChange('zoom-in')}
                active={currentTool === 'zoom-in'}
                icon={Plus}
                title="Zoom In"
            />
            <MenuButton
                onClick={() => onToolChange && onToolChange('zoom-out')}
                active={currentTool === 'zoom-out'}
                icon={Minus}
                title="Zoom Out"
            />

            <div className="w-[1px] h-4 bg-border mx-1" />

            {/* History Controls */}
            <MenuButton
                onClick={undo}
                disabled={!canUndo}
                icon={Lucide.ChevronLeft}
                title="Undo"
            />

            <div className="flex items-center justify-center px-2 text-[10px] text-muted-foreground min-w-[60px] select-none">
                {hasHistory ? `${historyIndex + 1} / ${history.length}` : "No History"}
            </div>

            <MenuButton
                onClick={redo}
                disabled={!canRedo}
                icon={Lucide.ChevronRight}
                title="Redo"
            />
        </MenuToolbar>
    )
}
