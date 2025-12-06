(ns code.tool.translate.js-dsl-integration-test
  (:use code.test)
  (:require [code.tool.translate.js-dsl :as sut]
            [std.lib :as h]
            [std.json :as json]
            [std.fs :as fs]))

(def +sample-js+
  "
  import React from 'react';
  
  export const MyComponent = (props) => {
    const [count, setCount] = React.useState(0);
    
    return (
      <div className=\"container\">
        <h1>Count: {count}</h1>
        <button onClick={() => setCount(count + 1)}>Increment</button>
      </div>
    );
  };
  
  class Helper {
    constructor() {
      this.value = 1;
    }
    
    getValue() {
      return this.value;
    }
  }
  ")

(defn parse-js [js-code]
  (let [tmp-file (fs/create-tmpfile js-code)
        proc (h/sh "node" "scripts/js_parser/parse.js" (str tmp-file) {:wait false})
        _    (h/sh-wait proc)
        res  (h/sh-output proc)]
    (if (zero? (:exit res))
      (json/read (:out res) json/+keyword-mapper+)
      (throw (ex-info "Failed to parse JS" {:error (:err res)})))))

(fact "integration: translates full page AST"
  (let [ast (parse-js +sample-js+)
        dsl (sut/translate-node ast)]
    
    ;; Check for key elements in the translated DSL
    dsl
    => (contains-in
        ['(import "react" :default React)
         '(export (defn.js MyComponent [props]
                    (do (var ([count setCount] (. React useState 0)))
                        (return ("div" {:className "container"}
                                       ("h1" {} "Count: " count)
                                       ("button" {:onClick (fn [] (return (setCount (+ count 1))))} "Increment"))))))
         '(defclass Helper []
            (constructor [] (do (:= (. this value) 1)))
            (getValue [] (do (return (. this value)))))])))
