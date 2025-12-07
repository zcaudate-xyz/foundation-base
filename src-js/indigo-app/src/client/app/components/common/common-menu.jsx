import React from 'react';


export function MenuContainer({ children, className }) {
    return (
        <div className={`flex flex-col h-full bg-[#252525] ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuHeader({ children, className }) {
    return (
        <div className={`px-3 py-2 border-b border-[#323232] text-xs text-gray-400 uppercase tracking-wide ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuSection({ children, className }) {
    return (
        <div className={`px-2 py-1 bg-[#2b2b2b] border-b border-[#323232] text-xs text-gray-400 ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuItem({ children, onClick, active, className }) {
    return (
        <div
            onClick={onClick}
            className={`flex items-center gap-2 px-2 py-1 text-xs cursor-pointer transition-colors ${active ? 'bg-[#37373d] text-white' : 'text-gray-300 hover:bg-[#323232]'
                } ${className || ''}`}
        >
            {children}
        </div>
    );
}

export function MenuToolbar({ children, className }) {
    return (
        <div className={`flex items-center gap-1 p-1 border-b border-[#323232] bg-[#252525] ${className || ''}`}>
            {children}
        </div>
    );
}

export function MenuButton({ children, onClick, disabled, className, icon: Icon, title, active }) {
    return (
        <button
            onClick={onClick}
            disabled={disabled}
            title={title}
            className={`flex items-center justify-center p-1 rounded hover:bg-[#323232] text-gray-400 hover:text-gray-200 disabled:opacity-50 disabled:cursor-not-allowed ${active ? 'bg-[#323232] text-blue-400' : ''} ${className || ''}`}
        >
            {Icon ? <Icon size={14} /> : children}
        </button>
    );
}
