(ns kmi.lang.runtime
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval read read-string load]))

(l/script :xtalk
  {:require [[kmi.lang.parser :as parser]
             [kmi.lang.runtime.env :as env]
             [kmi.lang.runtime.primitive :as prim]
             [kmi.lang.runtime.eval :as eval]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.protocol-base :as p]
             [xt.substrate :as substrate]
             [xt.lang.spec-base :as xt]]})

;;
;; PUBLIC RUNTIME API
;;

(defn.xt empty-runtime
  "returns an empty runtime seeded with primitives"
  {:added "4.1"}
  []
  (return (prim/init-runtime (env/runtime-create))))

(defn.xt read-string
  "reads a single form from a string"
  {:added "4.1"}
  [source]
  (return (parser/read-string source)))

(defn.xt read-many
  "reads all forms from a string"
  {:added "4.1"}
  [source]
  (return (eval/read-many source)))

(defn.xt eval-form
  "evaluates a single form in a fresh environment"
  {:added "4.1"}
  [runtime form]
  (return (eval/eval-form runtime (env/empty-env) form)))

(defn.xt eval-string
  "evaluates a string expression and returns a result map"
  {:added "4.1"}
  [runtime source]
  (return (eval/eval-string runtime source)))

(defn.xt eval-string-many
  "evaluates all forms in a string"
  {:added "4.1"}
  [runtime source]
  (return (eval/eval-string-many runtime source)))

;;
;; SUBSTRATE HANDLERS
;;

(defn.xt handler-read
  "substrate handler for @kmi.lang/read"
  {:added "4.1"}
  [space args request node]
  (var source (xt/x:first args))
  (return {"form" (parser/read-string source)}))

(defn.xt handler-eval
  "substrate handler for @kmi.lang/eval"
  {:added "4.1"}
  [space args request node]
  (var source (xt/x:first args))
  (var runtime (xt/x:get-key (xt/x:get-key space "state") "runtime"))
  (var out (eval/eval-string runtime source))
  (when (eval/errorp out)
    (return {"error" (xt/x:get-key out "error")}))
  (substrate/set-space-state
   node
   (xt/x:get-key space "id")
   {"runtime" (eval/get-runtime out)})
  (return {"value" (eval/get-value out)}))

(defn.xt handler-load
  "substrate handler for @kmi.lang/load"
  {:added "4.1"}
  [space args request node]
  (var source (xt/x:first args))
  (var runtime (xt/x:get-key (xt/x:get-key space "state") "runtime"))
  (var out (eval/eval-string-many runtime source))
  (when (eval/errorp out)
    (return {"error" (xt/x:get-key out "error")}))
  (substrate/set-space-state
   node
   (xt/x:get-key space "id")
   {"runtime" (eval/get-runtime out)})
  (return {"value" (eval/get-value out)}))

(defn.xt handler-describe
  "substrate handler for @kmi.lang/describe"
  {:added "4.1"}
  [space args request node]
  (var source (xt/x:first args))
  (var form (parser/read-string source))
  (var size nil)
  (when (util/is-managed? form)
    (:= size (p/size form)))
  (return {"tag" (common-hash/native-class form)
           "type" (common-hash/native-type form)
           "size" size
           "string" (util/show form)}))

;;
;; NODE CREATION
;;

(defn.xt create-node
  "creates a substrate node with the kmi.lang runtime handlers"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (var node (substrate/node-create
             (xt/x:obj-assign
              {"spaces"
               {"kmi.session"
                {"state"
                 {"runtime" (-/empty-runtime)}}}
               "handlers"
               {"@kmi.lang/read"     {"fn" -/handler-read}
                "@kmi.lang/eval"     {"fn" -/handler-eval}
                "@kmi.lang/load"     {"fn" -/handler-load}
                "@kmi.lang/describe" {"fn" -/handler-describe}}}
              opts)))
  (return node))

(defn.xt stop
  "placeholder stop for a runtime node"
  {:added "4.1"}
  [node]
  (return true))
