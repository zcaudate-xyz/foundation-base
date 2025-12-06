import React from 'react';

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

export function useStateHistory(components, setComponents) {
    let [history, setHistory] = React.useState(defaultHistory);
    let [historyIndex, setHistoryIndex] = React.useState(0);
    let isUndoRedoAction = React.useRef(false);

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

    return {
        history,
        historyIndex,
        undo,
        redo
    };
}
