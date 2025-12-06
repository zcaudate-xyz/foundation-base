import * as ReactDnd from 'react-dnd'
import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'
import React from 'react'
import { scanNamespaces } from '../../api'
import { fuzzyMatch } from '../utils/search'

export function buildLibraryTree(libraryData) {
  // libraryData is [{ language: "js", namespaces: [...] }, ...]
  let root = {
    "name": "root",
    "fullPath": "",
    "children": new Map()
  };

  libraryData.forEach(lib => {
    let langNode = {
      "name": lib.language,
      "fullPath": lib.language,
      "type": "language",
      "children": new Map()
    };
    root.children.set(lib.language, langNode);

    lib.namespaces.sort().forEach(ns => {
      let parts = ns.split(".");
      let current = langNode;
      parts.forEach((part, index) => {
        if (!current.children.has(part)) {
          current.children.set(part, {
            "name": part,
            "fullPath": `${lib.language}:${parts.slice(0, index + 1).join(".")}`,
            "type": "node",
            "children": new Map()
          });
        }
        current = current.children.get(part);
      });
      // Mark the last node as a namespace
      current.type = "namespace";
      current.originalNs = ns;
      current.language = lib.language;
    });
  });

  return root;
}

export function LibraryBrowser({ onImportComponent, onImportAndEdit }) {
  const [libraryData, setLibraryData] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [expandedNodes, setExpandedNodes] = React.useState(new Set());
  const [search, setSearch] = React.useState("");

  React.useEffect(() => {
    async function loadLibrary() {
      try {
        const scanned = await scanNamespaces();
        // scanned is { "js": ["ns1", "ns2"], "lua": [...] }
        const data = Object.entries(scanned).map(([lang, namespaces]) => ({
          language: lang,
          namespaces: namespaces
        }));
        setLibraryData(data);
        setLoading(false);
      } catch (err) {
        console.error("Failed to fetch library", err);
        setError(err.message);
        setLoading(false);
      }
    }
    loadLibrary();
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

  const filteredData = React.useMemo(() => {
    if (!search) return libraryData;
    return libraryData.map(lib => ({
      ...lib,
      namespaces: lib.namespaces.filter(ns => fuzzyMatch(ns, search))
    })).filter(lib => lib.namespaces.length > 0);
  }, [libraryData, search]);

  const tree = React.useMemo(() => buildLibraryTree(filteredData), [filteredData]);

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

    return (
      <React.Fragment>
        {children.map(child => {
          const isExpanded = expandedNodes.has(child.fullPath);
          const isLanguage = child.type === "language";
          const isNamespace = child.type === "namespace";

          let Icon = Lucide.Folder;
          let colorClass = "text-gray-500";

          if (isLanguage) {
            Icon = Lucide.Languages;
            colorClass = "text-orange-400";
          } else if (isNamespace) {
            Icon = Lucide.Book;
            colorClass = "text-purple-400";
          }

          return (
            <div key={child.fullPath}>
              <div
                className="flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer text-xs text-gray-300"
                style={{ paddingLeft: `${depth * 12 + 8}px` }}
                onClick={() => toggleNode(child.fullPath)}
              >
                {child.children.size > 0 ? (
                  isExpanded ? <Lucide.ChevronDown className="w-3 h-3 text-gray-500" /> : <Lucide.ChevronRight className="w-3 h-3 text-gray-500" />
                ) : <div className="w-3" />}
                <Icon className={`w-3 h-3 ${colorClass}`} />
                <span>{child.name}</span>
              </div>
              {isExpanded && renderTree(child, depth + 1)}
            </div>
          );
        })}
      </React.Fragment>
    );
  };

  if (loading) return <div className="p-4 text-xs text-gray-400">Loading library...</div>;
  if (error) return <div className="p-4 text-xs text-red-400">Error: {error}</div>;

  return (
    <div className="flex flex-col h-full bg-[#252525]">
      <div className="p-3 border-b border-[#323232]">
        <h2 className="text-xs text-gray-400 uppercase tracking-wide mb-2">Library</h2>
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