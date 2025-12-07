import React from 'react'
import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'
import { MenuContainer } from '../common/common-menu.jsx'

export function BrowserPanel({ title, search, onSearchChange, children, loading, error }) {
    if (loading) return <div className="p-4 text-xs text-gray-400">Loading...</div>;
    if (error) return <div className="p-4 text-xs text-red-400">Error: {error}</div>;

    return (
        <MenuContainer>
            <div className="h-8 bg-[#252525] border-b border-[#323232] flex items-center shrink-0">
                {title && <span className="text-xs font-medium text-gray-300 uppercase tracking-wide">{title}</span>}
                <div className="relative flex-1">
                    <Lucide.Search className="absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-gray-500" />
                    <FigmaUi.Input
                        value={search}
                        onChange={(e) => onSearchChange(e.target.value)}
                        placeholder="Search..."
                        className="h-8 pl-7 bg-transparent border-none text-gray-300 text-xs placeholder:text-gray-600 w-full rounded-none focus:ring-0 outline-none"
                    />
                </div>
            </div>
            <div className="flex-1 overflow-y-auto">
                <div className="py-2">
                    {children}
                </div>
            </div>
        </MenuContainer>
    );
}

export function ContextMenu({ x, y, items, onClose }) {
    React.useEffect(() => {
        const handleClick = () => onClose();
        window.addEventListener('click', handleClick);
        return () => window.removeEventListener('click', handleClick);
    }, [onClose]);

    return (
        <div
            className="fixed z-50 bg-[#252526] border border-[#323232] shadow-lg rounded-md py-1 min-w-[160px]"
            style={{ top: y, left: x }}
            onClick={(e) => e.stopPropagation()}
        >
            {items.map((item, index) => (
                <div
                    key={index}
                    className="px-3 py-1.5 text-xs text-gray-300 hover:bg-[#094771] hover:text-white cursor-pointer flex items-center gap-2"
                    onClick={() => {
                        item.action();
                        onClose();
                    }}
                >
                    {item.icon && <item.icon size={12} />}
                    {item.label}
                </div>
            ))}
        </div>
    );
}

export function BrowserTree({ nodes, selectedId, onSelect, onDoubleClick, expandedIds, onToggleExpand, getIcon, onNodeContextMenu }) {
    if (!nodes || nodes.length === 0) {
        return <div className="p-4 text-xs text-gray-500 text-center">No results found</div>;
    }

    const renderNode = (node, depth = 0) => {
        const isExpanded = expandedIds.has(node.id);
        const hasChildren = node.children && node.children.length > 0;
        const isSelected = selectedId === node.id;
        const paddingLeft = `${depth * 12 + 8}px`;

        return (
            <div key={node.id}>
                <div
                    className={`flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer text-xs group ${isSelected ? "bg-[#37373d] text-white" : "text-gray-300"}`}
                    style={{ paddingLeft }}
                    onClick={(e) => {
                        e.stopPropagation();
                        if (hasChildren) {
                            onToggleExpand(node.id);
                        } else {
                            if (onSelect) {
                                onSelect(node.id);
                            }
                        }
                    }}
                    onDoubleClick={(e) => {
                        e.stopPropagation();
                        if (!hasChildren && onDoubleClick) {
                            onDoubleClick(node.id);
                        }
                    }}
                    onContextMenu={(e) => {
                        if (onNodeContextMenu) {
                            e.preventDefault();
                            e.stopPropagation();
                            onNodeContextMenu(e, node);
                        }
                    }}
                >
                    <span className="text-gray-500 group-hover:text-gray-300 flex-shrink-0">
                        {hasChildren ? (
                            isExpanded ? <Lucide.ChevronDown size={12} /> : <Lucide.ChevronRight size={12} />
                        ) : <div className="w-3" />}
                    </span>

                    <span className="flex-shrink-0">
                        {getIcon ? getIcon(node, isExpanded, isSelected) : (
                            hasChildren ? <Lucide.Folder size={12} className="text-gray-500" /> : <Lucide.File size={12} className="text-gray-400" />
                        )}
                    </span>

                    <span className={`truncate ${isSelected ? "font-medium" : ""}`}>{node.label}</span>
                </div>
                {isExpanded && hasChildren && (
                    <div>
                        {node.children.map(child => renderNode(child, depth + 1))}
                    </div>
                )}
            </div>
        );
    };

    return (
        <div>
            {nodes.map(node => renderNode(node))}
        </div>
    );
}
