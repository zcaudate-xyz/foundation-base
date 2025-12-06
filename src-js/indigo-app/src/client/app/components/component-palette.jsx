import * as Lucide from 'lucide-react'

import * as FigmaUi from '@xtalk/figma-ui'

// code.dev.client.app.components.component-palette/componentGroups [9] 
export var componentGroups = [
  {
  "name":"Layout (Tamagui)",
  "components":[
    {"color":"text-blue-500","type":"View","icon":Lucide.Square},
    {
    "color":"text-blue-600",
    "type":"XStack",
    "icon":Lucide.AlignHorizontalSpaceAround
  },
    {
    "color":"text-blue-700",
    "type":"YStack",
    "icon":Lucide.AlignVerticalSpaceAround
  },
    {"color":"text-purple-500","type":"Card","icon":Lucide.Layers}
  ]
},
  {
  "name":"Typography",
  "components":[
    {"color":"text-green-600","type":"Heading","icon":Lucide.Type},
    {"color":"text-gray-500","type":"Text","icon":Lucide.Type}
  ]
},
  {
  "name":"Form Elements",
  "components":[
    {
    "color":"text-red-500",
    "type":"Button",
    "icon":Lucide.MousePointer2
  },
    {"color":"text-orange-500","type":"Input","icon":Lucide.Type},
    {
    "color":"text-indigo-500",
    "type":"Checkbox",
    "icon":Lucide.CheckSquare
  },
    {
    "color":"text-teal-500",
    "type":"Switch",
    "icon":Lucide.ToggleLeft
  }
  ]
}
];

// code.dev.client.app.components.component-palette/ComponentPalette [44] 
export function ComponentPalette({onAddComponent}){
  return (
    <div className="flex flex-col h-full bg-white border-r">
      <div className="px-4 py-2 bg-gray-200 border-b"><h2 className="text-sm">Tamagui Components</h2></div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="p-3 space-y-4">
          {componentGroups.map(function (group){
            return (
              <div key={group.name}>
                <h3 className="text-xs text-gray-500 mb-2 px-1">{group.name}</h3>
                <div className="space-y-1">
                  {group.components.map(function (component){
                    let Icon = component.icon;
                    return (
                      <FigmaUi.Button
                        key={component.type}
                        variant="ghost"
                        className="w-full justify-start h-auto py-2 px-2"
                        onClick={function (){
                            return onAddComponent(component.type);
                          }}>
                        <Icon className={"w-4 h-4 mr-2 " + component.color}></Icon>
                        <span className="text-sm">{component.type}</span>
                      </FigmaUi.Button>);
                  })}
                </div>
                {(group != componentGroups[componentGroups.length - 1]) ? (
                  <FigmaUi.Separator className="mt-3"></FigmaUi.Separator>) : null}
              </div>);
          })}
        </div>
      </FigmaUi.ScrollArea>
      <div className="p-3 border-t bg-gray-50">
        <p className="text-xs text-gray-500">Click to add Tamagui components</p>
      </div>
    </div>);
}