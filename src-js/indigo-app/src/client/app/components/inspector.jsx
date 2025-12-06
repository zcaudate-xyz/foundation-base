import * as FigmaUi from '@xtalk/figma-ui'

// code.dev.client.app.components.inspector/Inspector [8] 
export function Inspector({object}){
  let renderObjectDetails = function (){
    if(!object){
      return (
        <div className="text-sm text-gray-500 italic">No object selected</div>);
    }
    let details = [];
    if(((typeof object) == "object") && (object != null)){
      details.push((
        <div key="self" className="mb-2">
          <span className="text-blue-600">self</span>
          : 
          {String.or(object.result,object)}
        </div>));
    }
    else{
      if(object.result && ((typeof object.result) == "object")){
        Object.entries(object.result).forEach(function ([key,value]){
          details.push((
            <div key={key} className="mb-1">
              <span className="text-blue-600">{key}</span>
              : 
              {String.value}
            </div>));
        });
      }
    }
    details.push((
      <div key="value" className="mb-2">
        <span className="text-blue-600">value</span>
        : 
        {String.object.result}
      </div>));
    details.push((
      <div key="type" className="mb-1"><span className="text-blue-600">type</span>: {object.type}</div>));
    return details;
  };
  return (
    <div className="flex flex-col h-full bg-white">
      <div className="px-4 py-2 bg-gray-200 border-b"><h2 className="text-sm">Inspector</h2></div>
      <div className="border-b p-3">
        <div className="text-xs text-gray-600 mb-2">Instance Variables</div>
        <FigmaUi.ScrollArea className="h-32">
          <div className="font-mono text-sm">{renderObjectDetails()}</div>
        </FigmaUi.ScrollArea>
      </div>
      <div className="flex-1 p-3">
        <div className="text-xs text-gray-600 mb-2">Object</div>
        <FigmaUi.ScrollArea className="h-full">
          <pre className="font-mono text-sm">{object ? JSON.stringify(object,null,2) : "nil"}</pre>
        </FigmaUi.ScrollArea>
      </div>
    </div>);
}