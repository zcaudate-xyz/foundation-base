import * as Lucide from 'lucide-react'

import * as ReactDnd from 'react-dnd'

import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'

// code.dev.client.app.components.outliner-panel/TreeNode [10] 
export function TreeNode({
  component,
  depth,
  selectedComponent,
  expandedNodes,
  hiddenNodes,
  onSelectComponent,
  onDeleteComponent,
  onMoveComponent,
  toggleExpanded,
  toggleVisibility
}){
  let isExpanded = expandedNodes.has(component.id);
  let isSelected = component.id == selectedComponent;
  let isHidden = hiddenNodes.has(component.id);
  let hasChildren = component.children.length > 0;
  let displayName = component.label || component.type;
  let [isDragging,drag] = ReactDnd.useDrag(function (){
    return {
      "type":"OUTLINER_ITEM",
      "item":{"id":component.id},
      "collect":function (monitor){
            return {"isDragging":monitor.isDragging()};
          }
    };
  });
  let [isOver,drop] = ReactDnd.useDrop(function (){
    return {
      "accept":"OUTLINER_ITEM",
      "drop":function (item,monitor){
            if(monitor.didDrop()){
              return;
            }
            if(item.id != component.id){
              onMoveComponent(item.id,component.id,"inside");
            }
          },
      "collect":function (monitor){
            return {"isOver":monitor.isOver({"shallow":true})};
          }
    };
  });
  return (
    <div>
      <div
        ref={function (node){
            drag(drop(node));
          }}
        className={"flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer group transition-colors " + (isSelected ? "bg-[#404040]" : "") + " " + (isOver ? "bg-[#2a3a2a]" : "") + " " + (isDragging ? "opacity-50" : "")}
        style={{"paddingLeft":(depth * 16) + 8 + "px"}}>
        {hasChildren ? (
          <button
            onClick={function (e){
                e.stopPropagation();
                toggleExpanded(component.id);
              }}
            className="p-0.5 hover:bg-[#404040] rounded">
            {isExpanded ? (
              <Lucide.ChevronDown className="w-3 h-3 text-gray-500"></Lucide.ChevronDown>) : (
              <Lucide.ChevronRight className="w-3 h-3 text-gray-500"></Lucide.ChevronRight>)}
          </button>) : (
          <div className="w-4"></div>)}
        <div
          className="flex-1 text-xs text-gray-300 flex items-center gap-1"
          onClick={function (){
              return onSelectComponent(component.id);
            }}>
          <span className={isHidden ? "text-gray-600" : ""}>{displayName}</span>
          {(component.label && (component.label != component.type)) ? (
            <span className="text-gray-600 text-[10px]">{"(" + component.type + ")"}</span>) : null}
        </div>
        <div
          className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <FigmaUi.Button
            size="sm"
            variant="ghost"
            className="h-5 w-5 p-0 hover:bg-[#404040]"
            onClick={function (e){
                return toggleVisibility(component.id,e);
              }}>
            {isHidden ? (
              <Lucide.EyeOff className="w-3 h-3 text-gray-600"></Lucide.EyeOff>) : (
              <Lucide.Eye className="w-3 h-3 text-gray-500"></Lucide.Eye>)}
          </FigmaUi.Button>
          {(component.id != "root") ? (
            <FigmaUi.Button
              size="sm"
              variant="ghost"
              className="h-5 w-5 p-0 hover:bg-[#404040]"
              onClick={function (e){
                  e.stopPropagation();
                  onDeleteComponent(component.id);
                }}>
              <Lucide.Trash2 className="w-3 h-3 text-red-500"></Lucide.Trash2>
            </FigmaUi.Button>) : null}
        </div>
      </div>
      {(hasChildren && isExpanded) ? (
        <div>
          {component.children.map(function (child){
            return (
              <TreeNode
                onDeleteComponent={onDeleteComponent}
                onSelectComponent={onSelectComponent}
                key={child.id}
                onMoveComponent={onMoveComponent}
                hiddenNodes={hiddenNodes}
                selectedComponent={selectedComponent}
                component={child}
                expandedNodes={expandedNodes}
                depth={depth + 1}
                toggleVisibility={toggleVisibility}
                toggleExpanded={toggleExpanded}>
              </TreeNode>);
          })}
        </div>) : null}
    </div>);
}

// code.dev.client.app.components.outliner-panel/OutlinerPanel [111] 
export function OutlinerPanel({
  components,
  selectedComponent,
  onSelectComponent,
  onDeleteComponent,
  onMoveComponent
}){
  let [expandedNodes,setExpandedNodes] = React.useState(new Set(["root"]));
  let [hiddenNodes,setHiddenNodes] = React.useState(new Set());
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
  let toggleVisibility = function (id,e){
    e.stopPropagation();
    setHiddenNodes(function (prev){
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
  return (
    <div className="flex flex-col h-full bg-[#252525]">
      <div
        className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3">
        <h3 className="text-xs text-gray-400 uppercase tracking-wide">Outliner</h3>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <div className="py-2">
          {components.map(function (component){
            return (
              <TreeNode
                onDeleteComponent={onDeleteComponent}
                onSelectComponent={onSelectComponent}
                key={component.id}
                onMoveComponent={onMoveComponent}
                hiddenNodes={hiddenNodes}
                selectedComponent={selectedComponent}
                component={component}
                expandedNodes={expandedNodes}
                depth={0}
                toggleVisibility={toggleVisibility}
                toggleExpanded={toggleExpanded}>
              </TreeNode>);
          })}
        </div>
      </FigmaUi.ScrollArea>
      <div
        className="h-8 bg-[#2b2b2b] border-t border-[#323232] flex items-center px-3">
        <span className="text-[10px] text-gray-600">
          {components.reduce(function (count,c){
            return count + (c.children.length + 1);
          },0) + " objects"}
        </span>
      </div>
    </div>);
}