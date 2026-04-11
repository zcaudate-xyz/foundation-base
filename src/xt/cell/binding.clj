(ns xt.cell.binding
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.service :as service]
             [xt.lang.common-spec :as xt]]
   :export  [MODULE]})

(defn.xt resolve-section
  "resolves a named db inside a descriptor section"
  {:added "4.0"}
  [service-registry descriptor section-key view-context]
  (var section (xt/x:get-key descriptor section-key))
  (when (xt/x:nil? section)
    (return [true nil]))
  (var db-ref (xt/x:get-key section "db"))
  (when (and (xt/x:is-string? db-ref)
             (xt/x:nil? (service/get-db service-registry db-ref)))
    (return [false {:status "error"
                    :tag "xt.cell.binding/db-not-found"
                    :data {:section section-key
                           :db db-ref}}]))
  (var resolved-db (service/resolve-db service-registry section view-context))
  (return [true (:? resolved-db
                    (xt/x:obj-assign (xt/x:obj-clone section)
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
                 "resolve" (xt/x:get-key descriptor "resolve")
                 "deps" (or (xt/x:get-key descriptor "deps") [])
                 "trigger" (xt/x:get-key descriptor "trigger")
                 "options" (or (xt/x:get-key descriptor "options") {})
                 "default_args" (xt/x:get-key descriptor "default_args")
                 "default_output" (xt/x:get-key descriptor "default_output")
                 "default_process" (xt/x:get-key descriptor "default_process")
                 "default_init" (xt/x:get-key descriptor "default_init")}]))

(defn.xt compile-model
  "compiles all views for a model using a provided view compiler"
  {:added "4.0"}
  [service-registry model-id views compile-view-fn]
  (var out {})
  (xt/for:object [[view-id descriptor] views]
    (var [ok prepared] (-/prepare-view service-registry model-id view-id descriptor))
    (when (not ok)
      (return [ok prepared]))
    (xt/x:set-key out view-id (compile-view-fn prepared)))
  (return [true out]))

(defn.xt compile-bindings
  "compiles declarative bindings using a provided view compiler"
  {:added "4.0"}
  [service-registry bindings compile-view-fn]
  (var out {})
  (xt/for:object [[model-id views] bindings]
    (var [ok compiled-model] (-/compile-model service-registry model-id views compile-view-fn))
    (when (not ok)
      (return [ok compiled-model]))
    (xt/x:set-key out model-id compiled-model))
  (return [true out]))

(def.xt MODULE (!:module))
