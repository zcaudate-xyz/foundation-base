import React from 'react'
import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'
import { MenuContainer } from '../common/common-menu.jsx'

export function BrowserPanel({ title, search, onSearchChange, children, loading, error }) {
    if (loading) return <div className="p-4 text-xs text-muted-foreground">Loading...</div>;
    if (error) return <div className="p-4 text-xs text-red-400">Error: {error}</div>;

    return (
        <MenuContainer>
            <div className="h-8 bg-muted/30 border-b border-border flex items-center shrink-0">
                {title && <span className="text-xs font-semibold text-foreground/80 uppercase tracking-wide pl-2">{title}</span>}
                <div className="relative flex-1">
                    <Lucide.Search className="absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-muted-foreground" />
                    <FigmaUi.Input
                        value={search}
                        onChange={(e) => onSearchChange(e.target.value)}
                        placeholder="Search..."
                        className="h-8 pl-7 bg-transparent border-none text-foreground text-xs placeholder:text-muted-foreground w-full rounded-none focus:ring-0 outline-none"
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
    const menuRef = React.useRef(null);

    React.useEffect(() => {
        const handleClick = (e) => {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                onClose();
            }
        };
        // Use capture phase to handle cases where other components stop propagation
        window.addEventListener('mousedown', handleClick, true);
        return () => window.removeEventListener('mousedown', handleClick, true);
    }, [onClose]);

    return (
        <div
            ref={menuRef}
            className="fixed z-50 bg-background border border-border shadow-lg rounded-md py-1 min-w-[160px]"
            style={{ top: y, left: x }}
            onClick={(e) => e.stopPropagation()}
        >
            {items.map((item, index) => (
                <div
                    key={index}
                    className="px-3 py-1.5 text-xs text-muted-foreground hover:bg-primary hover:text-primary-foreground cursor-pointer flex items-center gap-2 transition-colors"
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
        return <div className="p-4 text-xs text-muted-foreground text-center">No results found</div>;
    }

    const renderNode = (node, depth = 0) => {
        const isExpanded = expandedIds.has(node.id);
        const hasChildren = node.children && node.children.length > 0;
        const isSelected = selectedId === node.id;
        const paddingLeft = `${depth * 12 + 8}px`;

        return (
            <div key={node.id}>
                <div
                    className={`flex items-center gap-1 py-1 px-2 hover:bg-muted/50 cursor-pointer text-xs group select-none transition-colors ${isSelected ? "bg-accent text-accent-foreground font-medium" : "text-foreground/80"}`}
                    style={{ paddingLeft }}
                    onClick={(e) => {
                        e.stopPropagation();
                        // If it's a selectable node (file or package), select it.
                        // If it's a pure folder (not selectable), toggle expand.
                        if (node.isSelectable && onSelect) {
                            onSelect(node.id);
                        } else if (hasChildren) {
                            onToggleExpand(node.id);
                        }
                    }}
                    onDoubleClick={(e) => {
                        e.stopPropagation();
                        // Double click always toggles expand for folders/packages, 
                        // and might open editor for files.
                        // Ideally: Packages -> Toggle Expand. Files -> Open.
                        if (hasChildren) {
                            onToggleExpand(node.id);
                        } else if (onDoubleClick) {
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
                    <span
                        className="text-foreground/70 group-hover:text-foreground flex-shrink-0 cursor-pointer transition-colors"
                        onClick={(e) => {
                            e.stopPropagation();
                            if (hasChildren) onToggleExpand(node.id);
                        }}
                    >
                        {hasChildren ? (
                            isExpanded ? <Lucide.ChevronDown size={12} /> : <Lucide.ChevronRight size={12} />
                        ) : <div className="w-3" />}
                    </span>

                    <span className="flex-shrink-0">
                        {getIcon ? getIcon(node, isExpanded, isSelected) : (
                            hasChildren ? <Lucide.Folder size={12} className="text-foreground/70" /> : <Lucide.File size={12} className="text-foreground/70" />
                        )}
                    </span>

                    <span className="truncate">{node.label}</span>
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
