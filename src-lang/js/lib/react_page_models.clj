(ns js.lib.react-page-models
  (:require [lang.core :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:require [[xt.lang.common-lib :as k]
             [js.react :as r]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-sort-by :as xtsb]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.view :as page-view]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "React"
                                   :tag "js"}]
  [useId])

(defn.js usePageModels
  "attaches declarative substrate models to a node/group and returns live input/output bindings

   Specs are keyed by model id. Use :fn and :args as shorthand for the substrate
   :handler and :defaults/:args. Keep model-specs stable (for example, memoize it)."
  {:added "0.1"}
  [node space-id group-id model-specs]
  (var hook-id (-/useId))
  (var model-ids (xtsb/sort-by (xtd/obj-keys model-specs) [k/identity]))
  (var view-bindings {})
  (xt/for:array [model-id model-ids]
    (xt/x:set-key view-bindings
                  (xt/x:cat model-id "::input")
                  {"source" "model-input"
                   "space_id" space-id
                   "group_id" group-id
                   "model_id" model-id
                   "path" ["data"]})
    (xt/x:set-key view-bindings
                  (xt/x:cat model-id "::output")
                  {"source" "model-output"
                   "space_id" space-id
                   "group_id" group-id
                   "model_id" model-id}))
  (var view-spec
       (r/useMemo
        (fn []
          (return (page-view/view-spec
                   (xt/x:cat "react-page-models-" hook-id)
                   view-bindings
                   nil)))
        [hook-id node space-id group-id model-specs]))
  (var [snapshot set-snapshot] (r/useState {}))
  (r/useEffect
   (fn []
     (var normalized {})
     (xt/for:array [model-id model-ids]
       (var model-spec (xt/x:get-key model-specs model-id))
       (var model (xtd/obj-assign {} model-spec))
       (when (xt/x:has-key? model-spec "fn")
         (xt/x:set-key model "handler" (xt/x:get-key model-spec "fn")))
       (when (xt/x:has-key? model-spec "args")
         (var defaults (xtd/obj-assign {} (. model-spec ["defaults"])))
         (xt/x:set-key defaults "args" (. model-spec ["args"]))
         (xt/x:set-key model "defaults" defaults))
       (xt/x:del-key model "fn")
       (xt/x:del-key model "args")
       (xt/x:set-key normalized model-id model))
     (page-core/group-add-attach node space-id group-id normalized)
     (var subscription
          (page-view/subscribe
           node view-spec hook-id
           (fn [next _revision _event]
             (set-snapshot next))))
     (set-snapshot (page-view/snapshot node view-spec))
     (page-core/group-update node space-id group-id {})
     (return
      (fn []
        (page-view/unsubscribe subscription))))
   [node space-id group-id model-specs view-spec])
  (var models {})
  (xt/for:array [model-id model-ids]
    (var model-spec (xt/x:get-key model-specs model-id))
    (var input-key (xt/x:cat model-id "::input"))
    (var input (xt/x:get-key snapshot input-key))
    (when (not (xt/x:has-key? snapshot input-key))
      (var defaults (xt/x:get-key model-spec "defaults"))
      (cond (xt/x:has-key? model-spec "args")
            (:= input (xt/x:get-key model-spec "args"))
            (xt/x:has-key? defaults "args")
            (:= input (xt/x:get-key defaults "args"))
            :else (:= input [])))
    (xt/x:set-key
     models model-id
     {"input" input
      "output" (xt/x:get-key snapshot (xt/x:cat model-id "::output"))
      "setInput"
      (fn [args]
        (return
         (page-core/model-set-input
          node space-id group-id model-id {"data" args} {})))
      "update"
      (fn []
        (return
         (page-core/model-update node space-id group-id model-id {})))}))
  (return {"models" models}))
