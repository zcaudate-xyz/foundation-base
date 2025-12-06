import * as ReactDnd from 'react-dnd'
import * as Lucide from 'lucide-react'
import * as FigmaUi from '@xtalk/figma-ui'
import React from 'react'
import { scanNamespaces } from '../../../api'
import { fuzzyMatch } from '../../utils/search'

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
  const [libraries, setLibraries] = React.useState({});
  const [expanded, setExpanded] = React.useState(() => {
    try {
      const saved = localStorage.getItem("indigo-library-expanded");
      return saved ? JSON.parse(saved) : {};
    } catch (e) {
      console.error("Failed to load expanded state", e);
      return {};
    }
  });

  React.useEffect(() => {
    try {
      localStorage.setItem("indigo-library-expanded", JSON.stringify(expanded));
    } catch (e) {
      console.error("Failed to save expanded state", e);
    }
  }, [expanded]);

  const [loading, setLoading] = React.useState(false);
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

  const [loadedComponents, setLoadedComponents] = React.useState(new Map()); // Map<ns, components[]>

  const fetchNamespaceComponents = async (lang, ns) => {
    console.log("Fetching components for", lang, ns);
    if (loadedComponents.has(ns)) {
      console.log("Already loaded", ns);
      return;
    }
    try {
      const comps = await import('../../../api').then(m => m.fetchComponents(lang, ns));
      console.log("Fetched components", ns, comps);
      setLoadedComponents(prev => new Map(prev).set(ns, comps));
    } catch (err) {
      console.error("Failed to fetch components for", ns, err);
    }
  };

  const toggleNode = (node) => {
    const path = node.fullPath;
    console.log("Toggling node", path, node.type, expandedNodes.has(path));
    if (node.type === "namespace" && !expandedNodes.has(path)) {
      console.log("Fetching for", node.language, node.originalNs);
      fetchNamespaceComponents(node.language, node.originalNs);
    }

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

  const [previewComponent, setPreviewComponent] = React.useState(null); // { ns, name, ... }
  const [previewCode, setPreviewCode] = React.useState("");

  React.useEffect(() => {
    if (previewComponent) {
      setPreviewCode("Loading...");
      import('../../../api').then(m => m.emitComponent(previewComponent.language, previewComponent.ns, previewComponent.name))
        .then(code => setPreviewCode(code))
        .catch(err => setPreviewCode(`Error: ${err.message}`));
    } else {
      setPreviewCode("");
    }
  }, [previewComponent]);

  const renderComponents = (ns, components, lang) => {
    const fragments = components.filter(c => c.type === 'fragment');
    const forms = components.filter(c => c.type === 'form');

    const renderItem = (c) => {
      const isPreviewing = previewComponent?.ns === ns && previewComponent?.name === c.name;

      return (
        <div key={c.name} className="mb-1">
          <div
            className={`text-xs text-gray-400 hover:text-blue-400 cursor-pointer py-0.5 px-2 hover:bg-[#323232] rounded flex items-center justify-between group ${isPreviewing ? 'bg-[#323232]' : ''}`}
            onClick={() => onImportAndEdit({ ...c, libraryRef: c.name })}
          >
            <span>{c.name}</span>
            <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100">
              <button
                className="p-1 hover:bg-[#404040] rounded text-gray-400 hover:text-white"
                onClick={(e) => {
                  e.stopPropagation();
                  setPreviewComponent(isPreviewing ? null : { ...c, ns, language: lang });
                }}
                title="Preview & Metadata"
              >
                <Lucide.Eye size={12} />
              </button>
              <button
                className="p-1 hover:bg-[#404040] rounded text-gray-400 hover:text-white"
                onClick={(e) => {
                  e.stopPropagation();
                  onImportAndEdit({ ...c, libraryRef: c.name });
                }}
                title="Import"
              >
                <Lucide.Plus size={12} />
              </button>
            </div>
          </div>
          {isPreviewing && (
            <div className="ml-2 mt-1 p-2 bg-[#1e1e1e] rounded border border-[#323232] text-[10px]">
              <div className="mb-2">
                <div className="font-bold text-gray-500 mb-0.5">METADATA</div>
                <pre className="text-gray-400 whitespace-pre-wrap font-mono max-h-32 overflow-auto">
                  {JSON.stringify(c.meta || {}, null, 2)}
                </pre>
              </div>
              <div>
                <div className="font-bold text-gray-500 mb-0.5">GENERATED OUTPUT</div>
                <pre className="text-gray-400 whitespace-pre-wrap font-mono max-h-64 overflow-auto">
                  {previewCode}
                </pre>
              </div>
            </div>
          )}
        </div>
      );
    };

    return (
      <div className="pl-4 border-l border-[#323232] ml-2 mt-1">
        {fragments.length > 0 && (
          <div className="mb-2">
            <div className="text-[10px] uppercase text-gray-500 font-bold mb-1">Fragments</div>
            {fragments.map(renderItem)}
          </div>
        )}
        {forms.length > 0 && (
          <div>
            <div className="text-[10px] uppercase text-gray-500 font-bold mb-1">Forms</div>
            {forms.map(renderItem)}
          </div>
        )}
      </div>
    );
  };

  const renderTree = (node, depth = 0) => {
    const children = Array.from(node.children.values());
    // If it's a namespace and expanded, we might have components to show
    const isNamespace = node.type === "namespace";
    const isExpanded = expandedNodes.has(node.fullPath);
    const components = isNamespace && isExpanded ? loadedComponents.get(node.originalNs) : null;

    if (children.length === 0 && !isNamespace) return null;

    return (
      <React.Fragment>
        {children.map(child => {
          const isChildExpanded = expandedNodes.has(child.fullPath);
          const isChildNamespace = child.type === "namespace";
          const isLanguage = child.type === "language";

          let Icon = Lucide.Folder;
          let colorClass = "text-gray-500";

          if (isLanguage) {
            Icon = Lucide.Languages;
            colorClass = "text-orange-400";
          } else if (isChildNamespace) {
            Icon = Lucide.Book;
            colorClass = "text-purple-400";
          }

          return (
            <div key={child.fullPath}>
              <div
                className="flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer text-xs text-gray-300"
                style={{ paddingLeft: `${depth * 12 + 8}px` }}
                onClick={() => toggleNode(child)}
              >
                {(child.children.size > 0 || isChildNamespace) ? (
                  isChildExpanded ? <Lucide.ChevronDown className="w-3 h-3 text-gray-500" /> : <Lucide.ChevronRight className="w-3 h-3 text-gray-500" />
                ) : <div className="w-3" />}
                <Icon className={`w-3 h-3 ${colorClass}`} />
                <span>{child.name}</span>
              </div>
              {isChildExpanded && (
                <>
                  {renderTree(child, depth + 1)}
                  {isChildNamespace && loadedComponents.get(child.originalNs) && (
                    renderComponents(child.originalNs, loadedComponents.get(child.originalNs), child.language)
                  )}
                </>
              )}
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