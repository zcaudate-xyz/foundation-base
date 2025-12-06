import * as FigmaUi from '@xtalk/figma-ui'

import * as Lucide from 'lucide-react'

// code.dev.client.app.components.properties-inspector/PropertiesInspector [9] 
export function PropertiesInspector({component,onUpdateProperty,onDeleteComponent}){
  if(!component){
    return (
      <div className="flex flex-col h-full bg-white border-l">
        <div className="px-4 py-2 bg-gray-200 border-b"><h2 className="text-sm">Properties</h2></div>
        <div className="flex-1 flex items-center justify-center">
          <p className="text-sm text-gray-400">No component selected</p>
        </div>
      </div>);
  }
  let renderPropertyEditor = function (key,value){
    let handleChange = function (newValue){
      onUpdateProperty(component.id,key,newValue);
    };
    if(key == "size"){
      return (
        <FigmaUi.Select value={value} onValueChange={handleChange}>
          <FigmaUi.SelectTrigger><FigmaUi.SelectValue></FigmaUi.SelectValue></FigmaUi.SelectTrigger>
          <FigmaUi.SelectContent>
            <FigmaUi.SelectItem value="$1">$1 (xs)</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$2">$2 (sm)</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$3">$3 (md)</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$4">$4 (lg)</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$5">$5 (xl)</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$6">$6 (2xl)</FigmaUi.SelectItem>
          </FigmaUi.SelectContent>
        </FigmaUi.Select>);
    }
    if((key == "padding") || (key == "margin") || (key == "gap")){
      return (
        <FigmaUi.Select value={value} onValueChange={handleChange}>
          <FigmaUi.SelectTrigger><FigmaUi.SelectValue></FigmaUi.SelectValue></FigmaUi.SelectTrigger>
          <FigmaUi.SelectContent>
            <FigmaUi.SelectItem value="$0">$0</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$1">$1</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$2">$2</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$3">$3</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$4">$4</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$5">$5</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$6">$6</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$8">$8</FigmaUi.SelectItem>
          </FigmaUi.SelectContent>
        </FigmaUi.Select>);
    }
    if((key == "backgroundColor") || (key == "color")){
      return (
        <FigmaUi.Select value={value} onValueChange={handleChange}>
          <FigmaUi.SelectTrigger><FigmaUi.SelectValue></FigmaUi.SelectValue></FigmaUi.SelectTrigger>
          <FigmaUi.SelectContent>
            <FigmaUi.SelectItem value="$background">$background</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$color">$color</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$borderColor">$borderColor</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$blue9">$blue9</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$red9">$red9</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$green9">$green9</FigmaUi.SelectItem>
            <FigmaUi.SelectItem value="$gray1">$gray1</FigmaUi.SelectItem>
          </FigmaUi.SelectContent>
        </FigmaUi.Select>);
    }
  };
  return (
    <FigmaUi.Input
      value={value || ""}
      onChange={function (e){
          return handleChange(e.target.value);
        }}
      className="text-sm">
    </FigmaUi.Input>);
  return (
    <div className="flex flex-col h-full bg-white border-l">
      <div className="px-4 py-2 bg-gray-200 border-b"><h2 className="text-sm">Properties</h2></div>
      <div className="p-4 border-b bg-gray-50">
        <div className="flex items-center justify-between mb-2">
          <div>
            <p className="text-sm">{"tm/" + component.type}</p>
            <p className="text-xs text-gray-500">{"#" + component.id}</p>
          </div>
          {(component.id != "root") ? (
            <FigmaUi.Button
              size="sm"
              variant="ghost"
              onClick={function (){
                  return onDeleteComponent(component.id);
                }}>
              <Lucide.Trash2 className="w-4 h-4 text-red-500"></Lucide.Trash2>
            </FigmaUi.Button>) : null}
        </div>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="p-4 space-y-4">
          <div>
            <h3 className="text-xs text-gray-500 mb-3">Tamagui Properties</h3>
            <div className="space-y-3">
              {Object.entries(component.properties).map(function ([key,value]){
                return (
                  <div key={key} className="space-y-1">
                    <FigmaUi.Label className="text-xs capitalize">{key.replace(/\B([A-Z])/," $1").trim()}</FigmaUi.Label>
                    {renderPropertyEditor(key,value)}
                  </div>);
              })}
            </div>
          </div>
        </div>
        <FigmaUi.Separator></FigmaUi.Separator>
        <div>
          <h3 className="text-xs text-gray-500 mb-2">Component Info</h3>
          <div className="text-xs space-y-1 text-gray-600">
            <p>{"Children: " + component.children.length}</p>
            <p>{"Type: Tamagui " + component.type}</p>
          </div>
        </div>
      </FigmaUi.ScrollArea>
    </div>);
}