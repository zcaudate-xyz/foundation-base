(ns js.react.helper-jsdom-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]
            [js.react.helper-jsdom :as helper-source]))

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
                 "id"  (. env ["root"] ["id"])
                 "window"   (== (. env ["window"])   (!:G window))
                 "document" (== (. env ["document"]) (!:G document))
                 "act" (== true (!:G IS_REACT_ACT_ENVIRONMENT))})
    (helper/teardown env)
    (return result))
  => {"tag" "DIV"
      "id" "mount"
      "window" true
      "document" true
      "act" true}

  (!.js
    (var first (helper/setup {}))
    (. localStorage (setItem "shared" "first"))
    (var firstValue (. localStorage (getItem "shared")))
    (helper/teardown first)
    (var second (helper/setup {}))
    (var secondValue (. localStorage (getItem "shared")))
    (var hasStorage (== "object" (typeof localStorage)))
    (helper/teardown second)
    (return {"first" firstValue
             "second" secondValue
             "hasStorage" hasStorage
             "restored" (== "undefined" (typeof window))}))
  => {"first" "first" "second" nil "hasStorage" true "restored" true})

^{:refer js.react.helper-jsdom/render :added "4.1"}
(fact "renders a component and waits for its effect"

  (defn.js Counter
    []
    (var [value setValue] (React.useState "before"))
    (React.useEffect (fn [] (setValue "after")) [])
    (return [:span value]))

  (notify/wait-on :js
    (var env (helper/setup {}))
    (. (helper/render env -/Counter {})
       (then (fn [_]
               (var html document.body.innerHTML)
               (helper/teardown env)
               (repl/notify {"html" html
                             "closed" (. env ["closed"])
                             "restored" (== "undefined" (typeof window))})))))
  => {"html" "<div id=\"root\"><span>after</span></div>"
      "closed" true
      "restored" true})

^{:refer js.react.helper-jsdom/await-dom :added "4.1"}
(fact "resolves after the next DOM task"
  (notify/wait-on :js
    (var order [])
    (. (helper/await-dom
        (fn []
          (. order (push "later"))
          (return (. order ["length"]))))
       (then (fn [value]
               (repl/notify {"value" value
                             "order" order}))))
    (. order (push "now")))
  => {"value" 2
      "order" ["now" "later"]})

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

^{:refer js.react.helper-jsdom/withEnv :added "4.1"}
(fact "waits for an async callback before tearing down JSDOM"

  (notify/wait-on :js
    (. (helper/withEnv {}
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

^{:refer js.react.helper-jsdom/withComponent :added "4.1"}
(fact "passes the rendered callback context"

  (notify/wait-on :js
    (var Component
         (fn [props]
           (return [:span (. props ["label"])])))
    (repl/notify
     (helper/withComponent Component {"label" "Ada"}
                           (fn [element document component env]
                             (return {"html" document.body.innerHTML
                                      "root-id" (. env ["root"] ["id"])
                                      "same-component" (== component Component)
                                      "same-document" (== document (!:G document))
                                      "element-type" (typeof element)})))))
  => {"html" "<div id=\"root\"><span>Ada</span></div>"
      "root-id" "root"
      "same-component" true
      "same-document" true
      "element-type" "undefined"})

^{:refer js.react.helper-jsdom/wait-on :added "4.1"}
(fact "sets up and tears down JSDOM around a React callback"
  (helper-source/wait-on
   (fn []
     (var React (require "react"))
     (var [value setValue] (React.useState "before"))
     (React.useEffect (fn [] (setValue "after")) [])
     (return (React.createElement "span" nil value)))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>after</span></div>"

  (!.js
    (return (== "undefined" (typeof window))))
  => true)
