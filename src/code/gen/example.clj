;; src/code/gen/example.clj
(ns code.gen.example
  (:require [code.gen.core :as gen]
            [std.lib.foundation :as f]
            [std.string :as str]
            [std.lib :as h]))

;; 1. Define a preprocessor function (optional, but good for demonstration)
(defn preprocess-greeter-bindings
  "TODO"
  {:added "4.0"}
  [bindings]
  (let [args-str (str/join ", " (map name (:args bindings)))]
    (assoc bindings :doc-extra (str "[Preprocessed args: " args-str "]"))))

;; 2. Define the template function using `gen/template-generator`
(def greeter-template-fn
  (gen/template-generator "resources/my/templates/def_greeter.block.clj"))

;; 3. Define a list of entries (bindings maps)
(def greeter-entries
  [{:name 'hello-world
    :docstring "Greets a person by name."
    :args '[first-name last-name]
    :body  '[(let [full-name (str first-name " " last-name)]
                (h/pl (str "Hello, " full-name "!")))]
    :preprocess-fn preprocess-greeter-bindings
    :namespace-block (gen/gen-namespace-block 'my.generated.core {'clojure.string 'str 'std.lib 'h})} ; Add namespace-block
   {:name 'goodbye-world
    :docstring "Says goodbye to a person."
    :args '[person]
    :body '[(h/pl (str "Goodbye, " person "!"))]
    :namespace-block (gen/gen-namespace-block 'my.generated.core {'std.lib 'h})}])

;; 4. Use `f/template-entries` to generate code
(def generated-code-list
  (f/template-entries [greeter-template-fn]
                      greeter-entries))

;; Print the generated code for demonstration
(doseq [code generated-code-list]
  (println code)
  (println "---"))