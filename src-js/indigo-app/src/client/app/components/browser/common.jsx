import React from 'react'
import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'

export function BrowserPanel({ title, search, onSearchChange, children, loading, error }) {
    if (loading) return <div className="p-4 text-xs text-gray-400">Loading...</div>;
    if (error) return <div className="p-4 text-xs text-red-400">Error: {error}</div>;

    return (
        <div className="flex flex-col h-full bg-[#252525]">
            <div className="h-8 bg-[#252526] border-b border-[#323232] flex items-center px-3 justify-between shrink-0">
                {title && <span className="text-xs font-medium text-gray-300 uppercase tracking-wide">{title}</span>}
                <div className="relative flex-1 ml-2">
                    <Lucide.Search className="absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-gray-500" />
                    <FigmaUi.Input
                        value={search}
                        onChange={(e) => onSearchChange(e.target.value)}
                        placeholder="Search..."
                        className="h-6 pl-7 bg-[#1e1e1e] border-[#323232] text-gray-300 text-xs placeholder:text-gray-600 w-full rounded-sm focus:border-blue-500"
                    />
                </div>
            </div>
            <FigmaUi.ScrollArea className="flex-1">
                <div className="py-2">
                    {children}
                </div>
            </FigmaUi.ScrollArea>
        </div>
    );
}

export function BrowserTree({ nodes, selectedId, onSelect, expandedIds, onToggleExpand, getIcon }) {
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
                            onSelect(node.id);
                        }
                        // Allow selecting folders if needed, but usually we select leaves
                        if (node.isSelectable && onSelect) {
                            onSelect(node.id);
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
