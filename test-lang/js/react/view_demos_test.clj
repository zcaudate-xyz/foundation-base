(ns js.react.view-demos-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [xt.substrate.view-demos :as demos]
             [js.react.view.runtime :as runtime]]})

(defn.js make-runtime
  [render-fn]
  (var node (substrate/node-create {}))
  (var spec (view/view-spec "demo" {} nil))
  (return (runtime/runtime-create node spec render-fn {"space_id" "app"})))

(defn.js collect-props
  [value out]
  (when (xt/x:is-array? value)
    (xt/for:array [item value]
      (-/collect-props item out)))
  (when (xt/x:is-object? value)
    (var props (xt/x:get-key value "props"))
    (when (xt/x:is-object? props)
      (xt/x:arr-push out props))
    (-/collect-props (xt/x:get-key props "children") out))
  (return out))

^{:refer js.react.view-demos-test/make-runtime :added "4.1"}
(fact "renders the kitchen sink as nested figma/DOM elements"
  (!.js
   (var rt (-/make-runtime demos/kitchen-sink-render))
   (var element (runtime/render rt {}))
   (var props (-/collect-props element []))
   (var variants {})
   (xt/for:array [p props]
     (var v (xt/x:get-key p "variant"))
     (when (xt/x:is-string? v)
       (xt/x:set-key variants v true)))
   [(xt/x:get-key element "type")
    (xt/x:has-key? (xt/x:get-key element "props") "className")
    (> (xt/x:len props) 30)
    (xt/x:get-key variants "destructive")
    (xt/x:get-key variants "secondary")
    (xt/x:get-key variants "outline")])
  => ["div" true true true true true])

^{:refer js.react.view-demos-test/collect-props :added "4.1"}
(fact "renders hidden nodes out of the kitchen sink"
  (!.js
   (var rt (-/make-runtime demos/kitchen-sink-render))
   (var element (runtime/render rt {}))
   (var props (-/collect-props element []))
   (var found false)
   (xt/for:array [p props]
     (when (== "you should not see this" (xt/x:get-key p "value"))
       (:= found true)))
   found)
  => false)

^{:refer xt.substrate.view-demos/web-escape-render :added "4.1"}
(fact "renders fg/ escape nodes against the figma package"
  (!.js
   (var rt (-/make-runtime demos/web-escape-render))
   (var element (runtime/render rt {}))
   (var props (-/collect-props element []))
   (var tag (xt/x:get-key (xt/x:get-idx (xt/x:get-key (xt/x:get-key element "props") "children") 2) "type"))
   [(xt/x:not-nil? tag)
    (xt/x:is-string? tag)
    (> (xt/x:len props) 3)])
  => [true false true])
