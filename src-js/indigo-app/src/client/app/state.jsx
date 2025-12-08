import React from 'react'
import * as te from '@/client/app/components/editor/theme-editor'
import { fetchNamespaceSource, fetchDocPath, fetchFileContent, fetchNamespaceEntries, scanNamespaces, fetchComponents } from '../api'
import { useServerEvent } from './events-context'

// code.dev.client.app/defaultComponents [18] 
export var defaultComponents = [
    {
        "properties": { "padding": "$4", "backgroundColor": "$background" },
        "children": [
            {
                "inputValues": {
                    "description": "This card demonstrates how inputs work. Edit the input values in the Inputs tab to see changes.",
                    "title": "Welcome to Input Binding!",
                    "buttonText": "Click Me",
                    "count": 42
                },
                "properties": {
                    "className": "p-6 bg-white rounded-lg shadow-md max-w-md mx-auto mt-8"
                },
                "children": [
                    {
                        "properties": {
                            "children": "{input.title}",
                            "className": "text-2xl font-bold text-gray-900 mb-4"
                        },
                        "children": [],
                        "type": "Heading",
                        "label": "Card Title",
                        "id": "example-heading-1"
                    },
                    {
                        "properties": {
                            "children": "{input.description}",
                            "className": "text-gray-600 mb-4"
                        },
                        "children": [],
                        "type": "Text",
                        "label": "Card Description",
                        "id": "example-text-1"
                    },
                    {
                        "properties": {
                            "children": "Clicks: {state.clickCount}",
                            "className": "text-sm text-gray-500 mb-4"
                        },
                        "children": [],
                        "type": "Text",
                        "label": "Counter Display",
                        "id": "example-text-2"
                    },
                    {
                        "properties": {
                            "children": "{input.buttonText}",
                            "className": "px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
                        },
                        "children": [],
                        "type": "Button",
                        "label": "Action Button",
                        "id": "example-button-1"
                    }
                ],
                "states": {
                    "clickCount": {
                        "description": "Number of button clicks",
                        "default": 0,
                        "type": "number"
                    },
                    "isVisible": {
                        "description": "Controls visibility of description",
                        "default": true,
                        "type": "boolean"
                    }
                },
                "type": "Card",
                "triggers": {
                    "onButtonClick": {
                        "description": "Increment counter when button is clicked",
                        "event": "click",
                        "action": "incrementClicks"
                    }
                },
                "actions": {
                    "incrementClicks": {
                        "description": "Increment click counter",
                        "type": "incrementState",
                        "target": "clickCount"
                    },
                    "toggleVisibility": {
                        "description": "Toggle description visibility",
                        "type": "toggleState",
                        "target": "isVisible"
                    }
                },
                "inputs": {
                    "description": { "description": "Card description", "type": "string" },
                    "title": { "description": "Card title text", "type": "string" },
                    "buttonText": { "description": "Button label", "type": "string" },
                    "count": { "description": "Counter value", "type": "number" }
                },
                "label": "Example Card",
                "id": "example-card-1"
            }
        ],
        "type": "View",
        "label": "Scene",
        "id": "root"
    }
];

import { useStateHistory } from './state-history'

const AppStateContext = React.createContext(null);

export function AppStateProvider({ children }) {
    let [components, setComponents] = React.useState(() => {
        try {
            const saved = localStorage.getItem("indigo-components");
            return saved ? JSON.parse(saved) : defaultComponents;
        } catch (e) {
            console.error("Failed to load components from localStorage", e);
            return defaultComponents;
        }
    });

    React.useEffect(() => {
        try {
            localStorage.setItem("indigo-components", JSON.stringify(components));
        } catch (e) {
            console.error("Failed to save components to localStorage", e);
        }
    }, [components]);

    let [selectedComponent, setSelectedComponent] = React.useState(() => {
        return localStorage.getItem("indigo-selected-component") || "example-card-1";
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-selected-component", selectedComponent);
    }, [selectedComponent]);

    let [selectedNamespace, setSelectedNamespace] = React.useState(() => {
        return localStorage.getItem("indigo-selected-namespace") || null;
    });

    React.useEffect(() => {
        if (selectedNamespace) {
            localStorage.setItem("indigo-selected-namespace", selectedNamespace);
        } else {
            localStorage.removeItem("indigo-selected-namespace");
        }
    }, [selectedNamespace]);

    let [treeSelectedId, setTreeSelectedId] = React.useState(null);

    let [editorTabs, setEditorTabs] = React.useState(() => {
        try {
            const saved = localStorage.getItem("indigo-editor-tabs");
            return saved ? JSON.parse(saved) : [];
        } catch (e) {
            return [];
        }
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-editor-tabs", JSON.stringify(editorTabs));
    }, [editorTabs]);

    const openEditorTab = React.useCallback((ns) => {
        setEditorTabs(prev => {
            if (!prev.includes(ns)) {
                return [...prev, ns];
            }
            return prev;
        });
        setSelectedNamespace(ns);
    }, []);

    const closeEditorTab = React.useCallback((ns) => {
        setEditorTabs(prev => {
            const newTabs = prev.filter(t => t !== ns);
            return newTabs;
        });
        // If closing active tab, switch to another
        if (selectedNamespace === ns) {
            setEditorTabs(prev => {
                const index = prev.indexOf(ns);
                const newTabs = prev.filter(t => t !== ns);
                if (newTabs.length > 0) {
                    // Try to go to left, else right
                    const nextIndex = Math.max(0, index - 1);
                    setSelectedNamespace(newTabs[nextIndex]);
                } else {
                    setSelectedNamespace(null);
                }
                return newTabs;
            });
        }
    }, [selectedNamespace]);

    let [selectedVar, setSelectedVar] = React.useState(() => {
        return localStorage.getItem("indigo-selected-var") || null;
    });

    React.useEffect(() => {
        if (selectedVar) {
            localStorage.setItem("indigo-selected-var", selectedVar);
        } else {
            localStorage.removeItem("indigo-selected-var");
        }
    }, [selectedVar]);

    let [viewMode, setViewMode] = React.useState("design");
    let [theme, setTheme] = React.useState("dark");

    const { history, historyIndex, undo, redo } = useStateHistory(components, setComponents);

    let [activeTab, setActiveTab] = React.useState(() => {
        return localStorage.getItem("indigo-active-tab") || "env";
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-active-tab", activeTab);
    }, [activeTab]);

    let [activeModal, setActiveModal] = React.useState(null);

    let findComponentById = function (components, id) {
        for (let component of components) {
            if (component.id == id) {
                return component;
            }
            let found = findComponentById(component.children, id);
            if (found) {
                return found;
            }
        }
        return null;
    };

    let removeComponentById = function (components, id) {
        for (let i = 0; i < components.length; ++i) {
            if (components[i].id == id) {
                components.splice(i, 1);
                return true;
            }
            if (removeComponentById(components[i].children, id)) {
                return true;
            }
        }
        return false;
    };

    let getDefaultProperties = function (type) {
        let defaults = {
            "Heading": { "children": "Heading", "className": "text-2xl font-bold" },
            "Container": { "className": "p-4" },
            "Input": {
                "placeholder": "Enter text...",
                "className": "px-4 py-2 border border-gray-300 rounded"
            },
            "Card": { "className": "p-6 bg-white rounded-lg shadow" },
            "Switch": { "className": "" },
            "Text": { "children": "Text content", "className": "text-gray-700" },
            "FlexRow": { "className": "flex gap-4" },
            "Checkbox": { "className": "" },
            "FlexCol": { "className": "flex flex-col gap-4" },
            "Button": {
                "children": "Button",
                "className": "px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            }
        };
        return defaults[type] || {};
    };

    let addComponent = function (type, parentId = "root") {
        if (type == "__REFRESH__") {
            setComponents(function (prev) {
                [transduce(map(identity), conj, [], prev)];
            });
            return;
        }
        let newComponent = {
            "properties": getDefaultProperties(type),
            "children": [],
            "parent": parentId,
            "type": type,
            "label": type,
            "id": type.toLowerCase() + "-" + Date.now()
        };
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let parent = findComponentById(updated, parentId);
            if (parent) {
                parent.children.push(newComponent);
            }
            return updated;
        });
        setSelectedComponent(newComponent.id);
    };

    let moveComponent = function (draggedId, targetId, position) {
        setComponents(function (prev) {
            let updated = JSON.parse(JSON.stringify(prev));
            let draggedComponent = null;
            let removeDragged = function (comps) {
                for (let i = 0; i < comps.length; ++i) {
                    if (comps[i].id == draggedId) {
                        draggedComponent = comps[i];
                        comps.splice(i, 1);
                        return true;
                    }
                    if (removeDragged(comps[i].children)) {
                        return true;
                    }
                }
                return false;
            };
            removeDragged(updated);
            if (!draggedComponent) {
                return prev;
            }
            let insertComponent = function (comps, parentComps = undefined) {
                for (let i = 0; i < comps.length; ++i) {
                    if (comps[i].id == targetId) {
                        if (position == "inside") {
                            comps[i].children.push(draggedComponent);
                        }
                        else {
                            if (position == "before") {
                                comps.splice(i, 0, draggedComponent);
                            }
                            else {
                                comps.splice(i + 1, 0, draggedComponent);
                            }
                        }
                        return true;
                    }
                    if (insertComponent(comps[i].children, comps)) {
                        return true;
                    }
                }
                return false;
            };
            insertComponent(updated);
            return updated;
        });
    };

    let updateComponentProperty = function (id, property, value) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                if (property == "label") {
                    component.label = value;
                }
                else {
                    component.properties[property] = value;
                }
            }
            return updated;
        });
    };

    let updateComponentInputs = function (id, inputs) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                component.inputs = inputs;
            }
            return updated;
        });
    };

    let updateComponentInputValues = function (id, inputValues) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                component.inputValues = inputValues;
            }
            return updated;
        });
    };

    let updateComponentStates = function (id, states) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                component.states = states;
            }
            return updated;
        });
    };

    let updateComponentTriggers = function (id, triggers) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                component.triggers = triggers;
            }
            return updated;
        });
    };

    let updateComponentActions = function (id, actions) {
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let component = findComponentById(updated, id);
            if (component) {
                component.actions = actions;
            }
            return updated;
        });
    };

    let deleteComponent = function (id) {
        if (id == "root") {
            return;
        }
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            removeComponentById(updated, id);
            return updated;
        });
        setSelectedComponent("root");
    };

    let importComponent = function (component) {
        let generateNewIds = function (comp, isRoot = true) {
            return {
                "children": comp.children.map(function (child) {
                    return generateNewIds(child, false);
                }),
                "id": comp.type.toLowerCase() + "-" + Date.now() + "-" + Math.random().toString(36).substr(2, 9),
                "libraryRef": isRoot ? comp.libraryRef : undefined,
                ...comp
            };
        };
        let newComponent = generateNewIds(component);
        setComponents(function (prev) {
            let updated = [transduce(map(identity), conj, [], prev)];
            let parent = findComponentById(updated, "root");
            if (parent) {
                parent.children.push(newComponent);
            }
            return updated;
        });
        return newComponent.id;
    };

    let importAndEditComponent = function (component) {
        let newId = importComponent(component);
        setSelectedComponent(newId);
    };

    let selectedComponentData = findComponentById(components, selectedComponent);

    // Namespace View State
    let [namespaceViewType, setNamespaceViewType] = React.useState(() => {
        return localStorage.getItem("indigo-ns-view-type") || "file";
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-ns-view-type", namespaceViewType);
    }, [namespaceViewType]);

    let [tabStates, setTabStates] = React.useState(() => {
        try {
            const saved = localStorage.getItem("indigo-tab-states");
            return saved ? JSON.parse(saved) : {};
        } catch (e) {
            return {};
        }
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-tab-states", JSON.stringify(tabStates));
    }, [tabStates]);

    // Derived view mode for current tab
    let namespaceFileViewMode = React.useMemo(() => {
        return (selectedNamespace && tabStates[selectedNamespace]?.viewMode) || "source";
    }, [selectedNamespace, tabStates]);

    const setNamespaceFileViewMode = React.useCallback((mode) => {
        if (!selectedNamespace) return;
        setTabStates(prev => ({
            ...prev,
            [selectedNamespace]: {
                ...prev[selectedNamespace],
                viewMode: mode
            }
        }));
    }, [selectedNamespace]);

    let [namespaceEntries, setNamespaceEntries] = React.useState([]);
    let [namespaceEntriesLoading, setNamespaceEntriesLoading] = React.useState(false);
    let [namespaceCode, setNamespaceCode] = React.useState("");
    let [namespaceLoading, setNamespaceLoading] = React.useState(false);
    let [namespaceError, setNamespaceError] = React.useState(null);
    let [runningTest, setRunningTest] = React.useState(null);

    // Env Browser State
    let [envExpandedNodes, setEnvExpandedNodes] = React.useState(() => {
        try {
            const saved = localStorage.getItem("indigo-env-expanded");
            return saved ? new Set(JSON.parse(saved)) : new Set();
        } catch (e) {
            console.error("Failed to load env expanded state", e);
            return new Set();
        }
    });

    React.useEffect(() => {
        try {
            localStorage.setItem("indigo-env-expanded", JSON.stringify(Array.from(envExpandedNodes)));
        } catch (e) {
            console.error("Failed to save env expanded state", e);
        }
    }, [envExpandedNodes]);

    // Library State
    let [libraryExpandedNodes, setLibraryExpandedNodes] = React.useState(() => {
        try {
            const saved = localStorage.getItem("indigo-library-expanded");
            return saved ? new Set(JSON.parse(saved)) : new Set();
        } catch (e) {
            console.error("Failed to load expanded state", e);
            return new Set();
        }
    });

    React.useEffect(() => {
        try {
            localStorage.setItem("indigo-library-expanded", JSON.stringify(Array.from(libraryExpandedNodes)));
        } catch (e) {
            console.error("Failed to save expanded state", e);
        }
    }, [libraryExpandedNodes]);

    let [librarySearch, setLibrarySearch] = React.useState("");
    let [libraryData, setLibraryData] = React.useState([]);
    let [libraryLoading, setLibraryLoading] = React.useState(false);
    let [libraryError, setLibraryError] = React.useState(null);

    // Fetch Library Data
    React.useEffect(() => {
        async function loadLibrary() {
            if (libraryData.length > 0) return; // Already loaded
            setLibraryLoading(true);
            try {
                const scanned = await scanNamespaces();
                if (scanned.error) {
                    console.error("Scan failed:", scanned.error);
                    setLibraryError(scanned.error);
                } else {
                    const data = Object.entries(scanned)
                        .filter(([lang, namespaces]) => Array.isArray(namespaces))
                        .map(([lang, namespaces]) => ({
                            language: lang,
                            namespaces: namespaces
                        }));
                    setLibraryData(data);
                }
            } catch (err) {
                console.error("Failed to fetch library", err);
                setLibraryError(err.message);
            } finally {
                setLibraryLoading(false);
            }
        }
        loadLibrary();
    }, []); // Run once on mount

    // Fetch Namespace Entries
    const refreshNamespaceEntries = React.useCallback(async () => {
        if (!selectedNamespace) {
            setNamespaceEntries([]);
            return;
        }
        setNamespaceEntriesLoading(true);
        try {
            if (activeTab === "library") {
                // Find language for selected namespace
                let lang = 'clj'; // Default
                for (const group of libraryData) {
                    if (group.namespaces.some(n => n.fullName === selectedNamespace || n === selectedNamespace)) {
                        lang = group.language;
                        break;
                    }
                }

                const components = await fetchComponents(lang, selectedNamespace);
                const entries = components.map(c => ({
                    var: c.name,
                    type: c.type, // :fragment or :code
                    op: c.op,     // defn, def, etc.
                    meta: c.meta,
                    test: null    // No tests for library entries yet
                }));
                setNamespaceEntries(entries);
            } else {
                const data = await fetchNamespaceEntries(selectedNamespace);
                setNamespaceEntries(data.entries || []);
            }
        } catch (err) {
            console.error("Failed to load entries", err);
        } finally {
            setNamespaceEntriesLoading(false);
        }
    }, [selectedNamespace, activeTab, libraryData]);

    React.useEffect(() => {
        refreshNamespaceEntries();
    }, [refreshNamespaceEntries]);

    // File Buffers (Cache for tab content)
    let [fileBuffers, setFileBuffers] = React.useState({});

    // Invalidate cache on file change from server
    useServerEvent('file-change', (msg) => {
        setFileBuffers({});
    });

    const getBufferKey = (ns, mode) => `${ns}:${mode}`;

    // Update code both in current state and buffer
    const updateNamespaceCode = React.useCallback((newCode) => {
        setNamespaceCode(newCode);
        if (selectedNamespace) {
            const key = getBufferKey(selectedNamespace, namespaceFileViewMode);
            setFileBuffers(prev => ({
                ...prev,
                [key]: newCode
            }));
        }
    }, [selectedNamespace, namespaceFileViewMode]);

    // Fetch Namespace Code (File View)
    const refreshNamespaceCode = React.useCallback(async (force = false) => {
        if (!selectedNamespace || namespaceViewType !== "file") return;

        const key = getBufferKey(selectedNamespace, namespaceFileViewMode);

        // Use buffer if available and not forcing reload
        if (!force && fileBuffers.hasOwnProperty(key)) {
            setNamespaceCode(fileBuffers[key]);
            return;
        }

        setNamespaceLoading(true);
        setNamespaceError(null);
        // Don't clear code immediately if we are reloading? Maybe nice to keep old code visible?
        // But for now follow existing pattern or clean it.
        // setNamespaceCode(""); // If we clear it, it flashes. Better to keep stale until load?
        // Existing behavior was setNamespaceCode("")
        if (!fileBuffers[key]) setNamespaceCode("");

        try {
            let content = "";
            let mode = namespaceFileViewMode; // Capture for closure consistency if needed

            if (mode === "source") {
                content = await fetchNamespaceSource(selectedNamespace);
            } else if (mode === "test") {
                const testNs = selectedNamespace + "-test";
                content = await fetchNamespaceSource(testNs);
                if (content.startsWith(";; File not found")) {
                    setNamespaceError("Test file not found");
                    setNamespaceCode("");
                    setNamespaceLoading(false);
                    return;
                }
            } else if (mode === "doc") {
                const docInfo = await fetchDocPath(selectedNamespace);
                if (docInfo.found) {
                    content = await fetchFileContent(docInfo.path);
                } else {
                    setNamespaceError(docInfo.message || "Documentation not found");
                    setNamespaceCode("");
                    setNamespaceLoading(false);
                    return;
                }
            }

            // Update state and buffer
            setNamespaceCode(content);
            setFileBuffers(prev => ({
                ...prev,
                [key]: content
            }));

        } catch (err) {
            console.error("Failed to load source", err);
            setNamespaceError(err.message);
        } finally {
            setNamespaceLoading(false);
        }
    }, [selectedNamespace, namespaceViewType, namespaceFileViewMode, fileBuffers]);

    React.useEffect(() => {
        refreshNamespaceCode();
    }, [refreshNamespaceCode]);

    const value = {
        components,
        selectedComponent,
        selectedNamespace,
        selectedVar,
        viewMode,
        theme,
        activeModal,
        history,
        historyIndex,
        activeTab,
        selectedComponentData,

        // Namespace View
        namespaceViewType,
        namespaceFileViewMode,
        namespaceEntries,
        namespaceEntriesLoading,
        namespaceCode,
        namespaceLoading,
        namespaceError,
        runningTest,
        setNamespaceViewType,
        setNamespaceFileViewMode,
        setNamespaceEntries,
        setNamespaceEntriesLoading,
        setNamespaceCode: updateNamespaceCode, // Use wrapper
        setNamespaceLoading,
        setNamespaceError,
        setRunningTest,
        refreshNamespaceCode,
        refreshNamespaceEntries,

        // Env Browser
        envExpandedNodes,
        setEnvExpandedNodes,

        // Library
        libraryExpandedNodes,
        librarySearch,
        libraryData,
        libraryLoading,
        libraryError,
        setLibraryExpandedNodes,
        setLibrarySearch,
        setLibraryData,
        setLibraryLoading,
        setLibraryError,

        setComponents,
        setSelectedComponent,
        setSelectedNamespace,
        setSelectedVar,
        setViewMode,
        setTheme,
        setActiveModal,
        setActiveTab,
        undo,
        redo,
        addComponent,
        moveComponent,
        updateComponentProperty,
        updateComponentInputs,
        updateComponentInputValues,
        updateComponentStates,
        updateComponentTriggers,
        updateComponentActions,
        deleteComponent,
        importComponent,
        importAndEditComponent,
        editorTabs,
        openEditorTab,
        closeEditorTab,
        treeSelectedId,
        setTreeSelectedId
    };

    return (
        <AppStateContext.Provider value={value}>
            {children}
        </AppStateContext.Provider>
    );
}

export function useAppState() {
    const context = React.useContext(AppStateContext);
    if (!context) {
        throw new Error("useAppState must be used within an AppStateProvider");
    }
    return context;
}
