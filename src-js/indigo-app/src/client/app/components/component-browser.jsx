import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'
import React from 'react'
import { fetchNamespaces } from '../../api'
import { fuzzyMatch } from '../utils/search'

export function buildNamespaceTree(namespaces) {
  let root = {
    "name": "root",
    "fullPath": "",
    "children": new Map()
  };

  // Create a set for O(1) lookup of valid namespaces
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

  // Recursive function to mark nodes as namespaces
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

  const tree = React.useMemo(() => buildNamespaceTree(filteredNamespaces), [filteredNamespaces]);

  // Auto-expand if searching
  React.useEffect(() => {
    if (search) {
      const allPaths = new Set();
      const traverse = (node) => {
        if (node.fullPath) allPaths.add(node.fullPath);
        node.children.forEach(traverse);
      };
      traverse(tree);
      setExpandedNodes(allPaths);
    }
  }, [search, tree]);

  const renderTree = (node, depth = 0) => {
    const children = Array.from(node.children.values());
    if (children.length === 0) return null;

    return children.map(child => {
      const isExpanded = expandedNodes.has(child.fullPath);
      const hasChildren = child.children.size > 0;
      const isNamespace = child.isNamespace;

      return (
        <div key={child.fullPath}>
          <div
            className="flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer text-xs text-gray-300"
            style={{ paddingLeft: `${depth * 12 + 8}px` }}
            onClick={() => {
              toggleNode(child.fullPath);
              if (isNamespace && onSelectNamespace) {
                onSelectNamespace(child.fullPath);
              }
            }}
          >
            {hasChildren ? (
              isExpanded ? <Lucide.ChevronDown className="w-3 h-3 text-gray-500" /> : <Lucide.ChevronRight className="w-3 h-3 text-gray-500" />
            ) : <div className="w-3" />}

            {isNamespace ? (
              <Lucide.Box className={`w-3 h-3 ${selectedNamespace === child.fullPath ? "text-blue-300" : "text-blue-400"}`} />
            ) : (
              <Lucide.Folder className="w-3 h-3 text-gray-500" />
            )}

            <span className={selectedNamespace === child.fullPath ? "text-white font-medium" : ""}>{child.name}</span>
          </div>
          {isExpanded && renderTree(child, depth + 1)}
        </div>
      );
    });
  };

  if (loading) return <div className="p-4 text-xs text-gray-400">Loading namespaces...</div>;
  if (error) return <div className="p-4 text-xs text-red-400">Error: {error}</div>;

  return (
    <div className="flex flex-col h-full bg-[#252525]">
      <div className="p-3 border-b border-[#323232]">
        <h2 className="text-xs text-gray-400 uppercase tracking-wide mb-2">Project Namespaces</h2>
        <div className="relative">
          <Lucide.Search className="absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-gray-500" />
          <FigmaUi.Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search..."
            className="h-8 pl-7 bg-[#1e1e1e] border-[#323232] text-gray-300 text-xs placeholder:text-gray-600"
          />
        </div>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="py-2">
          {renderTree(tree)}
        </div>
      </FigmaUi.ScrollArea>
    </div>
  );
}

// Export as ComponentBrowser to maintain compatibility with app.jsx import
export { EnvBrowser as ComponentBrowser };