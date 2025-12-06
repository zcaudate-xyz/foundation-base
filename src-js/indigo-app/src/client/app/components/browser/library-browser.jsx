import React from 'react'
import * as Lucide from 'lucide-react'
import { scanNamespaces } from '../../../api'
import { useAppState } from '../../state'

export function LibraryBrowser() {
  const {
    libraryData: data,
    libraryLoading: loading,
    libraryError: error,
    libraryExpandedNodes: expandedNodes,
    setLibraryExpandedNodes: setExpandedNodes,
    librarySearch: search,
    setLibrarySearch: setSearch,
    selectedNamespace,
    setSelectedNamespace
  } = useAppState();

  const toggleNode = (fullPath) => {
    console.log("Toggling node:", fullPath);
    const newExpanded = new Set(expandedNodes);
    if (newExpanded.has(fullPath)) {
      newExpanded.delete(fullPath);
    } else {
      newExpanded.add(fullPath);
    }
    console.log("New expanded set:", Array.from(newExpanded));
    setExpandedNodes(newExpanded);
  };

  const handleSelectNamespace = (ns) => {
    setSelectedNamespace(ns);
  };

  // Filter data based on search
  const filteredData = React.useMemo(() => {
    if (!search) return data;
    const lowerSearch = search.toLowerCase();

    return data.map(langGroup => {
      const matchingNamespaces = langGroup.namespaces.filter(ns =>
        ns.toLowerCase().includes(lowerSearch)
      );

      if (matchingNamespaces.length > 0) {
        return { ...langGroup, namespaces: matchingNamespaces };
      }
      return null;
    }).filter(Boolean);
  }, [data, search]);

  // Build tree from filtered data
  const tree = React.useMemo(() => {
    const root = {};

    filteredData.forEach(langGroup => {
      // Ensure language node exists
      if (!root[langGroup.language]) {
        root[langGroup.language] = {
          name: langGroup.language,
          type: 'language',
          children: {},
          namespaces: []
        };
      }

      langGroup.namespaces.forEach(ns => {
        const parts = ns.split('.');
        let current = root[langGroup.language];

        parts.forEach((part, index) => {
          const isLast = index === parts.length - 1;
          const fullPath = [langGroup.language, ...parts.slice(0, index + 1)].join('.');

          if (isLast) {
            current.namespaces.push({
              name: part,
              fullName: ns,
              fullPath: fullPath
            });
          } else {
            if (!current.children[part]) {
              current.children[part] = {
                name: part,
                type: 'folder',
                children: {},
                namespaces: [],
                fullPath: fullPath
              };
            }
            current = current.children[part];
          }
        });
      });
    });

    return root;
  }, [filteredData]);

  const renderTree = (node, level = 0) => {
    const isExpanded = expandedNodes.has(node.fullPath) || search.length > 0;
    const hasChildren = Object.keys(node.children).length > 0 || node.namespaces.length > 0;
    const paddingLeft = `${level * 12 + 12}px`;

    return (
      <div key={node.fullPath}>
        <div
          className={`flex items-center py-1 pr-2 hover:bg-[#2a2d2e] cursor-pointer select-none text-xs group ${selectedNamespace === node.fullName ? 'bg-[#37373d] text-white' : 'text-gray-300'}`}
          style={{ paddingLeft }}
          onClick={() => hasChildren ? toggleNode(node.fullPath) : null}
        >
          <span className="mr-1 text-gray-500 group-hover:text-gray-300">
            {hasChildren ? (
              isExpanded ? <Lucide.ChevronDown size={12} /> : <Lucide.ChevronRight size={12} />
            ) : <div className="w-3" />}
          </span>

          <span className="mr-1.5 text-blue-400">
            {node.type === 'language' ? <Lucide.Box size={12} /> : <Lucide.Folder size={12} />}
          </span>

          <span className="truncate">{node.name}</span>
        </div>

        {isExpanded && (
          <div>
            {Object.values(node.children).sort((a, b) => a.name.localeCompare(b.name)).map(child => renderTree(child, level + 1))}
            {node.namespaces.sort((a, b) => a.name.localeCompare(b.name)).map(ns => (
              <div
                key={ns.fullName}
                className={`flex items-center py-1 pr-2 hover:bg-[#2a2d2e] cursor-pointer select-none text-xs group ${selectedNamespace === ns.fullName ? 'bg-[#37373d] text-white' : 'text-gray-300'}`}
                style={{ paddingLeft: `${(level + 1) * 12 + 12}px` }}
                onClick={() => handleSelectNamespace(ns.fullName)}
              >
                <span className="mr-1 text-gray-500"><div className="w-3" /></span>
                <span className="mr-1.5 text-yellow-500"><Lucide.FileCode size={12} /></span>
                <span className="truncate">{ns.name}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  if (loading && data.length === 0) {
    return <div className="p-4 text-xs text-gray-500 text-center">Loading library...</div>;
  }

  if (error) {
    return <div className="p-4 text-xs text-red-500 text-center">Error: {error}</div>;
  }

  return (
    <div className="flex flex-col h-full bg-[#1e1e1e] text-gray-300">
      {/* Search */}
      <div className="p-2 border-b border-[#323232]">
        <div className="relative">
          <Lucide.Search className="absolute left-2 top-1.5 text-gray-500" size={12} />
          <input
            type="text"
            placeholder="Search namespaces..."
            className="w-full bg-[#252526] border border-[#323232] rounded pl-7 pr-2 py-1 text-xs text-gray-300 focus:border-blue-500 outline-none placeholder-gray-600"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {/* Tree */}
      <div className="flex-1 overflow-y-auto py-2">
        {Object.values(tree).map(langNode => {
          // Manually construct fullPath for root language nodes since they are top level
          const nodeWithFullPath = { ...langNode, fullPath: langNode.name };
          return renderTree(nodeWithFullPath);
        })}
        {Object.keys(tree).length === 0 && (
          <div className="p-4 text-xs text-gray-500 text-center">No results found</div>
        )}
      </div>
    </div>
  );
}