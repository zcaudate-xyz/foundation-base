(ns app.manage.page-runtime
  "Shared declaration lookup, request bindings and scenario execution."
  (:require [lang.core :as l]))
(l/script :js)
(defn.js definition
  [pages id]
  (var page (. pages (find (fn [page] (return (== page.id id))))))
  (when (not page)
    (throw (new Error "Unknown page definition.")))
  (return page))
(defn.js entry
  [pages id kind member]
  (var page (-/definition pages id))
  (var entries (:? (== kind "action") page.actions page.models))
  (var found (. (or entries {}) [member]))
  (when (not found)
    (throw (new Error "Unknown page member.")))
  (return found))
(defn.js request
  [entry values]
  (var spec entry.transport)
  (when (not spec)
    (throw (new Error "No request binding declared.")))
  (var body (Object.assign {} (or spec.constants {})))
  (for:array [key (Object.keys (or spec.bindings {}))]
             (:= (. body [key]) (. values [(. spec.bindings [key])])))
  (return {"path" spec.path "method" spec.method "body" (:? (== spec.method "GET") nil body)}))
(defn.js resolveRefs
  [input results]
  (when (or (== input nil) (not= (typeof input) "object"))
    (return input))
  (when (Array.isArray input)
    (return (. input (map (fn [value] (return (-/resolveRefs value results)))))))
  (when (Array.isArray (. input ["$ref"]))
    (var value results)
    (for:array [part (. input ["$ref"])]
               (when (or (== value nil) (not (Object.hasOwn value part)))
                 (throw (new Error "Scenario reference is not available.")))
               (:= value (. value [part])))
    (return value))
  (var value {})
  (for:array [key (Object.keys input)] (:= (. value [key]) (-/resolveRefs (. input [key]) results)))
  (return value))
(defn.js ^{:- [:async]} execute
  [pages adapters input]
  (var page (-/definition pages input.page))
  (var order (or page.order (Object.keys (or page.models {}))))
  (when (and (== input.kind "page") (> order.length 1))
    (var output {})
    (var state {})
    (for:array [member order]
               (var actual
                    (await (-/execute pages
                                      adapters
                                      (Object.assign {} input {"kind" "model" "member" member}))))
               (when (or actual.error actual.blocked)
                 (return actual))
               (:= (. output [member]) actual.result)
               (:= (. state [member]) actual.state))
    (return {"result" output "state" state}))
  (var member (or input.member (. (or page.order []) [0]) (. (Object.keys page.models) [0])))
  (var selected (-/entry pages input.page input.kind member))
  (var values (Object.assign {} (or selected.defaults {}) (or input.payload {})))
  (var inputs (or input.inputs {}))
  (for:array [target (Object.keys (or selected.bindings {}))]
             (var source (. selected.bindings [target]))
             (when (or (not (Object.hasOwn inputs source)) (== (. inputs [source]) nil))
               (throw (new Error (+ "Missing page binding: " source))))
             (if (== target "*")
               (Object.assign values (. inputs [source]))
               (:= (. values [target]) (. inputs [source]))))
  (:= input (Object.assign {} input {"payload" values}))
  (when (== input.kind "resolve")
    (return {"result" {"page" input.page
                       "name" member
                       "values" (or input.payload {})
                       "operation" selected.operation}}))
  (when (not (. ["page" "model" "action"] (includes input.kind)))
    (return {"blocked" "Choose resolve, model, action or page."}))
  (var adapter (. adapters [selected.operation]))
  (when (not adapter)
    (return {"blocked" "This operation has no live workbench adapter yet."}))
  (return (await (adapter input selected))))
(defn.js ^{:- [:async]} scenario
  [request run]
  (var results {})
  (when (Array.isArray request.steps)
    (var steps [])
    (for:array [step request.steps]
               (var actual
                    (await (run (-/resolveRefs (Object.assign {"actor" (or request.actor "alice")}
                                                              step)
                                               results))))
               (:= actual.id step.id)
               (:= (. results [step.id]) actual)
               (. steps (push actual)))
    (return {"steps" steps}))
  (return (await (run request))))

(defn.js operation
  [pages id]
  (for:array [page pages]
             (for:array [kind ["models" "actions"]]
                        (for:array [entry (Object.values (or (. page [kind]) {}))]
                                   (when (== entry.operation id)
                                     (return entry)))))
  (throw (new Error "Unknown declared operation.")))
