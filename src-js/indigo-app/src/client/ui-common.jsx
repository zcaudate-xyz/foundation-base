import React from 'react'

import ReactDOM from 'react-dom/client'

import * as k from '@/libs/xt/lang/base-lib'

import * as r from '@/libs/js/react'

// code.dev.client.ui-common/isReactRoot [14] 
export function isReactRoot(container) {
  let internalKey = Object.keys(container).find(function (key) {
    return key.startsWith("__reactContainer");
  });
  return boolean(internalKey) || (container.hasOwnProperty("_reactRootContainer") && (container._reactRootContainer !== undefined));
}

// code.dev.client.ui-common/getReactRoot [23] 
export function getReactRoot(domNode) {
  let internalKey = k.arr_find(k.obj_keys(domNode), function (key) {
    return key.startsWith("__reactContainer$");
  });
  if (internalKey > 0) {
    return domNode[internalKey];
  }
  return null;
}

// code.dev.client.ui-common/renderRoot [34] 
export function renderRoot(id, Component) {
  let rootElement = document.getElementById(id);
  let root = getReactRoot(rootElement);
  if (null == root) {
    root = ReactDOM.createRoot(rootElement);
  }
  root.render((
    <Component></Component>));
  return true;
}

// code.dev.client.ui-common/TabComponent [43] 
export function TabComponent({ controls, controlKey = "current", pages = [] }) {
  let [current, setCurrent] = controls ? r.useStateFor(controls, controlKey) : React.useState(0);
  if (null == current) {
    current = 0;
    setCurrent(0);
  }
  let CurrentPage = pages[current];
  return (
    <div className="flex flex-col grow">
      <React.Fragment>
        <div role="tablist" className="tabs tabs-border">
          {pages.map(function (page, i) {
            return (
              <React.Fragment key={i}>
                <div
                  role="tab"
                  className={"tab" + ((i == current) ? " tab-active" : "")}
                  onClick={function () {
                    setCurrent(i);
                  }}>{page.title}
                </div>
              </React.Fragment>);
          })}
        </div>
        <div className="flex flex-col grow py-4 px-2">
          {("function" == (typeof CurrentPage.content)) ? (
            <CurrentPage.content></CurrentPage.content>) : CurrentPage.content}
        </div>
      </React.Fragment>
    </div>);
}

// code.dev.client.ui-common/languageToMode [79] 
export var languageToMode = {
  "javascript": "javascript",
  "html": "htmlmixed",
  "clojure": "clojure",
  "python": "python",
  "plpgsql": "text/x-pgsql"
};

// code.dev.client.ui-common/CodeEditor [86] 
export function CodeEditor({ value, onChange, onSubmit, language = "javascript", ...props }) {
  let textareaRef = React.useRef(null);
  let editorRef = React.useRef(null);
  let onSubmitRef = React.useRef(onSubmit);
  React.useEffect(function () {
    onSubmitRef.current = onSubmit;
  }, [onSubmit]);
  React.useEffect(function () {
    if (((typeof window.CodeMirror) == "undefined") || (null == textareaRef.current)) {
      return null;
    }
    let currentMode = languageToMode[language];
    let handleSubmit = function (cm) {
      if (onSubmitRef.current) {
        onSubmitRef.current(cm.getValue());
      }
    };
    let editor = window.CodeMirror.fromTextArea(textareaRef.current, {
      "lineNumbers": true,
      "lineWrapping": false,
      "mode": currentMode,
      "extraKeys": { "Ctrl-Enter": handleSubmit, "Cmd-Enter": handleSubmit }
    });
    editorRef.current = editor;
    editor.setValue(value || "");
    editor.on("change", function (instance) {
      return onChange(instance.getValue());
    });
    return function () {
      if (editorRef.current) {
        return editorRef.current.toTextArea();
      }
    };
  }, []);
  React.useEffect(function () {
    if (editorRef.current && (value !== editorRef.current.getValue())) {
      return editorRef.current.setValue(value || "");
    }
  }, [value]);
  React.useEffect(function () {
    if (editorRef.current) {
      let newMode = languageToMode[language];
    }
    else {
      return editorRef.current.setOption("mode", newMode);
    }
  }, [language]);
  return (
    <textarea ref={textareaRef} {...props}></textarea>);
}

// code.dev.client.ui-common/Icon [154] 
export function Icon({ name, color, size, strokeWidth, className, ...rest }) {
  let iconRef = React.useRef(null);
  React.useEffect(function () {
    if (null == iconRef.current) {
      return function () {

      };
    }
    if (((typeof window.lucide) == "undefined") || (null == k.get_in(window.lucide, ["icons", name]))) {
      console.warn("Icon \"" + name + "\" not found in Lucide library.");
      iconRef.current.innerHTML = "";
      return function () {

      };
    }
    let iconNode = k.get_in(window.lucide, ["icons", name]);
    let svgEl = window.lucide.createElement(iconNode);
    if (color) {
      svgEl.setAttribute("stroke", color);
    }
    if (size) {
      svgEl.setAttribute("width", size);
      svgEl.setAttribute("height", size);
    }
    if (strokeWidth) {
      svgEl.setAttribute("stroke-width", strokeWidth);
    }
    if (className) {
      for (let cls of className.split(" ")) {
        if (k.not_emptyp(cls)) {
          svgEl.classList.add(cls);
        }
      };
    }
    iconRef.current.innerHTML = "";
    iconRef.current.appendChild(svgEl);
    return function () {

    };
  }, [name, color, size, strokeWidth, className]);
  return (
    <span ref={iconRef} {...rest}></span>);
}

// code.dev.client.ui-common/getLocalStore [204] 
export function getLocalStore(storage_key) {
  let stored = localStorage.getItem(storage_key);
  try {
    stored = JSON.parse(stored);
  }
  catch (e) {
    stored = null;
  }
  return stored;
}

// code.dev.client.ui-common/useLocalHistory [214] 
export function useLocalHistory(history_key) {
  let [history, setHistory] = React.useState(getLocalStore(history_key) || []);
  let historyStr = JSON.stringify(history);
  React.useEffect(function () {
    localStorage.setItem(history_key, historyStr);
  }, [historyStr]);
  return [history, setHistory];
}

// code.dev.client.ui-common/SplitPane [227] 
export function SplitPane({ left, right }) {
  let [leftWidth, setLeftWidth] = React.useState(null);
  let containerRef = React.useRef(null);
  let handleMouseMove = React.useCallback(function (e) {
    if (null == containerRef.current) {
      return null;
    }
    let containerRect = containerRef.current.getBoundingClientRect();
    let newLeftWidth = e.clientX - containerRect.left;
    let minWidth = 100;
    let maxWidth = containerRect.width - 100;
    if (newLeftWidth < minWidth) {
      newLeftWidth = minWidth;
    }
    if (newLeftWidth > maxWidth) {
      newLeftWidth = maxWidth;
    }
    setLeftWidth(newLeftWidth);
  }, []);
  let handleMouseUp = React.useCallback(function () {
    document.removeEventListener("mousemove", handleMouseMove);
    document.removeEventListener("mouseup", handleMouseUp);
  }, [handleMouseMove]);
  let handleMouseDown = function (e) {
    e.preventDefault();
    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
  };
  return (
    <div
      ref={containerRef}
      className="flex w-full overflow-hidden bg-muted">
      <div className={"bg-background overflow-y-auto"} style={{ width: leftWidth || '50%' }}>{left}</div>
      <div
        onMouseDown={handleMouseDown}
        className="w-1 bg-border cursor-col-resize hover:bg-primary transition-colors">
      </div>
      <div className="flex-1 grow bg-background overflow-y-auto">{right}</div>
    </div>);
}

// code.dev.client.ui-common/TextDiffViewer [312] 
export function TextDiffViewer({ newValue, oldValue }) {
  let diff = window.Diff.diffLines(oldValue, newValue);
  let diff_elements = [];
  let line = 1;
  for (let index = 0; index < diff.length; ++index) {
    let part = diff[index];
    let className = "";
    if (part.added) {
      className = "diff-added";
      k.arr_pushl(diff_elements, (
        <span key={index} className="bg-green-100">{"L" + line + ": " + part.value}</span>));
      line = (line + part.count);
    }
    else if (part.removed) {
      className = "diff-removed";
      k.arr_pushl(diff_elements, (
        <span key={index} className="bg-red-100">{"L" + line + ": " + part.value}</span>));
    }
    else {
      line = (line + part.count);
    }
  };
  return (
    <pre className="diff-viewer-pre">{diff_elements}</pre>);
}

// code.dev.client.ui-common/MODULE [380] 
export var MODULE = {
  "isReactRoot": isReactRoot,
  "getReactRoot": getReactRoot,
  "renderRoot": renderRoot,
  "TabComponent": TabComponent,
  "languageToMode": languageToMode,
  "CodeEditor": CodeEditor,
  "Icon": Icon,
  "getLocalStore": getLocalStore,
  "useLocalHistory": useLocalHistory,
  "SplitPane": SplitPane,
  "TextDiffViewer": TextDiffViewer,
  "MODULE": MODULE
};