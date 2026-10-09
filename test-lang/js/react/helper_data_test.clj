(ns js.react.helper-data-test
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]
             [js.react.helper-data :as data]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.helper-data/wrapMemoize :added "4.0"}
(fact "caches the wrapped component by identity"
  (!.js
   (var component (fn [] nil))
   (var first (data/wrapMemoize component
                                (fn [target]
                                  (return {"target" target}))))
   (var second (data/wrapMemoize component
                                 (fn [target]
                                   (return {"target" target}))))
   (return {"same" (== first second)
            "target" (== component (. first ["target"]))}))
  => {"same" true
      "target" true})

^{:refer js.react.helper-data/useWrappedComponent :added "4.0"}
(fact "passes nested context data to the wrapped component"
  (helper-source/test
   (data/wrapData
    (fn []
      (var WrappedChild
           (data/wrapData
            (fn [props]
              (return (r/createElement "span" nil
                                       (+ (. props ["value"])
                                          ":"
                                          (. props ["label"])))))
            "Child"))
      (return (r/createElement WrappedChild {"$id" "child"})))
    "Parent")
   {"$data" {"$.child" {"value" "nested"
                           "label" "context"}}}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>nested:context</span></div>")

^{:refer js.react.helper-data/wrapData :added "4.0"}
(fact "wraps a component and preserves its display name"
  (!.js
   (var Component (fn [] nil))
   (var Wrapped (data/wrapData Component "NamedComponent"))
   (return {"wrapped" (== true (. Wrapped [data/__WRAPPED__]))
            "name" (. Wrapped ["displayName"])}))
  => {"wrapped" true
      "name" "NamedComponent"})

^{:refer js.react.helper-data/wrapForward :added "4.0"}
(fact "forwards a ref through the data wrapper"
  (helper-source/test
   (fn [props]
     (var target (. props ["target"]))
     (var Wrapped
          (data/wrapForward
           (fn [props]
             (return (r/createElement "button"
                                      {"ref" (. props ["ref"])}
                                      "forwarded")))
           "ForwardedButton"))
     (return (r/createElement Wrapped {"ref" target})))
   (fn [_]
     (return {"target" (r/createRef)}))
   (fn [props document _]
     (var targetRef (. props ["target"]))
     (var target (. targetRef ["current"]))
     (var result {"html" document.body.innerHTML
                  "tag" (. target ["tagName"])})
     (return result)))
  => {"html" "<div id=\"root\"><button>forwarded</button></div>"
      "tag" "BUTTON"})
