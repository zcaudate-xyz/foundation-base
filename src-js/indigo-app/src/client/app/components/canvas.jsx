import * as FigmaUi from '@xtalk/figma-ui'

// code.dev.client.app.components.canvas/Canvas [8] 
export function Canvas({
  components = [],
  selectedComponent,
  onSelectComponent,
  onAddComponent
}){
  let renderComponent = function (component){
    let isSelected = component.id == selectedComponent;
    let baseClasses = isSelected ? "outline outline-2 outline-blue-500 outline-offset-2" : "hover:outline hover:outline-1 hover:outline-gray-300 hover:outline-offset-2";
    let handleClick = function (e){
      e.stopPropagation();
      onSelectComponent(component.id);
    };
    let renderChildren = function (){
      if(component.children.length == 0){
        return null;
      }
      else{
        return (
          <div className="space-y-2">
            {component.children.map(function (child){
              return (
                <div key={child.id}>{renderComponent(child)}</div>);
            })}
          </div>);
      }
    };
    let commonProps = {
      "onClick":handleClick,
      "className":baseClasses + " cursor-pointer transition-all"
    };
    let textContent = component.properties.children;
    switch(component.type){
      case "View":
        return (
          <div
            className={commonProps.className + " p-4 border rounded"}
            {...commonProps}>
            <div className="text-xs text-gray-400 mb-2">{component.type}</div>
            {renderChildren()}
          </div>);
      
      case "XStack":
        return (
          <div
            className={commonProps.className + " p-4 border rounded"}
            {...commonProps}>
            <div className="text-xs text-gray-400 mb-2">{component.type}</div>
            {renderChildren()}
          </div>);
      
      case "YStack":
        return (
          <div
            className={commonProps.className + " p-4 border rounded"}
            {...commonProps}>
            <div className="text-xs text-gray-400 mb-2">{component.type}</div>
            {renderChildren()}
          </div>);
      
      case "Card":
        return (
          <FigmaUi.Card {...commonProps}>
            <div className="text-xs text-gray-400 mb-2">{component.type}</div>
            {renderChildren()}
          </FigmaUi.Card>);
      
      case "Button":
        return (
          <div {...commonProps}>
            <FigmaUi.Button className="pointer-events-none">{textContent || "Button"}</FigmaUi.Button>
          </div>);
      
      case "Input":
        return (
          <div {...commonProps}>
            <FigmaUi.Input
              placeholder={component.properties.placeholder || "Enter text..."}
              className="pointer-events-none">
            </FigmaUi.Input>
          </div>);
      
      case "Checkbox":
        return (
          <div {...commonProps}>
            <div className="flex items-center gap-2">
              <FigmaUi.Checkbox className="pointer-events-none"></FigmaUi.Checkbox>
              <label className="text-sm">Checkbox</label>
            </div>
          </div>);
      
      case "Switch":
        return (
          <div {...commonProps}>
            <div className="flex items-center gap-2">
              <FigmaUi.Switch className="pointer-events-none"></FigmaUi.Switch>
              <label className="text-sm">Switch</label>
            </div>
          </div>);
      
      case "Text":
        return (
          <p {...commonProps}>{textContent || "Text content"}</p>);
      
      case "Heading":
        return (
          <h2 {...commonProps}>{textContent || "Heading"}</h2>);
      
      default:
        return (
          <div {...commonProps}><span className="text-gray-500">{component.type}</span></div>);
    }
  };
  return (
    <div className="flex flex-col h-full bg-white">
      <div
        className="px-4 py-2 bg-gray-200 border-b flex items-center justify-between">
        <h2 className="text-sm">Canvas (Preview)</h2>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">Tamagui Preview</span>
        </div>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="p-8 min-h-full bg-gray-50">
          <div className="bg-white rounded-lg shadow-sm p-8 min-h-[600px]">
            {components.map(function (component){
              return (
                <div key={component.id}>{renderComponent(component)}</div>);
            })}
          </div>
        </div>
      </FigmaUi.ScrollArea>
    </div>);
}