import React from 'react'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'
import { BrowserPanel, BrowserTree } from './common'

// Helper to convert raw library tree to standardized nodes
function convertToStandardNodes(node) {
  let children = [];

  // Process children (folders)
  if (node.children) {
    children = children.concat(
      Object.values(node.children)
        .map(convertToStandardNodes)
        .sort((a, b) => a.label.localeCompare(b.label))
    );
  }

  // Process namespaces (files)
  if (node.namespaces) {
    children = children.concat(
      node.namespaces
        .map(ns => ({
          id: ns.fullName,
          label: ns.name,
          children: null,
          isNamespace: true,
          isSelectable: true
        }))
        .sort((a, b) => a.label.localeCompare(b.label))
    );
  }

  return {
    id: node.fullPath || node.name, // Fallback for root language nodes
    label: node.name,
    children: children.length > 0 ? children : null,
    type: node.type,
    isSelectable: false // Only namespaces are selectable
  };
}

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
    const newExpanded = new Set(expandedNodes);
    if (newExpanded.has(fullPath)) {
      newExpanded.delete(fullPath);
    } else {
      newExpanded.add(fullPath);
    }
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
  const treeNodes = React.useMemo(() => {
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

    return Object.values(root).map(convertToStandardNodes);
  }, [filteredData]);

  const getIcon = (node, isExpanded, isSelected) => {
    if (node.type === 'language') {
      return <Lucide.Square size={12} className="text-magenta-500 fill-current" style={{ color: '#ff00ff' }} />;
    }
    if (node.isNamespace) {
      return <Lucide.FileCode size={12} className="text-yellow-500" />;
    }
    return <Lucide.Folder size={12} className="text-gray-500" />;
  };

  return (
    <BrowserPanel
      search={search}
      onSearchChange={setSearch}
      loading={loading && data.length === 0}
      error={error}
    >
      <BrowserTree
        nodes={treeNodes}
        selectedId={selectedNamespace}
        onSelect={handleSelectNamespace}
        expandedIds={expandedNodes}
        onToggleExpand={toggleNode}
        getIcon={getIcon}
      />
    </BrowserPanel>
  );
}