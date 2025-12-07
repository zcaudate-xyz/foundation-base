import React from 'react'
import * as cr from '@/client/app/components/canvas/component-renderer'
import * as et from '@/client/app/components/canvas/editing-toolbar'
import { useAppState } from '../../state'

// code.dev.client.app.components.viewport-canvas/countComponents
export function countComponents(comp) {
  return 1 + comp.children.reduce(function (acc, child) {
    return acc + countComponents(child);
  }, 0);
}

// code.dev.client.app.components.viewport-canvas/componentToJSON
export function componentToJSON(component) {
  if (!component) {
    return null;
  }
  if (component.libraryRef) {
    return {
      "type": component.libraryRef,
      "props": component.properties,
      "inputs": component.inputValues || null
    };
  }
  else {
    let result = { "type": component.type, "props": component.properties };
    if (component.inputs && (Object.keys(component.inputs).length > 0)) {
      result.inputSchema = component.inputs;
    }
    if (component.inputValues && (Object.keys(component.inputValues).length > 0)) {
      result.inputs = component.inputValues;
    }
    if (component.states && (Object.keys(component.states).length > 0)) {
      result.states = component.states;
    }
    if (component.triggers && (Object.keys(component.triggers).length > 0)) {
      result.triggers = component.triggers;
    }
    if (component.actions && (Object.keys(component.actions).length > 0)) {
      result.actions = component.actions;
    }
    if (component.children.length > 0) {
      result.children = component.children.map(componentToJSON);
    }
    return result;
  }
}

// code.dev.client.app.components.viewport-canvas/ViewportCanvas
export function ViewportCanvas() {
  const {
    components,
    selectedComponent,
    setSelectedComponent: onSelectComponent,
    addComponent: onAddComponent,
    moveComponent: onMoveComponent,
    viewMode,
    theme
  } = useAppState();
  let [componentStates, setComponentStates] = React.useState({});
  let [currentTool, setCurrentTool] = React.useState('select');

  React.useEffect(function () {
    let initStates = {};
    let collectStates = function (comp) {
      if (comp.states && (Object.keys(comp.states).length > 0)) {
        initStates[comp.id] = {};
        Object.entries(comp.states).forEach(function ([stateName, stateDef]) {
          initStates[comp.id][stateName] = stateDef.default;
        });
      }
      comp.children.forEach(collectStates);
    };
    components.forEach(collectStates);
    setComponentStates(initStates);
  }, [components]);

  let updateState = function (componentId, stateName, value) {
    setComponentStates(function (prev) {
      return {
        componentId: { stateName: value, ...prev[componentId] || {} },
        ...prev
      };
    });
  };

  let executeAction = function (component, actionName) {
    if (!component.actions || !component.actions[actionName]) {
      return;
    }
    let action = component.actions[actionName];
    let currentStates = componentStates[component.id] || {};
    switch (action.type) {
      case "toggleState":
        if (action.target) {
          let currentValue = currentStates[action.target];
          updateState(component.id, action.target, !currentValue);
        }

      case "setState":
        if (action.target && (action.value != undefined)) {
          let parsedValue = action.value;
          try {
            if (((typeof action.value) == "string") && ((action.value == "true") || (action.value == "false"))) {
              parsedValue = (action.value == "true");
            }
            if (((typeof action.value) == "string") && !isNaN(Number(action.value))) {
              parsedValue = Number(action.value);
            }
          }
          catch (e) {

          }
          updateState(component.id, action.target, parsedValue);
        }

      case "incrementState":
        if (action.target) {
          let currentValue = currentStates[action.target];
          if ((typeof currentValue) == "number") {
            updateState(component.id, action.target, currentValue + 1);
          }
        }

      case "customScript":
        if (action.script) {
          try {
            let context = {
              "state": currentStates,
              "setState": function (stateName, value) {
                return updateState(component.id, stateName, value);
              }
            };
            let func = new Function("state", "setState", action.script);
            func(context.state, context.setState);
          }
          catch (error) {
            console.error("Error executing custom script:", error);
          }
        }
    }
  };

  let generateThemeStyles = function () {
    if (!theme) {
      return {};
    }
    return {
      "--color-primary": theme.colors.primary,
      "--color-secondary": theme.colors.secondary,
      "--color-accent": theme.colors.accent,
      "--color-background": theme.colors.background,
      "--color-text": theme.colors.text
    };
  };

  return (
    <div className="flex flex-col h-full bg-[#1a1a1a]">
      <et.EditingToolbar currentTool={currentTool} onToolChange={setCurrentTool} />
      <div
        className="flex-1 overflow-auto p-8"
        style={Object.assign({
          "background": "radial-gradient(circle at 20px 20px, #2a2a2a 1px, transparent 1px)",
          "backgroundSize": "40px 40px"
        }, generateThemeStyles())}>
        <div className="min-h-full">
          {components.map(function (component) {
            return (
              <cr.ComponentRenderer
                key={component.id}
                component={component}
                selectedComponent={selectedComponent}
                onSelectComponent={onSelectComponent}
                onDropComponent={onAddComponent}
                theme={theme}
                componentStates={componentStates}
                executeAction={executeAction}>
              </cr.ComponentRenderer>);
          })}
        </div>
      </div>

    </div>);
}