import React from 'react';
import { HealCodeTask } from './heal-code-task';
import { TranslateHtmlTask } from './translate-html-task';
import { useAppState } from '../../state';
import { Icon } from '../../../ui-common';
import * as Lucide from 'lucide-react';

export function TaskMenu() {
    const { setActiveModal } = useAppState();
    const [open, setOpen] = React.useState(false);

    const tasks = [
        { id: 'heal-code', label: 'Heal Code', icon: 'Bandage' },
        { id: 'translate-html', label: 'Translate HTML', icon: 'Code' }
    ];

    return (
        <div className="absolute bottom-4 right-4 z-50 flex flex-col items-end gap-2">
            {open && (
                <div className="bg-popover bg-white dark:bg-zinc-900 border text-popover-foreground rounded-lg shadow-lg p-1 mb-2 min-w-[160px]">
                    {tasks.map(t => (
                        <button
                            key={t.id}
                            className="flex items-center gap-2 w-full p-2 hover:bg-accent hover:bg-gray-100 dark:hover:bg-zinc-800 rounded text-sm text-left transition-colors"
                            onClick={() => {
                                setActiveModal(t.id);
                                setOpen(false);
                            }}
                        >
                            <Icon name={t.icon} className="w-4 h-4" />
                            <span>{t.label}</span>
                        </button>
                    ))}
                </div>
            )}
            <button
                className="w-12 h-12 bg-blue-600 text-white rounded-full shadow-lg flex items-center justify-center hover:bg-blue-700 transition-transform hover:scale-105 active:scale-95"
                onClick={() => setOpen(!open)}
                title="Tasks"
            >
                {open ? (
                    <Lucide.X className="w-6 h-6" />
                ) : (
                    <Icon name="Zap" className="w-6 h-6" />
                )}
            </button>
        </div>
    );
}

export function TaskModalContent() {
    const { activeModal } = useAppState();
    if (activeModal === 'heal-code') return <HealCodeTask />;
    if (activeModal === 'translate-html') return <TranslateHtmlTask />;
    return null;
}
