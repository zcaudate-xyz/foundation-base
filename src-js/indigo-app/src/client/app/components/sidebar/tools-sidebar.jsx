import React from 'react';
import { useAppState } from '../../state';
import { Icon } from '../../../ui-common';

export function ToolsSidebar() {
    const { setActiveModal, activeModal } = useAppState();

    const tools = [
        { id: 'heal-code', label: 'Heal Code', icon: 'Bandage' },
        { id: 'translate-html', label: 'Translate HTML', icon: 'Code' }
    ];

    return (
        <div className="w-12 border-l bg-muted/30 flex flex-col items-center py-4 gap-4 z-10 shrink-0">
            {tools.map(tool => (
                <div
                    key={tool.id}
                    className={`p-2 rounded cursor-pointer transition-colors ${activeModal === tool.id ? 'bg-primary/20 text-primary' : 'text-muted-foreground hover:text-foreground'}`}
                    onClick={() => setActiveModal(tool.id)}
                    title={tool.label}
                >
                    <Icon name={tool.icon} className="w-5 h-5" />
                </div>
            ))}
        </div>
    );
}
