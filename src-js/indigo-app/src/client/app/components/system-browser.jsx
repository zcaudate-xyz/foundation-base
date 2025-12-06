import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'

// code.dev.client.app.components.system-browser/categories [8] 
export var categories = [
  "Kernel-Objects",
  "Collections-Sequenceable",
  "Collections-Unordered",
  "Graphics-Primitives",
  "System-Support"
];

// code.dev.client.app.components.system-browser/classes [10] 
export var classes = {
  "Kernel-Objects":[
    "Object",
    "Behavior",
    "Class",
    "Metaclass",
    "Boolean",
    "True",
    "False",
    "UndefinedObject"
  ],
  "Collections-Sequenceable":[
    "Array",
    "OrderedCollection",
    "SortedCollection",
    "String",
    "Symbol"
  ],
  "Collections-Unordered":["Set","Dictionary","IdentityDictionary","Bag"],
  "Graphics-Primitives":["Point","Rectangle","Color","Form"],
  "System-Support":["System","Transcript","Compiler","Debugger"]
};

// code.dev.client.app.components.system-browser/methods [17] 
export var methods = {
  "Object":[
    "initialize",
    "printOn:",
    "isNil",
    "notNil",
    "yourself",
    "copy",
    "deepCopy",
    "hash",
    "class"
  ],
  "Array":[
    "at:",
    "at:put:",
    "size",
    "do:",
    "collect:",
    "select:",
    "reject:",
    "first",
    "last"
  ],
  "String":[
    "size",
    "at:",
    "concat:",
    "isEmpty",
    "asUppercase",
    "asLowercase",
    "findString:"
  ],
  "Point":["x","y","x:y:","dist:","transpose","dotProduct:"]
};

// code.dev.client.app.components.system-browser/methodSource [23] 
export var methodSource = {
  "Object-initialize":"initialize\n    \"Initialize the receiver\"\n    ^ self",
  "Object-printOn":"printOn: aStream\n    \"Print the receiver on a stream\"\n    aStream nextPutAll: self class name",
  "Array-size":"size\n    \"Answer the number of elements in the receiver\"\n    ^ self basicSize",
  "Array-at":"at: index\n    \"Answer the given index\"\n    ^ self basicAt: index"
};

// code.dev.client.app.components.system-browser/SystemBrowser [29] 
export function SystemBrowser({onExecute,onMessage}){
  let [selectedCategory,setSelectedCategory] = React.useState(categories[0]);
  let [selectedClass,setSelectedClass] = React.useState("Object");
  let [selectedMethod,setSelectedMethod] = React.useState("initialize");
  let [methodCode,setMethodCode] = React.useState(methodSource["Object-initialize"]);
  let handleCategorySelect = function (category){
    setSelectedCategory(category);
    let firstClass = classes[category][0];
    setSelectedClass(firstClass);
    updateMethodView(firstClass);
  };
  let handleClassSelect = function (className){
    setSelectedClass(className);
    updateMethodView(className);
  };
  let updateMethodView = function (className){
    let classMethods = methods[className] || ["(no methods)"];
    setSelectedMethod(classMethods[0]);
    setMethodCode(
      methodSource[className + "-" + classMethods[0]] || (classMethods[0] + "\n    \"Method source not available\"")
    );
  };
  let handleMethodSelect = function (method){
    setSelectedMethod(method);
    let key = selectedClass + "-" + method;
    setMethodCode(
      methodSource[key] || (method + "\n    \"Method source not available\"")
    );
  };
  return (
    <div className="flex flex-col h-full bg-white">
      <div className="px-4 py-2 bg-gray-200 border-b"><h2 className="text-sm">System Browser</h2></div>
      <div className="flex flex-1 border-b overflow-hidden">
        <div className="w-1/4 border-r flex flex-col">
          <div className="px-2 py-1 bg-gray-100 border-b text-xs">Categories</div>
          <FigmaUi.ScrollArea className="flex-1">
            {categories.map(function (category){
              return (
                <div
                  key={category}
                  className={"px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 " + ((selectedCategory == category) ? "bg-blue-100" : "")}
                  onClick={function (){
                      return handleCategorySelect(category);
                    }}>{category}
                </div>);
            })}
          </FigmaUi.ScrollArea>
        </div>
        <div className="w-1/4 border-r flex flex-col">
          <div className="px-2 py-1 bg-gray-100 border-b text-xs">Classes</div>
          <FigmaUi.ScrollArea className="flex-1">
            {(classes[selectedCategory] || []).map(function (className){
              return (
                <div
                  key={className}
                  className={"px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 " + ((selectedClass == className) ? "bg-blue-100" : "")}
                  onClick={function (){
                      return handleClassSelect(className);
                    }}>{className}
                </div>);
            })}
          </FigmaUi.ScrollArea>
        </div>
        <div className="w-1/4 border-r flex flex-col">
          <div className="px-2 py-1 bg-gray-100 border-b text-xs">Protocols</div>
          <FigmaUi.ScrollArea className="flex-1">
            {["accessing","testing","printing","copying"].map(function (protocol){
              return (
                <div
                  key={protocol}
                  className="px-2 py-1 text-sm cursor-pointer hover:bg-gray-100">{protocol}
                </div>);
            })}
          </FigmaUi.ScrollArea>
        </div>
        <div className="w-1/4 flex flex-col">
          <div className="px-2 py-1 bg-gray-100 border-b text-xs">Methods</div>
          <FigmaUi.ScrollArea className="flex-1">
            {(methods[selectedClass] || []).map(function (method){
              return (
                <div
                  key={method}
                  className={"px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 " + ((selectedMethod == method) ? "bg-blue-100" : "")}
                  onClick={function (){
                      return handleMethodSelect(method);
                    }}>{method}
                </div>);
            })}
          </FigmaUi.ScrollArea>
        </div>
      </div>
      <div className="flex-1 flex flex-col">
        <div className="px-2 py-1 bg-gray-100 border-b text-xs">{selectedClass + " >> " + selectedMethod}</div>
        <textarea
          value={methodCode}
          onChange={function (e){
              return setMethodCode(e.target.value);
            }}
          className="flex-1 px-3 py-2 font-mono text-sm resize-none focus:outline-none">
        </textarea>
        <div className="flex gap-2 px-3 py-2 bg-gray-50 border-t">
          <FigmaUi.Button
            size="sm"
            variant="outline"
            onClick={function (){
                return onMessage("Method accepted");
              }}>Accept
          </FigmaUi.Button>
          <FigmaUi.Button
            size="sm"
            variant="outline"
            onClick={function (){
                return onMessage("Method cancelled");
              }}>Cancel
          </FigmaUi.Button>
        </div>
      </div>
    </div>);
}