(ns dart.ui.view-demos-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as xts]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [xt.substrate.view-demos :as demos]
             [dart.ui.view.runtime :as runtime]]})

(defn.dt make-runtime
  [render-fn]
  (var node (substrate/node-create {}))
  (var spec (view/view-spec "demo" {} nil))
  (return (runtime/runtime-create node spec render-fn {"space_id" "app"})))

(defn.dt collect-nodes
  [value out]
  (when (xt/x:is-array? value)
    (xt/for:array [item value]
      (-/collect-nodes item out)))
  (when (xt/x:is-object? value)
    (xt/x:arr-push out value)
    (-/collect-nodes (or (xt/x:get-key value "children") []) out))
  (return out))

(defn.dt has-type?
  [nodes type]
  (var found false)
  (xt/for:array [node nodes]
    (when (== type (xt/x:get-key node "type"))
      (:= found true)))
  (return found))

^{:refer dart.ui.view-demos-test/make-runtime :added "4.1"}
(fact "prepares the kitchen sink as a Wind JSON/action bundle"
  (!.dt
   (var rt (-/make-runtime demos/kitchen-sink-render))
   (var bundle (runtime/prepare rt))
   (var nodes (-/collect-nodes (xt/x:get-key bundle "json") []))
   (var actions (xt/x:get-key bundle "actions"))
   [(xt/x:get-key (xt/x:get-key bundle "json") "type")
    (> (xt/x:len nodes) 40)
    (>= (xt/x:len (xt/x:obj-keys actions)) 3)
    (-/has-type? nodes "WDiv")
    (-/has-type? nodes "WText")
    (-/has-type? nodes "WButton")
    (-/has-type? nodes "WInput")
    (-/has-type? nodes "WImage")
    (-/has-type? nodes "WIcon")])
  => ["WDiv" true true true true true true true true])

^{:refer dart.ui.view-demos-test/collect-nodes :added "4.1"}
(fact "maps variants, table lowering and event actions into the bundle"
  (!.dt
   (var rt (-/make-runtime demos/kitchen-sink-render))
   (var bundle (runtime/prepare rt))
   (var nodes (-/collect-nodes (xt/x:get-key bundle "json") []))
   (var destructive false)
   (var table-head false)
   (xt/for:array [node nodes]
     (var props (xt/x:get-key node "props"))
     (when (== "bg-red-600 text-white" (xt/x:get-key props "className"))
       (:= destructive true))
     (when (== "Price" (xt/x:get-key props "text"))
       (:= table-head true)))
   [destructive table-head])
  => [true true])

^{:refer xt.substrate.view-demos/web-escape-render :added "4.1"}
(fact "rejects fg/ escape nodes on the Wind backend"
  (!.dt
   (var rt (-/make-runtime demos/web-escape-render))
   (var rejected false)
   (try
    (runtime/prepare rt)
    (catch err (:= rejected true)))
   rejected)
  => true)
