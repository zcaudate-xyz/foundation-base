import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'

import * as Lucide from 'lucide-react'

// code.dev.client.app.components.hierarchy-tree/HierarchyTree [9] 
export function HierarchyTree({
  components = [],
  selectedComponent,
  onSelectComponent,
  onDeleteComponent
}){
  let [expandedNodes,setExpandedNodes] = React.useState(new Set(["root"]));
  let toggleExpanded = function (id){
    setExpandedNodes(function (prev){
      let next = new Set(prev);
      if(next.has(id)){
        next.delete(id);
      }
      else{
        next.add(id);
      }
      return next;
    });
  };
  let renderTreeNode = function (component,depth = 0){
    let isExpanded = expandedNodes.has(component.id);
    let isSelected = component.id == selectedComponent;
    let hasChildren = component.children.length > 0;
    return (
      <div key={component.id}>
        <div
          className={"flex items-center gap-1 py-1 px-2 hover:bg-gray-100 cursor-pointer group " + (isSelected ? "bg-blue-100" : "")}
          style={{"paddingLeft":(depth * 16) + 8 + "px"}}>
          {hasChildren ? (
            <button
              onClick={function (e){
                  e.stopPropagation();
                  toggleExpanded(component.id);
                }}
              className="p-0.5 hover:bg-gray-200 rounded">
              {isExpanded ? (
                <Lucide.ChevronDown className="w-3 h-3"></Lucide.ChevronDown>) : (
                <Lucide.ChevronRight className="w-3 h-3"></Lucide.ChevronRight>)}
            </button>) : (
            <div className="w-4"></div>)}
          <div
            className="flex-1 text-sm"
            onClick={function (){
                return onSelectComponent(component.id);
              }}>
            <span className="text-gray-600">{component.type}</span>
            <span className="text-gray-400 text-xs ml-2">{"#" + component.id.split("-")[1]}</span>
          </div>
          {(component.id != "root") ? (
            <FigmaUi.Button
              size="sm"
              variant="ghost"
              className="opacity-0 group-hover:opacity-100 h-6 w-6 p-0"
              onClick={function (e){
                  e.stopPropagation();
                  onDeleteComponent(component.id);
                }}>
              <Lucide.Trash2 className="w-3 h-3 text-red-500"></Lucide.Trash2>
            </FigmaUi.Button>) : null}
        </div>
        {(hasChildren && isExpanded) ? (
          <div>
            {component.children.map(function (child){
              return renderTreeNode(child,depth + 1);
            })}
          </div>) : null}
      </div>);
  };
  return (
    <div className="flex flex-col h-full bg-white">
      <div className="px-4 py-2 bg-gray-100 border-b"><h3 className="text-sm">Component Hierarchy</h3></div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="py-2">
          {components.map(function (component){
            return renderTreeNode(component);
          })}
        </div>
      </FigmaUi.ScrollArea>
    </div>);
}