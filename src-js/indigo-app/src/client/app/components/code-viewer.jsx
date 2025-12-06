import * as FigmaUi from '@xtalk/figma-ui'

import * as Lucide from 'lucide-react'

// code.dev.client.app.components.code-viewer/generateLayout [10] 
export function generateLayout(component,indent = 0){
  let indentation = new Array(indent + 1).join("  ");
  let code = "";
  let tagMap = {
    "Heading":"h2",
    "Container":"div",
    "Input":"input",
    "Card":"div",
    "Switch":"input",
    "Text":"p",
    "FlexRow":"div",
    "Checkbox":"input",
    "FlexCol":"div",
    "Button":"button"
  };
  let tag = tagMap[component.type] || "div";
  code = (code + indentation + ":" + tag);
  let props = component.properties;
  let propKeys = Object.keys(props);
  if(propKeys.length > 0){
    code = (code + " {");
    propKeys.forEach(function (key,idx){
      let value = props[key];
      if((key == "children") && ((typeof value) == "string")){
        return;
      }
      code = (code + ":" + key + " ");
      if((typeof value) == "string"){
        code = (code + "\"" + value + "\"");
      }
      else{
        code = (code + value);
      }
      if(idx < (propKeys.length - 1)){
        code = (code + "\n" + indentation + "      ");
      }
    });
    code = (code + "}");
  }
  if(props.children && ((typeof props.children) == "string")){
    code = (code + "\n" + indentation + "   \"" + props.children + "\"");
  }
  if(component.children.length > 0){
    component.children.forEach(function (child){
      code = (code + "\n" + generateLayout(child,indent + 1));
    });
  }
  code = (code + "]");
  return code;
}

// code.dev.client.app.components.code-viewer/generateStdLangCode [66] 
export function generateStdLangCode(components){
  let code = "";
  code = (code + ";; Generated std.lang UI Component\n");
  code = (code + ";; Styled with Tailwind CSS\n\n");
  code = (code + "(ns my-component\n");
  code = (code + "  (:require [std.lang :as l]\n");
  code = (code + "            [std.lib :as h]))\n\n");
  code = (code + "(l/script :js\n");
  code = (code + "  {:runtime :websocket\n");
  code = (code + "   :require [[js.react :as r]\n");
  code = (code + "             [xt.lang.base-lib :as k]]\n");
  code = (code + "   :export [MODULE]}))\n\n");
  code = (code + "(defn.js MyComponent\n");
  code = (code + "  [props]\n");
  code = (code + "  (return\n");
  code = (code + "    (r/return-ui\n");
  code = (code + "      {;; Layout Section - Tailwind CSS Styled Components\n");
  code = (code + "       :layout\n");
  if(components.length > 0){
    let layoutCode = components.map(function (comp){
      return generateLayout(comp,4);
    }).join("\n\n");
    code = (code + layoutCode + "\n\n");
  }
  code = (code + "       ;; States Section\n");
  code = (code + "       :states {}\\n\n");
  code = (code + "       ;; Triggers Section\n");
  code = (code + "       :triggers {}\\n\n");
  code = (code + "       ;; Actions Section\n");
  code = (code + "       :actions {}\\n\n");
  code = (code + "       ;; Components Section\n");
  code = (code + "       ;; Define reusable component mappings here\n");
  code = (code + "       :components {\n");
  code = (code + "         ;; Example:\n");
  code = (code + "         ;; :ui/button {:tag :button\n");
  code = (code + "         ;;             :props {:class [\"btn\"]}}\n");
  code = (code + "       }}))\n\n");
  code = (code + "(def.js MODULE (!:module))\n");
  return code;
}

// code.dev.client.app.components.code-viewer/copyToClipboard [131] 
export function copyToClipboard(text){
  return navigator.clipboard.writeText(fullCode);
}

// code.dev.client.app.components.code-viewer/CodeViewer [135] 
export function CodeViewer({components = []}){
  let fullCode = generateStdLangCode(components);
  return (
    <div className="flex flex-col h-full bg-white">
      <div
        className="px-4 py-2 bg-gray-100 border-b flex items-center justify-between">
        <h3 className="text-sm">Generated std.lang Code</h3>
        <FigmaUi.Button size="sm" variant="ghost" onClick={copyToClipboard}><Lucide.Copy className="w-3 h-3 mr-1"></Lucide.Copy>Copy</FigmaUi.Button>
      </div>
      <FigmaUi.ScrollArea className="flex-1">
        <pre className="p-4 font-mono text-xs"><code>{fullCode}</code></pre>
      </FigmaUi.ScrollArea>
    </div>);
}