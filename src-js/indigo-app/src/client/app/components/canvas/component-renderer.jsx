// code.dev.client.app.components.component-renderer/resolveInputBindings [10] 
export function resolveInputBindings(value, inputValues = {}) {
  if (!((typeof value) == "string")) {
    return value;
  }
  let resolved = value;
  let bracketPattern = /\{inputs?\.(\w+)\}/g;
  resolved = resolved.replace(bracketPattern, function (match, inputName) {
    return (inputValues[inputName] != undefined) ? String(inputValues[inputName]) : match;
  });
  let dollarPattern = /\{$inputs?\.(\w+)\}/g;
  resolved = resolved.replace(dollarPattern, function (match, inputName) {
    return (inputValues[inputName] != undefined) ? String(inputValues[inputName]) : match;
  });
  return resolved;
}

// code.dev.client.app.components.component-renderer/resolveStateBindings [36] 
export function resolveStateBindings(value, stateValues = {}) {
  if (!((typeof value) == "string")) {
    return value;
  }
  let resolved = value;
  let bracketPattern = /\{states?\.(\w+)\}/g;
  resolved = resolved.replace(bracketPattern, function (match, stateName) {
    return (stateValues[stateName] != undefined) ? String(stateValues[stateName]) : match;
  });
  let dollarPattern = /\{$states?\.(\w+)\}/g;
  resolved = resolved.replace(dollarPattern, function (match, stateName) {
    return (stateValues[stateName] != undefined) ? String(stateValues[stateName]) : match;
  });
  return resolved;
}

// code.dev.client.app.components.component-renderer/ComponentRenderer [62] 
export function ComponentRenderer({
  component,
  selectedComponent,
  onSelectComponent,
  onDropComponent,
  theme,
  componentStates,
  executeAction
}) {
  let resolveComponentBindings = function (comp, parentInputs = null) {
    let inputsToUse = comp.inputValues || parentInputs || {};
    let statesToUse = componentStates[comp.id] || {};
    let resolvedProperties = { ...comp.properties };
    Object.keys(resolvedProperties).forEach(function (key) {
      resolvedProperties[key] = resolveInputBindings(resolvedProperties[key], inputsToUse);
      resolvedProperties[key] = resolveStateBindings(resolvedProperties[key], statesToUse);
    });
    return {
      "properties": resolvedProperties,
      "children": comp.children.map(function (child) {
        return resolveComponentBindings(child, inputsToUse);
      }),
      ...comp
    };
  };
  let resolvedComponent = resolveComponentBindings(component);
  let isSelected = component.id === selectedComponent;
  let resolvedProperties = resolvedComponent.properties;
  let textContent = resolvedProperties.children;
  let displayLabel = component.label || component.type;
  let createEventHandlers = function () {
    let handlers = {};
    if (component.triggers && component.actions) {
      Object.entries(component.triggers).forEach(function ([triggerName, triggerDef]) {
        let eventName = "on" + triggerDef.event.charAt(0).toUpperCase() + triggerDef.event.slice(1);
        handlers[eventName] = (function (e) {
          e.stopPropagation();
          if (triggerDef.action) {
            executeAction(component, triggerDef.action);
          }
        });
      });
    }
    return handlers;
  };
  let eventHandlers = createEventHandlers();
  let renderChildren = function () {
    component.children.map(function (child) {
      return (
        <ComponentRenderer
          key={child.id}
          component={child}
          selectedComponent={selectedComponent}
          onSelectComponent={onSelectComponent}
          onDropComponent={onDropComponent}
          theme={theme}
          componentStates={componentStates}
          executeAction={executeAction}>
        </ComponentRenderer>);
    });
  };
  let commonProps = {
    "onClick": function (e) {
      e.stopPropagation();
      onSelectComponent(component.id);
    },
    "className": isSelected ? "ring-2 ring-blue-500" : ""
  };
  let allProps = { ...commonProps, ...eventHandlers };
  switch (component.type) {
    case "Container":
      null;

    case "FlexRow":
      null;

    case "FlexCol":
      return (
        <div
          className={allProps.className + " " + (resolvedProperties.className || "p-4 border border-border rounded bg-card text-card-foreground")}
          {...allProps}>
          <div className="text-[10px] text-muted-foreground mb-2">{displayLabel}</div>
          {renderChildren()}
          {(component.children.length == 0) ? (
            <div className="text-[10px] text-muted-foreground italic py-2">Drop components here</div>) : null}
        </div>);

    case "Card":
      return (
        <div
          className={allProps.className + " " + (resolvedProperties.className || "p-6 bg-card rounded-lg shadow border border-border text-card-foreground")}
          {...allProps}>
          <div className="text-[10px] text-muted-foreground mb-2">{displayLabel}</div>
          {renderChildren()}
          {(component.children.length == 0) ? (
            <div className="text-[10px] text-muted-foreground italic py-2">Drop components here</div>) : null}
        </div>);

    case "Button":
      return (
        <button
          className={allProps.className + " " + (resolvedProperties.className || "px-4 py-2 bg-primary text-primary-foreground rounded hover:bg-primary/90 transition-colors")}
          {...allProps}>{textContent || "Button"}
        </button>);

    case "Text":
      return (
        <p
          className={allProps.className + " " + (resolvedProperties.className || "text-muted-foreground")}
          {...allProps}>{textContent || "Text content"}
        </p>);

    case "Heading":
      return (
        <h2
          className={allProps.className + " " + (resolvedProperties.className || "text-2xl font-bold text-foreground")}
          {...allProps}>{textContent || "Heading"}
        </h2>);

    case "Image":
      return (
        <img
          src={resolvedProperties.src || "https://via.placeholder.com/400x300"}
          alt={resolvedProperties.alt || "Image"}
          className={allProps.className + " " + (resolvedProperties.className || "w-full h-auto")}
          {...allProps}>
        </img>);

    case "Input":
      return (
        <input
          type={resolvedProperties.type || "text"}
          placeholder={resolvedProperties.placeholder || "Enter text..."}
          className={allProps.className + " " + (resolvedProperties.className || "px-3 py-2 border border-input rounded bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring")}
          {...allProps}>
        </input>);

    case "View":
      return (
        <div
          className={allProps.className + " " + (resolvedProperties.className || "")}
          {...allProps}>{renderChildren()}
        </div>);

    default:
      return (
        <div
          className={allProps.className + " text-muted-foreground"}
          {...allProps}><span>{displayLabel}</span>
        </div>);
  }
}