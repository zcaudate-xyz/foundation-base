import React from 'react'
import * as te from '@/client/app/components/editor/theme-editor'

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

// code.dev.client.app/defaultHistory [80] 
export var defaultHistory = [
    [
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
    ]
];

export function useAppState() {
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
    let [theme, setTheme] = React.useState(te.defaultTheme);
    let [history, setHistory] = React.useState(defaultHistory);
    let [historyIndex, setHistoryIndex] = React.useState(0);
    let isUndoRedoAction = React.useRef(false);

    let [activeTab, setActiveTab] = React.useState(() => {
        return localStorage.getItem("indigo-active-tab") || "env";
    });

    React.useEffect(() => {
        localStorage.setItem("indigo-active-tab", activeTab);
    }, [activeTab]);

    React.useEffect(function () {
        if (isUndoRedoAction.current) {
            isUndoRedoAction.current = false;
            return;
        }
        let newState = JSON.parse(JSON.stringify(components));
        setHistory(function (prev) {
            let newHistory = prev.slice(0, historyIndex + 1);
            newHistory.push(newState);
            if (newHistory.length > 50) {
                newHistory.shift();
            }
            return newHistory;
        });
        setHistoryIndex(function (prev) {
            let newIndex = prev + 1;
            return (newIndex >= 50) ? 49 : newIndex;
        });
    }, [components]);

    let undo = function () {
        if (historyIndex > 0) {
            isUndoRedoAction.current = true;
            let newIndex = historyIndex - 1;
            setHistoryIndex(newIndex);
            setComponents(JSON.parse(JSON.stringify(history[newIndex])));
        }
    };

    let redo = function () {
        if (historyIndex < (history.length - 1)) {
            isUndoRedoAction.current = true;
            let newIndex = historyIndex + 1;
            setHistoryIndex(newIndex);
            setComponents(JSON.parse(JSON.stringify(history[newIndex])));
        }
    };

    React.useEffect(function () {
        let handleKeyDown = function (e) {
            if ((e.ctrlKey || e.metaKey) && !e.shiftKey && (e.key == "z")) {
                e.preventDefault();
                undo();
            }
            else {
                if ((e.ctrlKey || e.metaKey) && ((e.shiftKey && (e.key == "z")) || (e.key == "y"))) {
                    e.preventDefault();
                    redo();
                }
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return function () {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [historyIndex, history]);

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

    return {
        components,
        selectedComponent,
        selectedNamespace,
        selectedVar,
        viewMode,
        theme,
        history,
        historyIndex,
        activeTab,
        selectedComponentData,
        setComponents,
        setSelectedComponent,
        setSelectedNamespace,
        setSelectedVar,
        setViewMode,
        setTheme,
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
        importAndEditComponent
    };
}
