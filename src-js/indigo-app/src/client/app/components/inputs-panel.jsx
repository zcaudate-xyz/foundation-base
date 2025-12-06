import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'

import * as Lucide from 'lucide-react'

// code.dev.client.app.components.inputs-panel/InputsPanel [9] 
export function InputsPanel({component,onUpdateInputs,onUpdateInputValues}){
  let [newInputName,setNewInputName] = React.useState("");
  let [newInputType,setNewInputType] = React.useState("string");
  if(!component){
    return (
      <div
        className="flex-1 flex items-center justify-center text-gray-500 text-sm">No component selected
      </div>);
  }
  let handleAddInput = function (){
    if(!newInputName.trim()){
      return;
    }
    let updatedInputs = Object.assign(
      {},
      component.inputs || {},
      {[newInputName]:{"description":"","type":newInputType}}
    );
    onUpdateInputs(component.id,updatedInputs);
    setNewInputName("");
    setNewInputType("string");
  };
  let handleRemoveInput = function (inputName){
    let updatedInputs = Object.assign({},component.inputs || {});
    delete updatedInputs[inputName];
    onUpdateInputs(component.id,updatedInputs);
    if(component.inputValues){
      let updatedValues = Object.assign({},component.inputValues);
      delete updatedValues[inputName];
      onUpdateInputValues(component.id,updatedValues);
    }
  };
  let handleUpdateInputDescription = function (inputName,description){
    let updatedInputs = Object.assign({},component.inputs || {},{
      [inputName]:Object.assign({},component.inputs[inputName],{"description":description})
    });
    onUpdateInputs(component.id,updatedInputs);
  };
  let handleUpdateInputValue = function (inputName,value){
    let updatedValues = Object.assign({},component.inputValues || {},{[inputName]:value});
    onUpdateInputValues(component.id,updatedValues);
  };
  let renderInputValueEditor = function (inputName,inputDef){
    let currentValue = (component.inputValues[inputName] || inputDef.default) || "";
    switch(inputDef.type){
      case "boolean":
        return (
          <FigmaUi.Switch
            checked={currentValue}
            onCheckedChange={function (checked){
                return handleUpdateInputValue(inputName,checked);
              }}>
          </FigmaUi.Switch>);
      
      case "number":
        return (
          <FigmaUi.Input
            type="number"
            value={currentValue}
            onChange={function (e){
                return handleUpdateInputValue(inputName,parseFloat(e.target.value));
              }}
            className="h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs">
          </FigmaUi.Input>);
      
      case "object" || "array":
        return (
          <FigmaUi.Input
            type="text"
            value={((typeof currentValue) == "string") ? currentValue : JSON.stringify(currentValue)}
            onChange={function (e){
                try{
                  let parsed = JSON.parse(e.target.value);
                  handleUpdateInputValue(inputName,parsed);
                }
                catch(_){
                  handleUpdateInputValue(inputName,e.target.value);
                }
              }}
            className="h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs font-mono"
            placeholder={(inputDef.type == "array") ? "[]" : "{}"}>
          </FigmaUi.Input>);
      
      default:
        return (
          <FigmaUi.Input
            type="text"
            value={currentValue}
            onChange={function (e){
                return handleUpdateInputValue(inputName,e.target.value);
              }}
            className="h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs">
          </FigmaUi.Input>);
    }
  };
  return (
    <div className="flex flex-col h-full">
      <FigmaUi.ScrollArea className="flex-1">
        <div className="p-4 space-y-4">
          <div
            className="p-3 bg-blue-950/30 border border-blue-900/50 rounded">
            <p className="text-xs text-blue-300 mb-1">💡 Input Binding</p>
            <p className="text-[10px] text-blue-400/80">
              Define inputs here, then use 
              <code className="bg-blue-900/30 px-1 rounded">{input.name}</code>
               or 
              <code className="bg-blue-900/30 px-1 rounded">$input.name</code>
               in properties to bind values.
            </p>
          </div>
          <div>
            <h3
              className="text-xs text-gray-500 uppercase tracking-wider mb-3">Input Schema
            </h3>
            {(component.inputs && (Object.keys(component.inputs).length > 0)) ? (
              <div className="space-y-3">
                {Object.entries(component.inputs).map(function ([inputName,inputDef]){
                  return (
                    <div
                      key={inputName}
                      className="p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-xs font-mono text-blue-400">{inputName}</span>
                            <span className="text-xs text-gray-500">{": " + inputDef.type}</span>
                          </div>
                          <FigmaUi.Input
                            type="text"
                            placeholder="Description (optional)"
                            value={inputDef.description || ""}
                            onChange={function (e){
                                return handleUpdateInputDescription(inputName,e.target.value);
                              }}
                            className="h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs">
                          </FigmaUi.Input>
                        </div>
                        <FigmaUi.Button
                          variant="ghost"
                          size="sm"
                          onClick={function (){
                              return handleRemoveInput(inputName);
                            }}
                          className="h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"><Lucide.Trash2 className="w-3 h-3"></Lucide.Trash2>
                        </FigmaUi.Button>
                      </div>
                    </div>);
                })}
              </div>) : (
              <p className="text-xs text-gray-500 italic">No inputs defined</p>)}
          </div>
          <div
            className="mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]">
            <FigmaUi.Label className="text-xs text-gray-400 mb-2 block">Add Input</FigmaUi.Label>
            <div className="flex gap-2">
              <FigmaUi.Input
                type="text"
                placeholder="Input name"
                value={newInputName}
                onChange={function (e){
                    return setNewInputName(e.target.value);
                  }}
                onKeyDown={function (e){
                    return (e.key == "Enter") ? handleAddInput() : null;
                  }}
                className="flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs">
              </FigmaUi.Input>
              <FigmaUi.Select
                value={newInputType}
                onValueChange={function (v){
                    return setNewInputType(v);
                  }}>
                <FigmaUi.SelectTrigger
                  className="w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"><FigmaUi.SelectValue></FigmaUi.SelectValue>
                </FigmaUi.SelectTrigger>
                <FigmaUi.SelectContent>
                  <FigmaUi.SelectItem value="string">string</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="number">number</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="boolean">boolean</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="object">object</FigmaUi.SelectItem>
                  <FigmaUi.SelectItem value="array">array</FigmaUi.SelectItem>
                </FigmaUi.SelectContent>
              </FigmaUi.Select>
              <FigmaUi.Button
                size="sm"
                onClick={handleAddInput}
                className="h-7 px-3 bg-blue-600 hover:bg-blue-700 text-white"><Lucide.Plus className="w-3 h-3"></Lucide.Plus>
              </FigmaUi.Button>
            </div>
          </div>
        </div>
      </FigmaUi.ScrollArea>
      {(component.inputs && (Object.keys(component.inputs).length > 0)) ? (
        <React.Fragment>
          <div className="h-[1px] bg-[#323232]"></div>
          <div>
            <h3
              className="text-xs text-gray-500 uppercase tracking-wider mb-3">Input Values
            </h3>
            <div className="space-y-3">
              {Object.entries(component.inputs).map(function ([inputName,inputDef]){
                return (
                  <div key={inputName}>
                    <FigmaUi.Label className="text-xs text-gray-400 mb-1 block">
                      {inputName}
                      {inputDef.description ? (
                        <span className="text-gray-600 ml-2">{"- " + inputDef.description}</span>) : null}
                    </FigmaUi.Label>
                    {renderInputValueEditor(inputName,inputDef)}
                  </div>);
              })}
            </div>
          </div>
        </React.Fragment>) : null}
    </div>);
}