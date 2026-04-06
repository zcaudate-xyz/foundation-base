(ns code.mcp.tool.form-heal
  (:require [code.mcp.heal.form :as form-heal]
            [code.mcp.heal.form-edits :as form-edits]))

(def ^:private +edit-registry+
  {"fix:namespaced-symbol-no-dot" #'form-edits/fix:namespaced-symbol-no-dot
   "fix:dash-indexing" #'form-edits/fix:dash-indexing
   "fix:set-arg-destructuring" #'form-edits/fix:set-arg-destructuring
   "fix:remove-fg-extra-references" #'form-edits/fix:remove-fg-extra-references
   "fix:replace-fg-extra-namepspaces" #'form-edits/fix:replace-fg-extra-namepspaces
   "fix:remove-mistranslated-syms" #'form-edits/fix:remove-mistranslated-syms})

(defn available-edits
  []
  (sort (keys +edit-registry+)))

(defn- normalize-source-paths
  [source-paths]
  (cond
    (vector? source-paths) source-paths
    (seq? source-paths) (vec source-paths)
    (string? source-paths) [source-paths]
    :else []))

(defn- normalize-env
  [{:keys [root source-paths]}]
  {:root root
   :source-paths (normalize-source-paths source-paths)})

(defn list-edits-fn
  [_ _]
  (let [edits (available-edits)]
    {:content [{:type "text" :text (pr-str edits)}]
     :structuredContent {:edits edits}
     :isError false}))

(defn get-dsl-deps-fn
  [_ {:keys [root source-paths]}]
  (let [deps (form-heal/get-dsl-deps (normalize-env {:root root :source-paths source-paths}))]
    {:content [{:type "text" :text (pr-str deps)}]
     :structuredContent {:deps deps}
     :isError false}))

(defn- resolve-edits
  [edits]
  (mapv (fn [edit-name]
          (or (get +edit-registry+ edit-name)
              (throw (ex-info "Unknown form heal edit"
                              {:edit edit-name
                               :available (available-edits)}))))
        edits))

(defn refactor-directory-fn
  [_ {:keys [root source-paths edits write]}]
  (let [resolved (resolve-edits (or edits []))
        result (form-heal/refactor-directory
                (normalize-env {:root root :source-paths source-paths})
                resolved
                {:write (boolean write)})]
    {:content [{:type "text" :text (pr-str result)}]
     :structuredContent {:result result}
     :isError false}))

(def list-edits-tool
  {:name "form-heal-list-edits"
   :description "List available code/form healing edits"
   :inputSchema {:type "object"
                 :properties {}}
   :implementation #'list-edits-fn})

(def get-dsl-deps-tool
  {:name "form-heal-get-dsl-deps"
   :description "Extract DSL dependencies for files in source paths"
   :inputSchema {:type "object"
                 :properties {"root" {:type "string"}
                              "source-paths" {:type "array"
                                              :items {:type "string"}}}
                 :required ["root" "source-paths"]}
   :implementation #'get-dsl-deps-fn})

(def refactor-directory-tool
  {:name "form-heal-refactor-directory"
   :description "Apply selected healing edits to files in source paths"
   :inputSchema {:type "object"
                 :properties {"root" {:type "string"}
                              "source-paths" {:type "array"
                                              :items {:type "string"}}
                              "edits" {:type "array"
                                        :items {:type "string"
                                                :enum (vec (available-edits))}}
                              "write" {:type "boolean"}}
                 :required ["root" "source-paths" "edits"]}
   :implementation #'refactor-directory-fn})
