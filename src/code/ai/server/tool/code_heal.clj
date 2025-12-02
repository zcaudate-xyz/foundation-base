(ns code.ai.server.tool.code-heal
  (:require [code.heal.core :as heal]
            [code.project :as project]
            [std.lib :as h]))

(defn heal-code-fn
  [_ {:keys [ns params]}]
  (let [ns (symbol ns)
        params (if params (read-string params) {})
        project (project/project)
        lookup (project/file-lookup project)
        result (heal/heal-code-single ns params lookup project)]
    {:content [{:type "text"
                :text (pr-str result)}]
     :isError false}))

(def heal-code-tool
  {:name "code-heal"
   :description "Heals code in a namespace (e.g. fix imports)"
   :inputSchema {:type "object"
                 :properties {"ns" {:type "string"}
                              "params" {:type "string" :description "EDN string of params"}}
                 :required ["ns"]}
   :implementation #'heal-code-fn})
