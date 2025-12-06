import * as ReactDndHtml5Backend from 'react-dnd-html5-backend'

import * as ReactDnd from 'react-dnd'

import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'


import * as te from '@/client/app/components/theme-editor'

import * as pp from '@/client/app/components/properties-panel'

import * as vc from '@/client/app/components/viewport-canvas'

import * as cb from '@/client/app/components/component-browser'

import * as lb from '@/client/app/components/library-browser'

import * as op from '@/client/app/components/outliner-panel'

import * as rp from '@/client/app/components/repl-panel'

import * as nv from '@/client/app/components/namespace-viewer'

// code.dev.client.app/defaultTheme [16] 
export var defaultTheme = te.defaultTheme;

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

// code.dev.client.app/App [140] 
export function App() {
  let [components, setComponents] = React.useState(defaultComponents);
  let [selectedComponent, setSelectedComponent] = React.useState("example-card-1");
  let [selectedNamespace, setSelectedNamespace] = React.useState(null);
  let [selectedVar, setSelectedVar] = React.useState(null);
  let [viewMode, setViewMode] = React.useState("design");
  let [theme, setTheme] = React.useState(defaultTheme);
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
  return (
    <ReactDnd.DndProvider backend={ReactDndHtml5Backend.HTML5Backend}>
      <div className="flex flex-col h-screen bg-[#1e1e1e]">
        <FigmaUi.ResizablePanelGroup direction="horizontal" className="flex-1">
          <FigmaUi.ResizablePanel defaultSize={20} minSize={15} maxSize={30}>
            <FigmaUi.Tabs defaultValue="env" className="flex-1 flex flex-col h-full">
              <div className="bg-[#252525] border-b border-[#323232]">
                <FigmaUi.TabsList
                  className="w-full justify-start rounded-none bg-transparent border-b-0 h-10">
                  <FigmaUi.TabsTrigger
                    value="env"
                    className="rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200">Env
                  </FigmaUi.TabsTrigger>
                  <FigmaUi.TabsTrigger
                    value="library"
                    className="rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200">Library
                  </FigmaUi.TabsTrigger>
                </FigmaUi.TabsList>
              </div>
              <FigmaUi.TabsContent value="env" className="flex-1 m-0">
                <cb.ComponentBrowser
                  onAddComponent={addComponent}
                  selectedNamespace={selectedNamespace}
                  onSelectNamespace={setSelectedNamespace}>
                </cb.ComponentBrowser>
              </FigmaUi.TabsContent>
              <FigmaUi.TabsContent value="library" className="flex-1 m-0">
                <lb.LibraryBrowser
                  onImportComponent={function (comp) {
                    return importComponent(comp);
                  }}
                  onImportAndEdit={importAndEditComponent}>
                </lb.LibraryBrowser>
              </FigmaUi.TabsContent>
            </FigmaUi.Tabs>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={50} minSize={30}>
            <FigmaUi.ResizablePanelGroup direction="vertical">
              <FigmaUi.ResizablePanel defaultSize={70} minSize={40}>
                {selectedNamespace ? (
                  <nv.NamespaceViewer
                    namespace={selectedNamespace}
                    selectedVar={selectedVar}
                  />
                ) : (
                  <vc.ViewportCanvas
                    components={components}
                    selectedComponent={selectedComponent}
                    onSelectComponent={setSelectedComponent}
                    onAddComponent={addComponent}
                    onMoveComponent={moveComponent}
                    viewMode={viewMode}
                    theme={theme}>
                  </vc.ViewportCanvas>
                )}
              </FigmaUi.ResizablePanel>
              <FigmaUi.ResizableHandle className="h-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
              <FigmaUi.ResizablePanel defaultSize={30} minSize={15}>
                <rp.ReplPanel />
              </FigmaUi.ResizablePanel>
            </FigmaUi.ResizablePanelGroup>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={30} minSize={20} maxSize={40}>
            <pp.PropertiesPanel
              component={selectedComponentData}
              selectedNamespace={selectedNamespace}
              onSelectVar={setSelectedVar}
              onUpdateProperty={updateComponentProperty}
              onDeleteComponent={deleteComponent}
              onUpdateInputs={updateComponentInputs}
              onUpdateInputValues={updateComponentInputValues}
              onUpdateStates={updateComponentStates}
              onUpdateTriggers={updateComponentTriggers}
              onUpdateActions={updateComponentActions}>
            </pp.PropertiesPanel>
          </FigmaUi.ResizablePanel>
        </FigmaUi.ResizablePanelGroup>
      </div>
    </ReactDnd.DndProvider>);
}