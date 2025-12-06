import * as FigmaUi from '@xtalk/figma-ui'

import * as Lucide from 'lucide-react'

import React from 'react'

// code.dev.client.app.components.states-triggers-panel/StatesTab [9] 
export function StatesTab({component,onUpdateStates}){
  let [newStateName,setNewStateName] = React.useState("");
  let [newStateType,setNewStateType] = React.useState("boolean");
  let handleAddState = function (){
    if(!newStateName.trim()){
      return;
    }
    let defaultValue = (newStateType == "boolean") ? false : ((newStateType == "number") ? 0 : ((newStateType == "array") ? [] : ((newStateType == "object") ? {} : "")));
    let updatedStates = Object.assign({},component.states || {},{
      [newStateName]:{"description":"","default":defaultValue,"type":newStateType}
    });
    onUpdateStates(component.id,updatedStates);
    setNewStateName("");
    setNewStateType("boolean");
  };
  let handleRemoveState = function (stateName){
    let updatedStates = Object.assign({},component.states || {});
    delete updatedStates[stateName];
    onUpdateStates(component.id,updatedStates);
  };
  let handleUpdateStateDescription = function (stateName,description){
    let updatedStates = Object.assign({},component.states || {},{
      [stateName]:Object.assign({},component.states[stateName],{"description":description})
    });
    onUpdateStates(component.id,updatedStates);
  };
  let handleUpdateStateDefault = function (stateName,defaultValue){
    let updatedStates = Object.assign({},component.states || {},{
      [stateName]:Object.assign({},component.states[stateName],{"default":defaultValue})
    });
    onUpdateStates(component.id,updatedStates);
  };
  return (
    <FigmaUi.ScrollArea className="h-full">
      <div className="p-4 space-y-4">
        <div
          className="p-3 bg-purple-950/30 border border-purple-900/50 rounded">
          <p className="text-xs text-purple-300 mb-1">💡 Component States</p>
          <p className="text-[10px] text-purple-400/80">
            Define reactive state variables. Use 
            <code className="bg-purple-900/30 px-1 rounded">{state.name}</code>
             or 
            <code className="bg-purple-900/30 px-1 rounded">$state.name</code>
             in properties to bind to state values.
          </p>
        </div>
        <div>
          <h3
            className="text-xs text-gray-500 uppercase tracking-wider mb-3">Defined States
          </h3>
          {(component.states && (Object.keys(component.states).length > 0)) ? (
            <div className="space-y-3">
              {Object.entries(component.states).map(function ([stateName,stateDef]){
                return (
                  <div
                    key={stateName}
                    className="p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-xs font-mono text-purple-400">{stateName}</span>
                          <span className="text-xs text-gray-500">{": " + stateDef.type}</span>
                        </div>
                        <FigmaUi.Input
                          type="text"
                          placeholder="Description (optional)"
                          value={stateDef.description || ""}
                          onChange={function (e){
                              return handleUpdateStateDescription(stateName,e.target.value);
                            }}
                          className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2">
                        </FigmaUi.Input>
                        <div>
                          <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">Default Value</FigmaUi.Label>
                          {(stateDef.type == "boolean") ? (
                            <select
                              value={String.stateDef.default}
                              onChange={function (e){
                                  return handleUpdateStateDefault(stateName,e.target.value == "true");
                                }}
                              className="w-full h-6 bg-[#252525] border border-[#3a3a3a] text-gray-300 text-xs rounded px-2">
                              <option value="false">false</option>
                              <option value="true">true</option>
                            </select>) : ((stateDef.type == "number") ? (
                            <FigmaUi.Input
                              type="number"
                              value={stateDef.default}
                              onChange={function (e){
                                  return handleUpdateStateDefault(stateName,parseFloat(e.target.value));
                                }}
                              className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
                            </FigmaUi.Input>) : (
                            <FigmaUi.Input
                              type="text"
                              value={((typeof stateDef.default) == "string") ? stateDef.default : JSON.stringify(stateDef.default)}
                              onChange={function (e){
                                  try{
                                    let parsed = JSON.parse(e.target.value);
                                    handleUpdateStateDefault(stateName,parsed);
                                  }
                                  catch(_){
                                    handleUpdateStateDefault(stateName,e.target.value);
                                  }
                                }}
                              className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
                            </FigmaUi.Input>))}
                        </div>
                      </div>
                    </div>
                    <FigmaUi.Button
                      variant="ghost"
                      size="sm"
                      onClick={function (){
                          return handleRemoveState(stateName);
                        }}
                      className="h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"><Lucide.Trash2 className="w-3 h-3"></Lucide.Trash2>
                    </FigmaUi.Button>
                  </div>);
              })}
            </div>) : (
            <p className="text-xs text-gray-500 italic">No states defined</p>)}
          <div
            className="mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
            <FigmaUi.Label className="text-xs text-gray-400 mb-2 block">Add State</FigmaUi.Label>
            <div className="flex gap-2">
              <FigmaUi.Input
                type="text"
                placeholder="State name"
                value={newStateName}
                onChange={function (e){
                    return setNewStateName(e.target.value);
                  }}
                onKeyDown={function (e){
                    return (e.key == "Enter") ? handleAddState() : null;
                  }}
                className="flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
              </FigmaUi.Input>
              <FigmaUi.Select
                value={newStateType}
                onValueChange={function (v){
                    return setNewStateType(v);
                  }}>
                <FigmaUi.SelectTrigger
                  className="w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"><FigmaUi.SelectValue></FigmaUi.SelectValue>
                </FigmaUi.SelectTrigger>
                <FigmaUi.SelectContent>
                  <FigmaUi.SelectItem value="boolean">boolean</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="string">string</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="number">number</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="object">object</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="array">array</FigmaUi.SelectItem>
                </FigmaUi.SelectContent>
              </FigmaUi.Select>
              <FigmaUi.Button
                size="sm"
                onClick={handleAddState}
                className="h-7 px-3 bg-purple-600 hover:bg-purple-700 text-white"><Lucide.Plus className="w-3 h-3"></Lucide.Plus>
              </FigmaUi.Button>
            </div>
          </div>
        </div>
      </div>
    </FigmaUi.ScrollArea>);
}

// code.dev.client.app.components.states-triggers-panel/TriggersTab [144] 
export function TriggersTab({component,onUpdateTriggers}){
  let [newTriggerName,setNewTriggerName] = React.useState("");
  let [newTriggerEvent,setNewTriggerEvent] = React.useState("click");
  let handleAddTrigger = function (){
    if(!newTriggerName.trim()){
      return;
    }
    let updatedTriggers = Object.assign({},component.triggers || {},{
      [newTriggerName]:{"description":"","event":newTriggerEvent,"action":""}
    });
    onUpdateTriggers(component.id,updatedTriggers);
    setNewTriggerName("");
    setNewTriggerEvent("click");
  };
  let handleRemoveTrigger = function (triggerName){
    let updatedTriggers = Object.assign({},component.triggers || {});
    delete updatedTriggers[triggerName];
    onUpdateTriggers(component.id,updatedTriggers);
  };
  let handleUpdateTrigger = function (triggerName,field,value){
    let updatedTriggers = Object.assign({},component.triggers || {},{
      [triggerName]:Object.assign({},component.triggers[triggerName],{[field]:value})
    });
    onUpdateTriggers(component.id,updatedTriggers);
  };
  let availableActions = component.actions ? Object.keys(component.actions) : [];
  return (
    <FigmaUi.ScrollArea className="h-full">
      <div className="p-4 space-y-4">
        <div
          className="p-3 bg-yellow-950/30 border border-yellow-900/50 rounded">
          <p className="text-xs text-yellow-300 mb-1">⚡ Event Triggers</p>
          <p className="text-[10px] text-yellow-400/80">
            Define event handlers that execute actions when events occur (click, change, submit, etc.)
          </p>
        </div>
        <div>
          <h3
            className="text-xs text-gray-500 uppercase tracking-wider mb-3">Defined Triggers
          </h3>
          {(component.triggers && (Object.keys(component.triggers).length > 0)) ? (
            <div className="space-y-3">
              {Object.entries(component.triggers).map(function ([triggerName,triggerDef]){
                return (
                  <div
                    key={triggerName}
                    className="p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-xs font-mono text-yellow-400">{triggerName}</span>
                          <span className="text-xs text-gray-500">{"on " + triggerDef.event}</span>
                        </div>
                        <FigmaUi.Input
                          type="text"
                          placeholder="Description (optional)"
                          value={triggerDef.description || ""}
                          onChange={function (e){
                              return handleUpdateTrigger(triggerName,"description",e.target.value);
                            }}
                          className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2">
                        </FigmaUi.Input>
                        <div className="space-y-2">
                          <div>
                            <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">Event</FigmaUi.Label>
                            <FigmaUi.Select
                              value={triggerDef.event}
                              onValueChange={function (v){
                                  return handleUpdateTrigger(triggerName,"event",v);
                                }}>
                              <FigmaUi.SelectTrigger
                                className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"><FigmaUi.SelectValue></FigmaUi.SelectValue>
                              </FigmaUi.SelectTrigger>
                              <FigmaUi.SelectContent>
                                <FigmaUi.SelectItem value="click">click</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="change">change</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="submit">submit</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="mouseenter">mouseenter</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="mouseleave">mouseleave</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="focus">focus</FigmaUi.SelectItem>
                                <FigmaUi.SelectItem value="blur">blur</FigmaUi.SelectItem>
                              </FigmaUi.SelectContent>
                            </FigmaUi.Select>
                          </div>
                          <div>
                            <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">Action to Execute</FigmaUi.Label>
                            <FigmaUi.Select
                              value={triggerDef.action}
                              onValueChange={function (v){
                                  return handleUpdateTrigger(triggerName,"action",v);
                                }}>
                              <FigmaUi.SelectTrigger
                                className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
                                <FigmaUi.SelectValue placeholder="Select action..."></FigmaUi.SelectValue>
                              </FigmaUi.SelectTrigger>
                              <FigmaUi.SelectContent>
                                {(availableActions.length > 0) ? availableActions.map(function (actionName){
                                  return (
                                    <FigmaUi.SelectItem key={actionName} value={actionName}>{actionName}</FigmaUi.SelectItem>);
                                }) : (
                                  <FigmaUi.SelectItem value="_none" disabled={true}>No actions defined</FigmaUi.SelectItem>)}
                              </FigmaUi.SelectContent>
                            </FigmaUi.Select>
                          </div>
                        </div>
                      </div>
                    </div>
                    <FigmaUi.Button
                      variant="ghost"
                      size="sm"
                      onClick={function (){
                          return handleRemoveTrigger(triggerName);
                        }}
                      className="h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"><Lucide.Trash2 className="w-3 h-3"></Lucide.Trash2>
                    </FigmaUi.Button>
                  </div>);
              })}
            </div>) : (
            <p className="text-xs text-gray-500 italic">No triggers defined</p>)}
          <div
            className="mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
            <FigmaUi.Label className="text-xs text-gray-400 mb-2 block">Add Trigger</FigmaUi.Label>
            <div className="flex gap-2">
              <FigmaUi.Input
                type="text"
                placeholder="Trigger name"
                value={newTriggerName}
                onChange={function (e){
                    return setNewTriggerName(e.target.value);
                  }}
                onKeyDown={function (e){
                    return (e.key == "Enter") ? handleAddTrigger() : null;
                  }}
                className="flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
              </FigmaUi.Input>
              <FigmaUi.Select value={newTriggerEvent} onValueChange={setNewTriggerEvent}>
                <FigmaUi.SelectTrigger
                  className="w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"><FigmaUi.SelectValue></FigmaUi.SelectValue>
                </FigmaUi.SelectTrigger>
                <FigmaUi.SelectContent>
                  <FigmaUi.SelectItem value="click">click</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="change">change</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="submit">submit</FigmaUi.SelectItem>
                </FigmaUi.SelectContent>
              </FigmaUi.Select>
              <FigmaUi.Button
                size="sm"
                onClick={handleAddTrigger}
                className="h-7 px-3 bg-yellow-600 hover:bg-yellow-700 text-white"><Lucide.Plus className="w-3 h-3"></Lucide.Plus>
              </FigmaUi.Button>
            </div>
          </div>
        </div>
      </div>
    </FigmaUi.ScrollArea>);
}

// code.dev.client.app.components.states-triggers-panel/ActionsTab [271] 
export function ActionsTab({component,onUpdateActions}){
  let [newActionName,setNewActionName] = React.useState("");
  let [newActionType,setNewActionType] = React.useState("toggleState");
  let handleAddAction = function (){
    if(!newActionName.trim()){
      return;
    }
    let updatedActions = Object.assign({},component.actions || {},{
      [newActionName]:{
            "description":"",
            "script":"",
            "value":"",
            "type":newActionType,
            "target":""
          }
    });
    onUpdateActions(component.id,updatedActions);
    setNewActionName("");
    setNewActionType("toggleState");
  };
  let handleRemoveAction = function (actionName){
    let updatedActions = Object.assign({},component.actions || {});
    delete updatedActions[actionName];
    onUpdateActions(component.id,updatedActions);
  };
  let handleUpdateAction = function (actionName,field,value){
    let updatedActions = Object.assign({},component.actions || {},{
      [actionName]:Object.assign({},component.actions[actionName],{[field]:value})
    });
    onUpdateActions(component.id,updatedActions);
  };
  let availableStates = component.states ? Object.keys(component.states) : [];
  return (
    <FigmaUi.ScrollArea className="h-full">
      <div className="p-4 space-y-4">
        <div
          className="p-3 bg-green-950/30 border border-green-900/50 rounded">
          <p className="text-xs text-green-300 mb-1">🎬 Actions</p>
          <p className="text-[10px] text-green-400/80">
            Define actions that modify state or execute custom logic. Link actions to triggers.
          </p>
        </div>
        <div>
          <h3
            className="text-xs text-gray-500 uppercase tracking-wider mb-3">Defined Actions
          </h3>
          {(component.actions && (Object.keys(component.actions).length > 0)) ? (
            <div className="space-y-3">
              {Object.entries(component.actions).map(function ([actionName,actionDef]){
                return (
                  <div
                    key={actionName}
                    className="p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-xs font-mono text-green-400">{actionName}</span>
                          <span className="text-xs text-gray-500">{actionDef.type}</span>
                        </div>
                        <FigmaUi.Input
                          type="text"
                          placeholder="Description (optional)"
                          value={actionDef.description || ""}
                          onChange={function (e){
                              return handleUpdateAction(actionName,"description",e.target.value);
                            }}
                          className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2">
                        </FigmaUi.Input>
                        {((actionDef.type == "setState") || (actionDef.type == "toggleState") || (actionDef.type == "incrementState")) ? (
                          <div className="mb-2">
                            <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">Target State</FigmaUi.Label>
                            <FigmaUi.Select
                              value={actionDef.target || ""}
                              onValueChange={function (v){
                                  return handleUpdateAction(actionName,"target",v);
                                }}>
                              <FigmaUi.SelectTrigger
                                className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
                                <FigmaUi.SelectValue placeholder="Select state..."></FigmaUi.SelectValue>
                              </FigmaUi.SelectTrigger>
                              <FigmaUi.SelectContent>
                                {(availableStates.length > 0) ? availableStates.map(function (stateName){
                                  return (
                                    <FigmaUi.SelectItem key={stateName} value={stateName}>{stateName}</FigmaUi.SelectItem>);
                                }) : (
                                  <FigmaUi.SelectItem value="_none" disabled={true}>No states defined</FigmaUi.SelectItem>)}
                              </FigmaUi.SelectContent>
                            </FigmaUi.Select>
                          </div>) : null}
                        {null}
                      </div>
                      {(actionDef.type == "setState") ? (
                        <div>
                          <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">Value</FigmaUi.Label>
                          <FigmaUi.Input
                            type="text"
                            placeholder="Value to set"
                            value={actionDef.value || ""}
                            onChange={function (e){
                                return handleUpdateAction(actionName,"value",e.target.value);
                              }}
                            className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
                          </FigmaUi.Input>
                        </div>) : null}
                      {(actionDef.type == "customScript") ? (
                        <div>
                          <FigmaUi.Label className="text-[10px] text-gray-500 mb-1 block">JavaScript Code</FigmaUi.Label>
                          <FigmaUi.Textarea
                            placeholder="// Custom JavaScript code"
                            value={actionDef.script || ""}
                            onChange={function (e){
                                return handleUpdateAction(actionName,"script",e.target.value);
                              }}
                            className="min-h-[60px] bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs font-mono">
                          </FigmaUi.Textarea>
                        </div>) : null}
                    </div>
                  </div>), (
                  <FigmaUi.Button
                    variant="ghost"
                    size="sm"
                    onClick={function (){
                        return handleRemoveAction(actionName);
                      }}
                    className="h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"><Lucide.Trash2 className="w-3 h-3"></Lucide.Trash2>
                  </FigmaUi.Button>);
              })}
            </div>) : null}
          <p className="text-xs text-gray-500 italic">No actions defined</p>
        </div>
        <div
          className="mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
          <FigmaUi.Label className="text-xs text-gray-400 mb-2 block">Add Action</FigmaUi.Label>
          <div className="flex gap-2">
            <FigmaUi.Input
              type="text"
              placeholder="Action name"
              value={newActionName}
              onChange={function (e){
                  return setNewActionName(e.target.value);
                }}
              onKeyDown={function (e){
                  return (e.key == "Enter") ? handleAddAction() : null;
                }}
              className="flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
            </FigmaUi.Input>
            <FigmaUi.Select
              value={newActionType}
              onValueChange={function (v){
                  return setNewActionType(v);
                }}>
              <FigmaUi.SelectTrigger
                className="w-32 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"><FigmaUi.SelectValue></FigmaUi.SelectValue>
              </FigmaUi.SelectTrigger>
              <FigmaUi.SelectContent>
                <FigmaUi.SelectItem value="toggleState">Toggle State</FigmaUi.SelectItem>
                <FigmaUi.SelectItem value="setState">Set State</FigmaUi.SelectItem>
                <FigmaUi.SelectItem value="incrementState">Increment</FigmaUi.SelectItem>
                <FigmaUi.SelectItem value="customScript">Custom Script</FigmaUi.SelectItem>
              </FigmaUi.SelectContent>
            </FigmaUi.Select>
            <FigmaUi.Button
              size="sm"
              onClick={handleAddAction}
              className="h-7 px-3 bg-green-600 hover:bg-green-700 text-white"><Lucide.Plus className="w-3 h-3"></Lucide.Plus>
            </FigmaUi.Button>
          </div>
        </div>
      </div>
    </FigmaUi.ScrollArea>);
}

// code.dev.client.app.components.states-triggers-panel/StatesTriggersPanel [408] 
export function StatesTriggersPanel({component,onUpdateStates,onUpdateTriggers,onUpdateActions}){
  if(!component){
    return (
      <div
        className="flex-1 flex items-center justify-center text-gray-500 text-sm">No component selected
      </div>);
  }
  return (
    <FigmaUi.Tabs defaultValue="states" className="flex flex-col h-full">
      <div className="bg-[#2b2b2b] border-b border-[#323232]">
        <FigmaUi.TabsList
          className="w-full justify-start rounded-none bg-transparent border-b-0 h-9">
          <FigmaUi.TabsTrigger
            value="states"
            className="rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200">
            <Lucide.Database className="w-3 h-3 mr-1"></Lucide.Database>
            States
          </FigmaUi.TabsTrigger>
          <FigmaUi.TabsTrigger
            value="triggers"
            className="rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"><Lucide.Zap className="w-3 h-3 mr-1"></Lucide.Zap>Triggers
          </FigmaUi.TabsTrigger>
          <FigmaUi.TabsTrigger
            value="actions"
            className="rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200">Actions
          </FigmaUi.TabsTrigger>
        </FigmaUi.TabsList>
      </div>
      <FigmaUi.TabsContent value="states" className="flex-1 m-0 overflow-hidden">
        <StatesTab component={component} onUpdateStates={onUpdateStates}></StatesTab>
      </FigmaUi.TabsContent>
      <FigmaUi.TabsContent value="triggers" className="flex-1 m-0 overflow-hidden">
        <TriggersTab component={component} onUpdateTriggers={onUpdateTriggers}></TriggersTab>
      </FigmaUi.TabsContent>
      <FigmaUi.TabsContent value="actions" className="flex-1 m-0 overflow-hidden">
        <ActionsTab component={component} onUpdateActions={onUpdateActions}></ActionsTab>
      </FigmaUi.TabsContent>
    </FigmaUi.Tabs>);
}