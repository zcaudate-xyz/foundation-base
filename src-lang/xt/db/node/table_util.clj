(ns xt.db.node.table-util
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.db.node.client-base :as client]]})

(defn.xt context-node
  "gets the node from a table utility context"
  {:added "4.1"}
  [context]
  (return (xt/x:get-key context "node")))

(defn.xt context-primary-id
  "gets the primary service id from a table utility context"
  {:added "4.1"}
  [context]
  (return (xt/x:get-key context "primary-id" "db/primary")))

(defn.xt context-opts
  "gets request options from a table utility context"
  {:added "4.1"}
  [context]
  (return (xt/x:get-key context "opts" {})))

(defn.xt request-data
  "runs a native pull, dataview, or rpc request through the node"
  {:added "4.1"}
  [context request-type request-kind spec entry]
  (var node (-/context-node context))
  (var primary-id (-/context-primary-id context))
  (var opts (-/context-opts context))
  (cond
    (== request-kind "pull")
    (if (== request-type "cached")
      (return (client/pull-cached node primary-id spec opts))
      (return (client/pull-call node primary-id spec opts)))

    (== request-kind "view")
    (if (== request-type "cached")
      (return (client/dataview-cached node primary-id spec opts))
      (return (client/dataview-call node primary-id spec opts)))

    (== request-kind "rpc")
    (do
      (var rpc-spec (or (xt/x:get-key entry "rpc-spec")
                        (xt/x:get-key spec "rpc-spec")))
      (var rpc-args (or (xt/x:get-key entry "rpc-args")
                        (xt/x:get-key spec "rpc-args")
                        []))
      (return (client/rpc-call node primary-id rpc-spec rpc-args opts)))

    :else
    (return nil)))

(defn.xt table-impl-entry
  "merges the base table entry with a node request entry"
  {:public true
   :added "4.1"}
  [impl request-type]
  (return
   (xtd/arr-foldl ["base" request-type]
                  (fn [acc key]
                    (return (xtd/obj-assign
                             acc
                             (or (xt/x:get-key impl key) {}))))
                  {})))

(defn.xt table-check-entry
  "checks whether a table list entry accepts the selection input"
  {:public true
   :added "4.1"}
  [entry sel-input]
  (var list-entry (xt/x:get-key entry "list"))
  (when (or (k/nil? entry)
            (k/nil? list-entry))
    (return false))
  (return ((or (xt/x:get-key list-entry "check") k/T) sel-input)))

(defn.xt table-level-entry
  "gets a request-level entry from a merged table implementation"
  {:added "4.1"}
  [entry request-level]
  (return (xt/x:get-key entry request-level)))

(defn.xt table-level-spec
  "gets a native query specification, preferring explicit input"
  {:added "4.1"}
  [entry input]
  (return (or input
              (xt/x:get-key entry "spec")
              (xt/x:get-key entry "query"))))

(defn.xt table-request-kind
  "normalises a table entry type to a node request kind"
  {:added "4.1"}
  [entry spec]
  (var type (xt/x:get-key entry "type"))
  (cond (or (== type "raw")
            (== type "pull")
            (xt/x:is-array? spec))
        (return "pull")
        (or (== type "custom")
            (== type "rpc"))
        (return "rpc")
        :else
        (return "view")))

(defn.xt table-rows
  "normalises a node request result to rows"
  {:added "4.1"}
  [result]
  (return (:? (xt/x:is-array? result) result [])))

(defn.xt table-ids-from
  "extracts non-nil ids from returned rows"
  {:added "4.1"}
  [result id-key]
  (return
   (xtd/arr-filter
    (xtd/arr-map (-/table-rows result)
                 (fn [row]
                   (return (xt/x:get-key row (or id-key "id")))))
    k/identity)))

(defn.xt table-pull-with-data
  "adds a data projection to a native pull tree"
  {:added "4.1"}
  [spec data]
  (when (or (k/nil? spec)
            (not (xt/x:is-array? spec)))
    (return spec))
  (var out (xt/x:arr-clone spec))
  (var params (xtd/obj-clone (or (xt/x:get-idx out 1) {})))
  (xt/x:set-key params "data"
                (xtd/arr-union (or (xt/x:get-key params "data") [])
                               (or data [])))
  (xt/x:set-idx out 1 params)
  (return out))

(defn.xt table-pull-with-ids
  "adds an id filter to a native pull tree or dataview"
  {:added "4.1"}
  [spec ids]
  (when (k/nil? spec)
    (return spec))
  (cond
    (xt/x:is-array? spec)
    (do
      (var out (xt/x:arr-clone spec))
      (var params (xtd/obj-clone (or (xt/x:get-idx out 1) {})))
      (var where (xt/x:arr-clone (or (xt/x:get-key params "where") [])))
      (xt/x:arr-push where {"id" ["in" ids]})
      (xt/x:set-key params "where" where)
      (xt/x:set-idx out 1 params)
      (return out))
    (xt/x:is-object? spec)
    (return (xtd/obj-assign (xtd/obj-clone spec)
                            {"return_bulk" ids}))
    :else
    (return spec)))

(defn.xt table-ids
  "gets table ids through the node runtime"
  {:public true
   :added "4.1"}
  [impl request-type sel-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
  (var list-entry (-/table-level-entry entry "list"))
  (var spec (-/table-level-spec list-entry sel-input))
  (var kind (-/table-request-kind list-entry spec))
  (return
   (promise/x:promise-then
    (-/request-data context request-type kind spec list-entry)
    (fn [result]
      (return (-/table-ids-from result
                              (xt/x:get-key list-entry "id-key" "id")))))))

(defn.xt table-ids-updated
  "gets table ids and update values through the node runtime"
  {:public true
   :added "4.1"}
  [impl request-type sel-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
  (var list-entry (-/table-level-entry entry "list"))
  (var update-key (xt/x:get-key list-entry "update-key" "time_updated"))
  (var spec (or (xt/x:get-key list-entry "updated-spec")
                (-/table-pull-with-data
                 (-/table-level-spec list-entry sel-input)
                 ["id" update-key])))
  (return (-/request-data context
                        request-type
                        (-/table-request-kind list-entry spec)
                        spec
                        list-entry)))

(defn.xt table-ids-data
  "gets records for a set of table ids through the node runtime"
  {:public true
   :added "4.1"}
  [impl request-type request-level ids ret-input context]
  (:= ids (xtd/arr-filter (or ids []) k/identity))
  (when (== 0 (xt/x:len ids))
    (return (promise/x:promise-run [])))
  (var entry (-/table-impl-entry impl request-type))
  (var level-entry (-/table-level-entry entry request-level))
  (when (or (k/nil? entry)
            (k/nil? level-entry))
    (return))
  (var spec (-/table-pull-with-ids
             (-/table-level-spec level-entry ret-input)
             ids))
  (return (-/request-data context
                        request-type
                        (-/table-request-kind level-entry spec)
                        spec
                        level-entry)))

(defn.xt table-pull
  "pulls table data through the node runtime"
  {:public true
   :added "4.1"}
  [impl request-type request-level sel-input ret-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
  (return
   (promise/x:promise-then
    (-/table-ids impl request-type sel-input context)
    (fn [ids]
      (return (-/table-ids-data impl request-type request-level
                              (xtd/arr-filter (or ids []) k/identity)
                              ret-input context))))))

(defn.xt sync-unknown-ids
  "gets ids present in only one of cached and primary data"
  {:public true
   :added "4.1"}
  [impl sel-input context]
  (return
   (promise/x:promise-then
    (promise/x:promise-all
     [(-/table-ids impl "cached" sel-input context)
      (-/table-ids impl "call" sel-input context)])
    (fn [values]
      (return (xtd/arr-union
               (xtd/arr-difference (or (xt/x:get-idx values 0) [])
                                   (or (xt/x:get-idx values 1) []))
               (xtd/arr-difference (or (xt/x:get-idx values 1) [])
                                   (or (xt/x:get-idx values 0) []))))))))

(defn.xt sync-outdated-ids
  "gets primary ids whose update value differs from the cache"
  {:public true
   :added "4.1"}
  [impl sel-input context]
  (var cached-entry (-/table-impl-entry impl "cached"))
  (when (not (-/table-check-entry cached-entry sel-input))
    (return))
  (var list-entry (-/table-level-entry cached-entry "list"))
  (var update-key (xt/x:get-key list-entry "update-key" "time_updated"))
  (return
   (promise/x:promise-then
    (promise/x:promise-all
     [(-/table-ids-updated impl "cached" sel-input context)
      (-/table-ids-updated impl "call" sel-input context)])
    (fn [values]
      (var cached-map {})
      (xt/for:array [row (-/table-rows (xt/x:get-idx values 0))]
        (xt/x:set-key cached-map
                      (xt/x:get-key row "id")
                      (xt/x:get-key row update-key)))
      (var out [])
      (xt/for:array [row (-/table-rows (xt/x:get-idx values 1))]
        (var id (xt/x:get-key row "id"))
        (when (or (not (xt/x:has-key? cached-map id))
                  (not (== (xt/x:get-key row update-key)
                            (xt/x:get-key cached-map id))))
          (xt/x:arr-push out id)))
      (return out)))))

(defn.xt sync-unknown
  "gets primary records for ids missing from the cache"
  {:public true
   :added "4.1"}
  [impl request-level sel-input ret-input context]
  (return
   (promise/x:promise-then
    (-/sync-unknown-ids impl sel-input context)
    (fn [ids]
      (return (:? (xtd/not-empty? ids)
                   (-/table-ids-data impl "call" request-level ids ret-input context)
                   []))))))

(defn.xt sync-outdated
  "gets primary records for ids with changed update values"
  {:public true
   :added "4.1"}
  [impl request-level sel-input ret-input context]
  (return
   (promise/x:promise-then
    (-/sync-outdated-ids impl sel-input context)
    (fn [ids]
      (return (:? (xtd/not-empty? ids)
                   (-/table-ids-data impl "call" request-level ids ret-input context)
                   []))))))

(defn.xt action-fn
  "creates an action backed by a node-runtime RPC"
  {:public true
   :added "4.1"}
  [action context]
  (var node (-/context-node context))
  (var primary-id (-/context-primary-id context))
  (var opts (-/context-opts context))
  (var rpc-spec (xt/x:get-key action "rpc-spec"))
  (var static-args (xt/x:get-key action "rpc-args"))
  (var args-fn (xt/x:get-key action "args-fn"))
  (var output (or (xt/x:get-key action "output") k/identity))
  (return
   (fn [...args]
     (var rpc-args (:? (xt/x:is-function? args-fn)
                        (args-fn args context)
                        (:? (xt/x:not-nil? static-args)
                            static-args
                            args)))
     (return
      (promise/x:promise-then
       (client/rpc-call node primary-id rpc-spec rpc-args opts)
       (fn [result]
         (return (output result))))))))

(defn.xt make-actions
  "creates all node-runtime actions from an implementation"
  {:public true
   :added "4.1"}
  [impl context]
  (return
   (xtd/obj-map (or (xt/x:get-key impl "actions") {})
                (fn [action]
                  (return (-/action-fn action context))))))
