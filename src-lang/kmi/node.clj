(ns kmi.node
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [eval read read-string load load-string]))

(l/script :xtalk
  {:require [[kmi.lang.runtime :as kmi]
             [kmi.lang.runtime.eval :as eval]
             [kmi.lang.common-util :as util]
             [kmi.lang.common-hash :as common-hash]
             [kmi.lang.protocol-base :as p]
             [xt.substrate :as substrate]
             [xt.lang.spec-base :as xt]]})

(def.xt DEFAULT_SPACE "kmi.session")

(defn.xt handler-read
  "substrate handler for @kmi.lang/read"
  {:added "4.1"}
  [space args request node]
  (return {"form" (kmi/read-string (xt/x:first args))}))

(defn.xt set-runtime
  "updates the KMI runtime while preserving unrelated space state"
  {:added "4.1"}
  [node space runtime]
  (var state (xt/x:obj-clone (or (xt/x:get-key space "state") {})))
  (xt/x:set-key state "runtime" runtime)
  (substrate/set-space-state node (xt/x:get-key space "id") state)
  (return runtime))

(defn.xt space-runtime
  "returns the space runtime, creating one lazily when absent"
  {:added "4.1"}
  [node space]
  (var runtime (xt/x:get-key (xt/x:get-key space "state") "runtime"))
  (when (xt/x:nil? runtime)
    (:= runtime (kmi/empty-runtime))
    (-/set-runtime node space runtime))
  (return runtime))

(defn.xt handler-eval
  "substrate handler for @kmi.lang/eval"
  {:added "4.1"}
  [space args request node]
  (var out (kmi/eval-string (-/space-runtime node space)
                            (xt/x:first args)))
  (when (eval/errorp out)
    (return {"error" (xt/x:get-key out "error")}))
  (-/set-runtime node space (eval/get-runtime out))
  (return {"value" (eval/get-value out)}))

(defn.xt handler-load
  "substrate handler for @kmi.lang/load"
  {:added "4.1"}
  [space args request node]
  (var out (kmi/eval-string-many (-/space-runtime node space)
                                 (xt/x:first args)))
  (when (eval/errorp out)
    (return {"error" (xt/x:get-key out "error")}))
  (-/set-runtime node space (eval/get-runtime out))
  (return {"value" (eval/get-value out)}))

(defn.xt handler-describe
  "substrate handler for @kmi.lang/describe"
  {:added "4.1"}
  [space args request node]
  (var form (kmi/read-string (xt/x:first args)))
  (var size nil)
  (when (util/is-managed? form)
    (:= size (p/size form)))
  (return {"tag" (common-hash/native-class form)
           "type" (common-hash/native-type form)
           "size" size
           "string" (util/show form)}))

(defn.xt install
  "installs KMI handlers and a default session into an existing node"
  {:added "4.1"}
  [node opts]
  (:= opts (or opts {}))
  (var space-id (or (xt/x:get-key opts "space_id") -/DEFAULT_SPACE))
  (var space (substrate/get-space node space-id))
  (when (xt/x:nil? space)
    (substrate/create-space node space-id {"state" {}})
    (:= space (substrate/get-space node space-id)))
  (var supplied-runtime (xt/x:get-key opts "runtime"))
  (if (xt/x:not-nil? supplied-runtime)
    (-/set-runtime node space supplied-runtime)
    (-/space-runtime node space))
  (substrate/register-handler
   node "@kmi.lang/read"
   (fn [space args request handler-node]
     (return (-/handler-read space args request handler-node))) nil)
  (substrate/register-handler
   node "@kmi.lang/eval"
   (fn [space args request handler-node]
     (return (-/handler-eval space args request handler-node))) nil)
  (substrate/register-handler
   node "@kmi.lang/load"
   (fn [space args request handler-node]
     (return (-/handler-load space args request handler-node))) nil)
  (substrate/register-handler
   node "@kmi.lang/describe"
   (fn [space args request handler-node]
     (return (-/handler-describe space args request handler-node))) nil)
  (return node))

(defn.xt create-node
  "creates a substrate node and installs KMI; nested `kmi` holds install opts"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (var node-opts (xt/x:obj-clone opts))
  (var kmi-opts (xt/x:get-key node-opts "kmi"))
  (xt/x:del-key node-opts "kmi")
  (return (-/install (substrate/node-create node-opts) kmi-opts)))

(defn.xt request
  "issues a KMI request using space_id from opts or the default session"
  {:added "4.1"}
  [node action source opts]
  (:= opts (or opts {}))
  (return (substrate/request node
                             (or (xt/x:get-key opts "space_id") -/DEFAULT_SPACE)
                             action [source] opts)))

(defn.xt read-string [node source opts]
  (return (-/request node "@kmi.lang/read" source opts)))

(defn.xt eval-string [node source opts]
  (return (-/request node "@kmi.lang/eval" source opts)))

(defn.xt load-string [node source opts]
  (return (-/request node "@kmi.lang/load" source opts)))

(defn.xt describe-string [node source opts]
  (return (-/request node "@kmi.lang/describe" source opts)))

(defn.xt stop
  "placeholder stop for a runtime node"
  {:added "4.1"}
  [node]
  (return true))
