import * as Lucide from 'lucide-react'
import React from 'react'
import { fetchNamespaces } from '../../../api'
import { useAppState } from '../../state'
import { fuzzyMatch } from '../../utils/search'
import { BrowserPanel, BrowserTree, ContextMenu } from './common'
import { toast } from 'sonner'

// Helper to convert raw namespace tree to standardized nodes
// Helper to convert raw namespace tree to standardized nodes
function convertToStandardNodes(node) {
  const children = Array.from(node.children.values()).map(convertToStandardNodes);
  children.sort((a, b) => a.label.localeCompare(b.label));

  // If this node is a namespace AND has children, it's a "package" (Folder + File)
  if (node.isNamespace && children.length > 0) {
    // Create a separate node for the file itself
    const fileNode = {
      id: node.fullPath,
      label: node.name, // Display name same as folder? Or maybe distinct?
      children: null,
      isNamespace: true,
      isSelectable: true
    };

    // Add file node to children (at the top)
    children.unshift(fileNode);

    // Return the folder node
    return {
      id: node.fullPath + ":folder", // Distinguish ID for the folder
      label: node.name,
      children: children,
      isNamespace: false, // It acts as a folder now
      isSelectable: false // Folders are not selectable for editing
    };
  }

  return {
    id: node.fullPath,
    label: node.name,
    children: children.length > 0 ? children : null,
    isNamespace: node.isNamespace,
    isSelectable: node.isNamespace // Only namespaces are selectable in this view
  };
}

export function buildNamespaceTree(namespaces) {
  let root = {
    "name": "root",
    "fullPath": "",
    "children": new Map()
  };

  const namespaceSet = new Set(namespaces);

  namespaces.sort().forEach(function (ns) {
    let parts = ns.split(".");
    let current = root;
    parts.forEach(function (part, index) {
      if (!current.children.has(part)) {
        current.children.set(part, {
          "name": part,
          "fullPath": parts.slice(0, index + 1).join("."),
          "children": new Map()
        });
      }
      current = current.children.get(part);
    });
  });

  const markNamespaces = (node) => {
    if (namespaceSet.has(node.fullPath)) {
      node.isNamespace = true;
    }
    node.children.forEach(child => markNamespaces(child));
  };

  markNamespaces(root);
  return root;
}

export function EnvBrowser() {
  const {
    envExpandedNodes: expandedNodes,
    setEnvExpandedNodes: setExpandedNodes,
    selectedNamespace,
    setSelectedNamespace,
    openEditorTab,
    treeSelectedId,
    setTreeSelectedId,
    addComponent
  } = useAppState();
  const [namespaces, setNamespaces] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [search, setSearch] = React.useState("");
  const [contextMenu, setContextMenu] = React.useState(null);

  const refreshNamespaces = React.useCallback(() => {
    setLoading(true);
    fetchNamespaces('clj')
      .then(data => {
        setNamespaces(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Failed to fetch namespaces", err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  React.useEffect(() => {
    refreshNamespaces();
  }, [refreshNamespaces]);

  const toggleNode = (path) => {
    setExpandedNodes(prev => {
      const next = new Set(prev);
      if (next.has(path)) {
        next.delete(path);
      } else {
        next.add(path);
      }
      return next;
    });
  };

  const filteredNamespaces = React.useMemo(() => {
    if (!search) return namespaces;
    return namespaces.filter(ns => fuzzyMatch(ns, search));
  }, [namespaces, search]);

  const treeNodes = React.useMemo(() => {
    const rawRoot = buildNamespaceTree(filteredNamespaces);
    // The root itself isn't a node we want to show, we want its children
    return Array.from(rawRoot.children.values()).map(convertToStandardNodes).sort((a, b) => a.label.localeCompare(b.label));
  }, [filteredNamespaces]);

  // Auto-expand if searching
  React.useEffect(() => {
    if (search) {
      const allPaths = new Set();
      const traverse = (node) => {
        if (node.id) allPaths.add(node.id);
        if (node.children) node.children.forEach(traverse);
      };
      treeNodes.forEach(traverse);
      setExpandedNodes(allPaths);
    }
  }, [search, treeNodes]);

  const getIcon = (node, isExpanded, isSelected) => {
    if (node.isNamespace) {
      return <Lucide.Box className={`w-3 h-3 ${isSelected ? "text-blue-300" : "text-blue-400"}`} />;
    }
    return <Lucide.Folder className="w-3 h-3 text-gray-500" />;
  };

  const handleNodeContextMenu = (e, node) => {
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
      node: node
    });
  };

  const handleAction = (action, node) => {
    console.log(`Action: ${action} on ${node.label}`);
    switch (action) {
      case 'reload':
        refreshNamespaces();
        toast.success("Reloaded namespaces");
        break;
      case 'delete':
        toast.info(`Delete ${node.label} (Not implemented)`);
        break;
      case 'rename':
        toast.info(`Rename ${node.label} (Not implemented)`);
        break;
      case 'move':
        toast.info(`Move ${node.label} (Not implemented)`);
        break;
      case 'create':
        toast.info(`Create New in ${node.label} (Not implemented)`);
        break;
      default:
        break;
    }
  };

  const contextMenuItems = [
    { label: 'Reload', icon: Lucide.RefreshCw, action: () => handleAction('reload', contextMenu?.node) },
    { label: 'Create New', icon: Lucide.Plus, action: () => handleAction('create', contextMenu?.node) },
    { label: 'Rename', icon: Lucide.Edit2, action: () => handleAction('rename', contextMenu?.node) },
    { label: 'Move', icon: Lucide.Move, action: () => handleAction('move', contextMenu?.node) },
    { label: 'Delete', icon: Lucide.Trash2, action: () => handleAction('delete', contextMenu?.node) }
  ];

  return (
    <BrowserPanel
      search={search}
      onSearchChange={setSearch}
      loading={loading}
      error={error}
    >
      <BrowserTree
        nodes={treeNodes}
        selectedId={treeSelectedId}
        onSelect={setTreeSelectedId}
        onDoubleClick={openEditorTab}
        expandedIds={expandedNodes}
        onToggleExpand={toggleNode}
        getIcon={getIcon}
        onNodeContextMenu={handleNodeContextMenu}
      />
      {contextMenu && (
        <ContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          items={contextMenuItems}
          onClose={() => setContextMenu(null)}
        />
      )}
    </BrowserPanel>
  );
}

export { EnvBrowser as ComponentBrowser };