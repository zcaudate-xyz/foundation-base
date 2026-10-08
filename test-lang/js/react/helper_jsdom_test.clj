(ns js.react.helper-jsdom-test
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.helper-jsdom/setup :added "4.1"}
(fact "creates an isolated DOM and installs the React globals"
  (!.js
   (var env (helper/setup {"rootId" "mount"
                           "html" "<!doctype html><html><body></body></html>"}))
   (var result {"tag" (. env ["root"] ["tagName"])
                "id" (. env ["root"] ["id"])
                "window" (== (. env ["window"]) (!:G window))
                "document" (== (. env ["document"]) (!:G document))
                "act" (== true (!:G IS_REACT_ACT_ENVIRONMENT))})
   (helper/teardown env)
   (return result))
  => {"tag" "DIV"
      "id" "mount"
      "window" true
      "document" true
      "act" true})

^{:refer js.react.helper-jsdom/render :added "4.1"}
(fact "renders a component and waits for its effect"
  (notify/wait-on :js
    (var env (helper/setup {}))
    (var React (require "react"))
    (var Counter
         (fn []
           (var [value setValue] (React.useState "before"))
           (React.useEffect (fn [] (setValue "after")) [])
           (return (React.createElement "span" nil value))))
    (. (helper/render env Counter {})
       (then (fn [_]
               (var html document.body.innerHTML)
               (var first (helper/teardown env))
               (var second (helper/teardown env))
               (repl/notify {"html" html
                             "first" first
                             "second" second
                             "closed" (. env ["closed"])
                             "restored" (== "undefined" (typeof window))})))))
  => {"html" "<div id=\"root\"><span>after</span></div>"
      "first" true
      "second" true
      "closed" true
      "restored" true})

^{:refer js.react.helper-jsdom/teardown :added "4.1"}
(fact "restores global descriptors and is idempotent"
  (notify/wait-on :js
    (var original (Object.getOwnPropertyDescriptor globalThis "navigator"))
    (var env (helper/setup {}))
    (var changed (not= original
                      (Object.getOwnPropertyDescriptor globalThis "navigator")))
    (var restored (helper/teardown env))
    (var current (Object.getOwnPropertyDescriptor globalThis "navigator"))
    (repl/notify {"changed" changed
                  "restored" restored
                  "same" (and (== (. original ["configurable"])
                                  (. current ["configurable"]))
                              (== (. original ["enumerable"])
                                  (. current ["enumerable"])))
                  "second" (helper/teardown env)}))
  => {"changed" true
      "restored" true
      "same" true
      "second" true})

^{:refer js.react.helper-jsdom/run :added "4.1"}
(fact "waits for an async callback before tearing down JSDOM"
  (notify/wait-on :js
    (. (helper/run {}
                   (fn [_]
                     (return
                      (. (Promise.resolve nil)
                         (then (fn [_]
                                 (return "ready")))))))
       (then (repl/>notify))))
  => "ready"

  (!.js
   (return (== "undefined" (typeof window))))
  => true)

^{:refer js.react.helper-jsdom/wait-on :added "4.1"}
(fact "sets up and tears down JSDOM around a React callback"
  (helper-source/wait-on
   {}
   (var React (require "react"))
   (var Counter
        (fn []
          (var [value setValue] (React.useState "before"))
          (React.useEffect (fn [] (setValue "after")) [])
          (return (React.createElement "span" nil value))))
   (return
    (. (helper/render env Counter {})
       (then (fn [_]
               (return document.body.innerHTML))))))
  => "<div id=\"root\"><span>after</span></div>"

  (!.js
   (return (== "undefined" (typeof window))))
  => true)

