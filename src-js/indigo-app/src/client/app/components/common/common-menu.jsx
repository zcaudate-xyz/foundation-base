import React from 'react';


export function MenuContainer({ children, className }) {
    return (
        <div className={`flex flex-col h-full bg-background ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuHeader({ children, className }) {
    return (
        <div className={`px-3 py-2 border-b border-border text-xs text-muted-foreground uppercase tracking-wide ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuSection({ children, className }) {
    return (
        <div className={`px-2 py-1 bg-muted/30 border-b border-border text-xs text-muted-foreground ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuItem({ children, onClick, active, className }) {
    return (
        <div
            onClick={onClick}
            className={`flex items-center gap-2 px-2 py-1 text-xs cursor-pointer transition-colors ${active ? 'bg-muted text-foreground' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                } ${className || ''}`}
        >
            {children}
        </div>
    );
}

export function MenuToolbar({ children, className }) {
    return (
        <div className={`flex items-center gap-1 p-1 border-b border-border bg-background ${className || ''}`}>
            {children}
        </div>
    );
}

import { Tooltip } from './common-tooltip';

export function MenuButton({ children, onClick, disabled, className, icon: Icon, title, active }) {
    const button = (
        <button
            onClick={onClick}
            disabled={disabled}
            className={`flex items-center justify-center p-1 rounded hover:bg-accent hover:text-accent-foreground hover:scale-110 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 ${active ? 'bg-accent text-accent-foreground' : 'text-muted-foreground'} ${className || ''}`}
        >
            {Icon ? <Icon size={14} /> : children}
        </button>
    );

    if (title) {
        return (
            <Tooltip content={title} side="right">
                {button}
            </Tooltip>
        );
    }

    return button;
}
