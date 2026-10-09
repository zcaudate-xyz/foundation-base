(ns js.lib.valtio-test
    (:require [lang.core :as l]
              [xt.lang.common-notify :as notify]
              [js.react.helper-jsdom :as helper-source])
    (:use code.test)
    (:refer-clojure :exclude [use val proxy]))

(l/script- :js
           {:runtime :basic
            :require [[xt.lang.common-repl :as repl]
                      [js.react :as r]
                      [js.react.helper-jsdom :as helper]
                      [js.lib.valtio :as v]
                      [xt.lang.spec-base :as xt]
                      [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup    [(l/rt:restart :js)
             (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.valtio/make :added "4.0"}
(fact "makes a proxy with reset"

  (!.js
   (v/make (fn:> {:a 1 :b 2})))
  => {"a" 1, "b" 2})

^{:refer js.lib.valtio/reset :added "4.0"}
(fact "resets proxy to original"

  (!.js
   (v/reset (xtd/obj-assign (v/make (fn:> {:a 1 :b 2}))
                            {:a 3 :b 4})))
  => {"a" 1, "b" 2})

^{:refer js.lib.valtio/useVal :added "4.0"}
(fact "reads a selected snapshot value"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"name" "Ada"}))
     (return (r/createElement "span" nil
                              (v/useVal store ["name"]))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>Ada</span></div>")

^{:refer js.lib.valtio/val :added "4.0"}
(fact "expands the value macro to a selected snapshot value"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"name" "Ada"}))
     (return (r/createElement "span" nil
                              (v/val store "name"))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>Ada</span></div>")

^{:refer js.lib.valtio/listen :added "4.0"}
(fact "expands the listener macro for multiple stores"
  (helper-source/wait-on
   (fn []
     (var firstStore (v/make {"value" "one"}))
     (var secondStore (v/make {"value" "two"}))
     (var [firstValue secondValue] (v/listen [firstStore secondStore]))
     (return (r/createElement "span" nil
                              (+ (. firstValue ["value"])
                                 ":"
                                 (. secondValue ["value"])))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>one:two</span></div>")

^{:refer js.lib.valtio/getAccessors :added "4.0"}
(fact "creates snapshot getters and proxy setters"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "before"}))
     (var [getValue setValue resetValue] (v/getAccessors store))
     (return (r/createElement "span" nil
                              (+ (. (getValue) ["value"])
                                 ":"
                                 (typeof setValue)
                                 ":"
                                 (typeof resetValue)))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before:function:function</span></div>")

^{:refer js.lib.valtio/getFieldAccessors :added "4.0"}
(fact "creates accessors for one proxy field"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "before"}))
     (var [getValue setValue resetValue]
          (v/getFieldAccessors store "value"))
     (return (r/createElement "span" nil
                              (+ (getValue)
                                 ":"
                                 (typeof setValue)
                                 ":"
                                 (typeof resetValue)))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before:function:function</span></div>")

^{:refer js.lib.valtio/useProxy :added "4.0"}
(fact "returns a reactive proxy value and setter"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "before"}))
     (var [value setValue] (v/useProxy store))
     (return (r/createElement "span" nil
                              (+ (. value ["value"])
                                 ":"
                                 (typeof setValue)))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before:function</span></div>")

^{:refer js.lib.valtio/useProxyField :added "4.0"}
(fact "returns a reactive proxy field and setter"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "before"}))
     (var [value setValue] (v/useProxyField store "value"))
     (return (r/createElement "span" nil
                              (+ value ":" (typeof setValue)))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before:function</span></div>")

^{:refer js.lib.valtio/wrapProxyField :added "4.0"}
(fact "wraps a component with a proxy field"
  (helper-source/wait-on
   (v/wrapProxyField
    (fn [props]
      (return (r/createElement "span" nil (. props ["value"]))))
    ["value" "setValue"])
   {"record" (v/make {"value" "before"})
    "field" "value"}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before</span></div>")

^{:refer js.lib.valtio/use :added "4.0"}
(fact "expands the proxy hook macro"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "ready"}))
     (var [value] (v/use store))
     (return (r/createElement "span" nil (. value ["value"]))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>ready</span></div>")

^{:refer js.lib.valtio/useData :added "4.0"}
(fact "provides data state and reset controls"
  (helper-source/wait-on
   (fn []
     (var store (v/make {"value" "before" "enabled" false}))
     (var data (v/useData store (fn:> {"value" "initial"
                                        "enabled" false})))
     (var snapshot (. data ["data"]))
     (return (r/createElement "span" nil
                              (+ (. snapshot ["value"])
                                 ":"
                                 (. snapshot ["enabled"])
                                 ":"
                                 (typeof (. data ["setKey"]))
                                 ":"
                                 (typeof (. data ["toggleKey"]))
                                 ":"
                                 (typeof (. data ["resetData"]))))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>before:false:function:function:function</span></div>")
