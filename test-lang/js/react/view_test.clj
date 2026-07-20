(ns js.react.view-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [js.react.view.runtime :as runtime]]})

^{:refer js.react.view.runtime/resolve-node :added "4.1"}
(fact "uses React polyfills and rejects unresolved component ids"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create node spec (fn [_] nil) {"space_id" "app"}))
   (var resolved (runtime/resolve-node
                  rt (view/node "ui/card" {} []) {}))
   (var rejected false)
   (try
     (runtime/resolve-node rt (view/node "ui/unknown" {} []) {})
     (catch err (:= rejected true)))
   [(xt/x:get-key resolved "component") rejected])
  => ["ui/column" true])

^{:refer js.react.view.runtime/render :added "4.1"}
(fact "renders a portable snapshot as a native React element"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_]
              (return (view/node "ui/text" {"value" "hello"} [])))
            {"space_id" "app"}))
   (var element (runtime/render rt {}))
   [(xt/x:get-key element "type")
    (xt/x:get-key (xt/x:get-key element "props") "children")])
  => ["span" "hello"])

^{:refer js.react.view.runtime/local-set :added "4.1"}
(fact "keeps explicitly local bindings inside the React adapter"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec
              "sample" {"dialog" {"source" "local" "initial" false}} nil))
   (var rt (runtime/runtime-create node spec (fn [_] nil) {"space_id" "app"}))
   (var before (xt/x:get-key (runtime/snapshot rt) "dialog"))
   (runtime/local-set rt "dialog" true)
   [before (xt/x:get-key (runtime/snapshot rt) "dialog")])
  => [false true])
