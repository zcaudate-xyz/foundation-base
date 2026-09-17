^{:seedgen/skip true}
(ns xt.ui.react-playground-ui-test
  "Node-hosted behavior tests; browser serving remains in react-playground-test."
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

;; This adapter is intentionally JavaScript/React-specific, not a portable seed.
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.ui.core :as ui]
             [xt.ui.state.core :as state]
             [js.react :as r]
             [xt.ui.react-playground-ui :as demo]]})

(defn.js with-react
  "uses the runtime import or temporarily supplies the playground's React global"
  [run]
  ;; Referencing js.react can install a read-only native import. Leave it intact.
  (when (xt/x:has-key? globalThis "React")
    (return (run)))
  (xt/x:set-key globalThis "React" (require "react"))
  (try
    (return (run))
    (catch err (throw err))
    (finally
      (xt/x:del-key globalThis "React"))))

^{:refer xt.ui.react-playground-ui/render-ui-node :added "4.1"}
(fact "recursively renders descriptors while preserving scalar and missing-renderer behavior"
  (!.js
   (var registry {"test/node" (fn [props children] (return {"props" props "children" children}))})
   [(demo/render-ui-node registry nil)
    (demo/render-ui-node registry ["text" 0])
    (demo/render-ui-node registry (ui/node "unknown" {} []))
    (demo/render-ui-node registry
     (ui/node "test/node" {"id" "root"} [(ui/node "test/node" {"id" "child"} [3])]))
    (demo/render-ui-node registry {"component" "test/node"})])
  => [nil ["text" "0"] nil
      {"props" {"id" "root"} "children" [{"props" {"id" "child"} "children" ["3"]}]}
      {"props" {} "children" nil}])

^{:refer xt.ui.react-playground-ui/react-registry :added "4.1"}
(fact "React renderers adapt element props and forward native event values"
  (!.js
   (-/with-react
    (fn []
      (var registry (demo/react-registry))
      (var seen [])
      (var input ((xt/x:get-key registry "ui/input")
                  {"value" "Ada" "placeholder" "Name" "disabled" true
                   "on_change" (fn [value] (xt/x:arr-push seen value) (return value))} []))
      (var button ((xt/x:get-key registry "ui/button")
                   {"label" "Save" "on_press" (fn [] (xt/x:arr-push seen "pressed") (return "saved"))} []))
      (var column ((xt/x:get-key registry "ui/column") {"gap" "8px" "padding" "4px"} ["child"]))
      (var row ((xt/x:get-key registry "ui/row") {} []))
      (var change-result ((xt/x:get-path input ["props" "onChange"]) {"target" {"value" "Grace"}}))
      (var press-result ((xt/x:get-path button ["props" "onClick"])))
      (return
       [(. input ["type"]) (xt/x:get-path input ["props" "value"])
        (xt/x:get-path input ["props" "placeholder"]) (xt/x:get-path input ["props" "disabled"])
        (. button ["type"]) (xt/x:get-path button ["props" "children"])
        (xt/x:get-path column ["props" "style"]) (xt/x:get-path row ["props" "style" "flexDirection"])
        change-result press-result seen]))))
  => ["input" "Ada" "Name" true "button" "Save"
      {"display" "flex" "flexDirection" "column" "gap" "8px" "padding" "4px"}
      "row" "Grace" "saved" ["Grace" "pressed"]])

^{:refer xt.ui.react-playground-ui/make-controller :added "4.1"}
(fact "list actions trim new items, ignore blank drafts and remove only the requested index"
  (notify/wait-on :js
    (var controller (demo/make-controller))
    (var initial (xt/x:json-encode (state/snapshot controller)))
    (-> (state/dispatch! controller "set_draft" "  gamma  ")
        (promise/x:promise-then (fn [_] (return (state/dispatch! controller "add_item" nil))))
        (promise/x:promise-then (fn [_] (return (state/dispatch! controller "set_draft" "   "))))
        (promise/x:promise-then (fn [_] (return (state/dispatch! controller "add_item" nil))))
        (promise/x:promise-then (fn [_] (return (state/dispatch! controller "remove_item" 1))))
        (promise/x:promise-then (fn [_] (repl/notify [(xt/x:json-decode initial) (state/snapshot controller)])))))
  => [{"items" ["alpha" "beta"] "draft" ""}
      {"items" ["alpha" "gamma"] "draft" "   "}])

^{:refer xt.ui.react-playground-ui/view :added "4.1"}
(fact "portable list views expose draft actions and preserve each row's removal index"
  (!.js
   (var seen [])
   (var tree
        (demo/view {"items" ["alpha" "beta"] "draft" "gamma"}
         {"set_draft" (fn [value] (xt/x:arr-push seen ["draft" value]))
          "add_item" (fn [] (xt/x:arr-push seen ["add"]))
          "remove_item" (fn [index] (xt/x:arr-push seen ["remove" index]))}))
   ((xt/x:get-path tree ["children" 1 "props" "on_change"]) "delta")
   ((xt/x:get-path tree ["children" 2 "props" "on_press"]))
   ((xt/x:get-path tree ["children" 3 "children" 0 "children" 1 "props" "on_press"]))
   ((xt/x:get-path tree ["children" 3 "children" 1 "children" 1 "props" "on_press"]))
   [(. tree ["component"]) (xt/x:get-path tree ["children" 1 "props" "value"])
    (xt/x:get-path tree ["children" 3 "children" 0 "children" 0 "props" "value"])
    (xt/x:get-path tree ["children" 3 "children" 1 "props" "key"]) seen])
  => ["ui/column" "gamma" "alpha" 1
      [["draft" "delta"] ["add"] ["remove" 0] ["remove" 1]]])

^{:refer xt.ui.react-playground-ui/App :added "4.1"}
(fact "the real React component renders its initial state with hooks under server rendering"
  (!.js
   (-/with-react
    (fn []
      (var server (require "react-dom/server"))
      (var html (. server (renderToStaticMarkup (r/createElement demo/App nil))))
      (return
       [(. html (includes "xt.ui React Playground"))
        (. html (includes "alpha")) (. html (includes "beta"))
        (. html (includes "New item...")) (. html (includes "Remove"))]))))
  => [true true true true true])

^{:refer xt.ui.react-playground-ui/mount! :added "4.1"}
(fact "mount hands a real App element to the playground stage and restores host globals"
  (!.js
   (-/with-react
    (fn []
      (var had-window (xt/x:has-key? globalThis "window"))
      (var previous (xt/x:get-key globalThis "window"))
      (var captured nil)
      (var result nil)
      (xt/x:set-key globalThis "window"
                    {"PLAYGROUND" {"setStage" (fn [element] (:= captured element))}})
      (try
        (:= result (demo/mount!))
        (catch err (throw err))
        (finally
          (if had-window
            (xt/x:set-key globalThis "window" previous)
            (xt/x:del-key globalThis "window"))))
      (return [result (r/isValidElement captured) (== (. captured ["type"]) demo/App)]))))
  => [true true true])
