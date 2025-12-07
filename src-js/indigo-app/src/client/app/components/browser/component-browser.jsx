import * as Lucide from 'lucide-react'
import React from 'react'
import { fetchNamespaces } from '../../../api'
import { fuzzyMatch } from '../../utils/search'
import { BrowserPanel, BrowserTree } from './common'

// Helper to convert raw namespace tree to standardized nodes
function convertToStandardNodes(node) {
  const children = Array.from(node.children.values()).map(convertToStandardNodes);
  // Sort children: folders first, then files? Or just alphabetical?
  // Original was just map, but let's sort alphabetically for consistency
  children.sort((a, b) => a.label.localeCompare(b.label));

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

export function EnvBrowser({ onAddComponent, selectedNamespace, onSelectNamespace }) {
  const [namespaces, setNamespaces] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [expandedNodes, setExpandedNodes] = React.useState(new Set());
  const [search, setSearch] = React.useState("");

  React.useEffect(() => {
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

  return (
    <BrowserPanel
      search={search}
      onSearchChange={setSearch}
      loading={loading}
      error={error}
    >
      <BrowserTree
        nodes={treeNodes}
        selectedId={selectedNamespace}
        onSelect={onSelectNamespace}
        expandedIds={expandedNodes}
        onToggleExpand={toggleNode}
        getIcon={getIcon}
      />
    </BrowserPanel>
  );
}

export { EnvBrowser as ComponentBrowser };