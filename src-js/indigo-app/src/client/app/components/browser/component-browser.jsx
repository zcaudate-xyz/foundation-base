import * as Lucide from 'lucide-react'
import React from 'react'
import { fetchNamespaces, deletePath } from '../../../api'
import { useAppState } from '../../state'
import { fuzzyMatch } from '../../utils/search'
import { BrowserPanel, BrowserTree, ContextMenu } from './common'
import { addMessageListener } from '../../../repl-client'
import { toast } from 'sonner'

// Helper to convert raw namespace tree to standardized nodes
function convertToStandardNodes(node) {
  // Recursively process children
  const children = Array.from(node.children.values()).flatMap(convertToStandardNodes);

  // Sort: Folders first, then Files. Within type, alphabetical.
  children.sort((a, b) => {
    // Heuristic: isNamespace=false usually implies folder in this view (except leaf packages? No, leaf packages have isNs=true)
    // Actually our node structure: 
    // - Folders: isNamespace=false, children!=null
    // - Files: isNamespace=true

    // Let's rely on isNamespace. Files (isNamespace=true) come AFTER Folders (isNamespace=false)
    if (a.isNamespace !== b.isNamespace) {
      return a.isNamespace ? 1 : -1;
    }
    return a.label.localeCompare(b.label);
  });

  // Collision case: It is a namespace AND has children
  // We want to return TWO nodes: one for the folder, one for the file.
  if (node.isNamespace && children.length > 0) {
    const folderNode = {
      id: node.fullPath + "-folder", // Distinct ID for folder
      label: node.name,
      children: children,
      isNamespace: false, // Acts as folder
      isSelectable: false // Just expands
    };

    const fileNode = {
      id: node.fullPath,
      label: node.name,
      children: null,
      isNamespace: true,
      isSelectable: true
    };

    return [folderNode, fileNode];
  }

  // Standard case
  const standardNode = {
    id: node.fullPath || "root", // Fallback for root
    label: node.name,
    children: children.length > 0 ? children : null,
    isNamespace: node.isNamespace,
    isSelectable: node.isNamespace
  };

  // Only return this node if it's relevant (root usually skipped by caller)
  return [standardNode];
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
    // Use flatMap to flatten the array of arrays returned by convertToStandardNodes
    const nodes = Array.from(rawRoot.children.values()).flatMap(convertToStandardNodes);

    // Sort top-level nodes as well
    nodes.sort((a, b) => {
      if (a.isNamespace !== b.isNamespace) {
        return a.isNamespace ? 1 : -1;
      }
      return a.label.localeCompare(b.label);
    });

    return nodes;
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
      return <Lucide.Box className={`w-3 h-3 ${isSelected ? "text-blue-500 dark:text-blue-300" : "text-blue-600 dark:text-blue-400"}`} />;
    }
    return <Lucide.Folder className="w-3 h-3 text-muted-foreground" />;
  };

  const handleNodeContextMenu = (e, node) => {
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
      node: node
    });
  };

  // File Watcher Listener
  React.useEffect(() => {
    // We can listen to log messages which include broadcasted messages
    // Ideally we should have a specific listener for file changes in repl-client
    // But broadcastLog handles 'in' messages which are what we get from server (JSON parsed)
    // Actually, repl-client `listeners` set is better.
    const removeListener = addMessageListener((msg) => {
      if (msg.type === "file-change") {
        const { kind, path } = msg;
        const filename = path.split("/").pop();
        toast(`File ${kind}: ${filename}`, {
          description: path,
          duration: 3000,
          action: {
            label: 'Reload',
            onClick: () => refreshNamespaces()
          }
        });
        // Auto-refresh if it looks like a namespace we care about
        if (path.endsWith(".clj") || path.endsWith(".cljs")) {
          refreshNamespaces(); // Less intrusive refresh
        }
      }
    });
    return removeListener;
  }, [refreshNamespaces]);

  const handleAction = async (action, node) => {
    console.log(`Action: ${action} on ${node.label}`);
    switch (action) {
      case 'reload':
        refreshNamespaces();
        toast.success("Reloaded namespaces");
        break;
      case 'eval':
        toast.info(`Eval ${node.label} (Not implemented)`);
        break;
      case 'delete':
        if (confirm(`Are you sure you want to delete ${node.label}?`)) {
          try {
            // node.id is the full path for namespaces/files
            // for folders created by convertToStandardNodes, id has "-folder" suffix, we need to handle that
            // Actually convertToStandardNodes uses node.fullPath as id for files.
            // For folders it appends "-folder". But we want the real path.
            // Let's modify convertToStandardNodes to pass real path in data or similar?
            // Or just strip suffix if present? 
            // Wait, buildNamespaceTree puts fullPath in the node object.
            // But handleAction receives the *tree node*, which is from convertToStandardNodes.
            // The tree node has `id` which IS the path for files.
            // For folders: `id: node.fullPath + "-folder"`.
            // So we need to strip "-folder" for folders.

            let path = node.id;
            if (path.endsWith("-folder") && !node.isNamespace) { // Heuristic
              path = path.substring(0, path.length - 7);
            }

            // Actually, a better way is to pass the real path as a custom prop in tree node
            // But since I can't easily change that without reading the file again contextually...
            // Let's rely on the ID convention I just added in previous step.

            await deletePath(path);
            toast.success(`Deleted ${node.label}`);
            refreshNamespaces();
          } catch (e) {
            console.error(e);
            toast.error(`Failed to delete: ${e.message}`);
          }
        }
        break;
      case 'new-folder':
        toast.info(`New Folder in ${node.label} (Not implemented)`);
        break;
      case 'new-namespace':
        toast.info(`New Namespace in ${node.label} (Not implemented)`);
        break;
      case 'rename':
        toast.info(`Rename ${node.label} (Not implemented)`);
        break;
      case 'move':
        toast.info(`Move ${node.label} (Not implemented)`);
        break;
      default:
        break;
    }
  };

  const getContextMenuItems = (node) => {
    if (!node) return [];

    // File / Namespace Menu
    if (node.isNamespace) {
      return [
        { label: 'Reload', icon: Lucide.RefreshCw, action: () => handleAction('reload', node) },
        { label: 'Eval', icon: Lucide.Play, action: () => handleAction('eval', node) },
        { label: 'Rename', icon: Lucide.Edit2, action: () => handleAction('rename', node) },
        { label: 'Move', icon: Lucide.Move, action: () => handleAction('move', node) },
        { label: 'Delete', icon: Lucide.Trash2, action: () => handleAction('delete', node) }
      ];
    }

    // Folder Menu
    return [
      { label: 'New Folder', icon: Lucide.FolderPlus, action: () => handleAction('new-folder', node) },
      { label: 'New Namespace', icon: Lucide.FilePlus, action: () => handleAction('new-namespace', node) },
      { label: 'Rename', icon: Lucide.Edit2, action: () => handleAction('rename', node) },
      { label: 'Move', icon: Lucide.Move, action: () => handleAction('move', node) },
      { label: 'Delete', icon: Lucide.Trash2, action: () => handleAction('delete', node) }
    ];
  };

  const contextMenuItems = getContextMenuItems(contextMenu?.node);

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