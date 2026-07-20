(ns dart.ui.view-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [dart.ui.view.runtime :as runtime]]})

^{:refer dart.ui.view.runtime/prepare :added "4.1"}
(fact "prepares portable IR as a Wind JSON/action bundle"
  (!.dt
   (var node (substrate/node-create {}))
   (substrate/register-handler
    node "sample/save" (fn [_space args _frame _node] (return (xt/x:first args))) {})
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_]
              (return
               (view/node "ui/card" {}
                          [(view/node "ui/button"
                                      {"on_press" (view/action "sample/save" nil)}
                                      ["Save"])])))
            {"space_id" "app"}))
   (var bundle (runtime/prepare rt))
   [(xt/x:get-key (xt/x:get-key bundle "json") "type")
    (xt/x:len (xt/x:obj-keys (xt/x:get-key bundle "actions")))])
  => ["WDiv" 1])

^{:refer dart.ui.view.runtime/open :added "4.1"}
(fact "opens and closes the unified substrate subscription"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec
              "sample"
              {"name" {"source" "state" "space_id" "app" "path" ["name"]}}
              nil))
   (var calls 0)
   (var rt (runtime/runtime-create node spec (fn [_] (return nil))
                                   {"space_id" "app"}))
   (runtime/open rt (fn [_snapshot _revision _event]
                      (:= calls (+ calls 1))))
   (view/state-set node "app" "sample" ["name"] "Ada")
   (runtime/close rt)
   (view/state-set node "app" "sample" ["name"] "Grace")
   calls)
  => 1)

^{:refer dart.ui.view.runtime/resolve-node :added "4.1"}
(fact "rejects missing implementations and cyclic Wind polyfills"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create node spec (fn [_] nil) {"space_id" "app"}))
   (var missing false)
   (try
     (runtime/resolve-node rt (view/node "ui/missing" {} []) {})
     (catch err (:= missing true)))
   (var registry (xt/x:get-key rt "registry"))
   (xt/x:set-key (xt/x:get-key registry "polyfills") "ui/a"
                 (fn [_] (return (view/node "ui/a" {} []))))
   (var cyclic false)
   (try
     (runtime/resolve-node rt (view/node "ui/a" {} []) {})
     (catch err (:= cyclic true)))
   [missing cyclic])
  => [true true])

^{:refer dart.ui.view.backend/props :added "4.1"}
(fact "maps classes and variants into the Wind className"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_]
              (return
               (view/node "ui/column" {"class" "gap-4 p-5"}
                          [(view/node "ui/button" {"variant" "destructive"}
                                      ["Delete"])])))
            {"space_id" "app"}))
   (var bundle (runtime/prepare rt))
   (var json (xt/x:get-key bundle "json"))
   (var button (xt/x:first (xt/x:get-key json "children")))
   [(xt/x:get-key (xt/x:get-key json "props") "className")
    (xt/x:get-key (xt/x:get-key button "props") "className")])
  => ["flex flex-col gap-4 p-5" "bg-red-600 text-white"])

^{:refer dart.ui.view.backend/native-entry :added "4.1"}
(fact "rejects fg/ platform ids on the Wind backend"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create node spec (fn [_] nil) {"space_id" "app"}))
   (var rejected false)
   (try
     (runtime/resolve-node rt (view/node "fg/button" {} []) {})
     (catch err (:= rejected true)))
   rejected)
  => true)

^{:refer dart.ui.view.polyfill/registry :added "4.1"}
(fact "lowers the table family to layout and text primitives"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_]
              (return
               (view/node "ui/table" {}
                          [(view/node "ui/table-row" {}
                                      [(view/node "ui/table-cell" {"value" "a"} [])])])))
            {"space_id" "app"}))
   (var json (xt/x:get-key (runtime/prepare rt) "json"))
   (var row (xt/x:first (xt/x:get-key json "children")))
   (var cell (xt/x:first (xt/x:get-key row "children")))
   [(xt/x:get-key json "type")
    (xt/x:get-key (xt/x:get-key row "props") "className")
    (xt/x:get-key cell "type")
    (xt/x:get-key (xt/x:get-key cell "props") "text")])
  => ["WDiv" "flex flex-row " "WText" "a"])

^{:refer dart.ui.view.runtime/prepare-node :added "4.1"}
(fact "prepares hidden nodes as empty text"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_] (return (view/node "ui/text" {"value" "x" "hidden" true} [])))
            {"space_id" "app"}))
   (var bundle (runtime/prepare rt))
   (xt/x:get-key (xt/x:get-key (xt/x:get-key bundle "json") "props") "text"))
  => "")

^{:refer dart.ui.view.runtime/local-set :added "4.1"}
(fact "keeps explicitly local bindings inside the Wind adapter"
  (!.dt
   (var node (substrate/node-create {}))
   (var spec (view/view-spec
              "sample" {"dialog" {"source" "local" "initial" false}} nil))
   (var rt (runtime/runtime-create node spec (fn [_] nil) {"space_id" "app"}))
   (var calls 0)
   (runtime/open rt (fn [_snapshot _revision _event]
                      (:= calls (+ calls 1))))
   (runtime/local-set rt "dialog" true)
   (runtime/close rt)
   [(xt/x:get-key (runtime/snapshot rt) "dialog") calls])
  => [true 1])
