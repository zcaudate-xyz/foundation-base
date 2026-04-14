(ns js.cell.binding
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[js.cell.service :as service]
             [xt.lang.common-lib :as k]]
   :export  [MODULE]})

(defn.xt resolve-section
  "resolves a named db inside a descriptor section"
  {:added "4.0"}
  [service-registry descriptor section-key view-context]
  (var section (k/get-key descriptor section-key))
  (when (k/nil? section)
    (return [true nil]))
  (var db-ref (k/get-key section "db"))
  (when (and (k/is-string? db-ref)
             (k/nil? (service/get-db service-registry db-ref)))
    (return [false {:status "error"
                    :tag "js.cell.binding/db-not-found"
                    :data {:section section-key
                           :db db-ref}}]))
  (var resolved-db (service/resolve-db service-registry section view-context))
  (return [true (:? resolved-db
                    (k/obj-assign (k/obj-clone section)
                                  {"db" resolved-db})
                    section)]))

(defn.xt prepare-view
  "prepares a declarative view descriptor for compilation"
  {:added "4.0"}
  [service-registry model-id view-id descriptor]
  (var view-context {"model-id" model-id
                     "view-id" view-id})
  (var [q-ok query] (-/resolve-section service-registry descriptor "query" view-context))
  (when (not q-ok)
    (return [q-ok query]))
  (var [s-ok sync] (-/resolve-section service-registry descriptor "sync" view-context))
  (when (not s-ok)
    (return [s-ok sync]))
  (var [r-ok remote] (-/resolve-section service-registry descriptor "remote" view-context))
  (when (not r-ok)
    (return [r-ok remote]))
  (var [st-ok stream] (-/resolve-section service-registry descriptor "stream" view-context))
  (when (not st-ok)
    (return [st-ok stream]))
  (return [true {"model_id" model-id
                 "view_id" view-id
                 "query" query
                 "sync" sync
                 "remote" remote
                 "stream" stream
                 "resolve" (k/get-key descriptor "resolve")
                 "deps" (or (k/get-key descriptor "deps") [])
                 "trigger" (k/get-key descriptor "trigger")
                 "options" (or (k/get-key descriptor "options") {})
                 "default_args" (k/get-key descriptor "default_args")
                 "default_output" (k/get-key descriptor "default_output")
                 "default_process" (k/get-key descriptor "default_process")
                 "default_init" (k/get-key descriptor "default_init")}]))

(defn.xt compile-model
  "compiles all views for a model using a provided view compiler"
  {:added "4.0"}
  [service-registry model-id views compile-view-fn]
  (var out {})
  (k/for:object [[view-id descriptor] views]
    (var [ok prepared] (-/prepare-view service-registry model-id view-id descriptor))
    (when (not ok)
      (return [ok prepared]))
    (k/set-key out view-id (compile-view-fn prepared)))
  (return [true out]))

(defn.xt compile-bindings
  "compiles declarative bindings using a provided view compiler"
  {:added "4.0"}
  [service-registry bindings compile-view-fn]
  (var out {})
  (k/for:object [[model-id views] bindings]
    (var [ok compiled-model] (-/compile-model service-registry model-id views compile-view-fn))
    (when (not ok)
      (return [ok compiled-model]))
    (k/set-key out model-id compiled-model))
  (return [true out]))

(def.xt MODULE (!:module))
