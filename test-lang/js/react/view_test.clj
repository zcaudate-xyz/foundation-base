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
(fact "keeps catalog-native ids and rejects unresolved component ids"
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
  => ["ui/card" true])

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

^{:refer js.react.view.backend/dom-props :added "4.1"}
(fact "renders a figma button with variant, class and press wiring"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_]
              (return (view/node "ui/button"
                                 {"variant" "destructive"
                                  "class" "w-full"
                                  "disabled" true
                                  "on_press" (view/action "sample/save" nil)}
                                 ["Delete"])))
            {"space_id" "app"}))
   (var element (runtime/render rt {}))
   (var props (xt/x:get-key element "props"))
   (var tag (xt/x:get-key element "type"))
   [(xt/x:not-nil? tag)
    (xt/x:is-string? tag)
    (xt/x:get-key props "variant")
    (xt/x:get-key props "className")
    (xt/x:get-key props "disabled")
    (xt/x:is-function? (xt/x:get-key props "onClick"))
    (xt/x:first (xt/x:get-key props "children"))])
  => [true false "destructive" "w-full" true true "Delete"])

^{:refer js.react.view.backend/native-entry :added "4.1"}
(fact "resolves fg/ escape ids against the figma package"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_] (return (view/node "fg/button" {"class" "x"} ["Hi"])))
            {"space_id" "app"}))
   (var element (runtime/render rt {}))
   (var tag (xt/x:get-key element "type"))
   [(xt/x:not-nil? tag)
    (xt/x:is-string? tag)
    (xt/x:get-key (xt/x:get-key element "props") "className")])
  => [true false "x"])

^{:refer js.react.view.runtime/render :id test-render-hidden :added "4.1"}
(fact "renders hidden nodes as nil"
  (!.js
   (var node (substrate/node-create {}))
   (var spec (view/view-spec "sample" {} nil))
   (var rt (runtime/runtime-create
            node spec
            (fn [_] (return (view/node "ui/text" {"value" "x" "hidden" true} [])))
            {"space_id" "app"}))
   (runtime/render rt {}))
  => nil)

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
