(ns js.react.ext-box-test
  (:require [lang.core :as l]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]
             [js.react.ext-box :as ext-box]
             [xt.event.base-box :as event-box]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.ext-box/createBox :added "4.0"}
(fact "creates a box with initial data"

  (!.js
    (var box (ext-box/createBox {"account" "hello"}))
    (return {"type" (. box ["::"])
             "data" (event-box/get-data box [])
             "account" (ext-box/getData box ["account"])}))
  => {"type" "event.box"
      "data" {"account" "hello"}
      "account" "hello"})

^{:refer js.react.ext-box/useListenBox :added "4.0"}
(fact "updates a mounted component when a matching path changes"

  (helper-source/test
   (fn [props]
     (var box (. props ["box"]))
     (var value (ext-box/useListenBox box ["account"]))
     (return (r/createElement "span" nil value)))
   (fn [_]
     (return {"box" (ext-box/createBox {"account" "before" "other" "unchanged"})}))
   (fn [props document _]
     (var box (. props ["box"]))
     (var before document.body.innerHTML)
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (r/act
           (fn []
             (event-box/set-data box ["other"] "ignored")
             (event-box/set-data box ["account"] "after"))))
         (then (fn [_]
                 (var result {"before" before
                              "after" document.body.innerHTML})
                 (return result)))))))
  => {"before" "<div id=\"root\"><span>before</span></div>"
      "after" "<div id=\"root\"><span>after</span></div>"})

^{:refer js.react.ext-box/listenBox :added "4.0"}
(fact "provides the listener alias"
  (helper-source/test
   (fn [props]
     (var box (. props ["box"]))
     (return (r/createElement "span" nil
                              (ext-box/listenBox box ["value"]))))
   (fn [_]
     (return {"box" (ext-box/createBox {"value" "a"})}))
   (fn [props document _]
     (var box (. props ["box"]))
     (var React (require "react"))
     (return
      (. (Promise.resolve
          (r/act
           (fn []
             (event-box/set-data box ["value"] "b"))))
         (then (fn [_]
                 (var result document.body.innerHTML)
                 (return result)))))))
  => "<div id=\"root\"><span>b</span></div>")

^{:refer js.react.ext-box/useBox :added "4.0"}
(fact "returns a setter that updates the box and component"

  (helper-source/test
   (fn [#{box controls}]
     (var [value setValue] (ext-box/useBox box ["account"]))
     (xt/x:set-key controls "setValue" setValue)
     (return (r/createElement "span" nil value)))
   (fn [_]
     (return {"box" (ext-box/createBox {"account" "before"})
              "controls" {}}))
   (fn [#{box controls} document _]
     (return
      (. (Promise.resolve
          (r/act
           (fn []
             ((. controls ["setValue"]) "after"))))
         (then (fn [_]
                 (var result {"html" document.body.innerHTML
                              "data" (event-box/get-data box ["account"])})
                 (return result)))))))
  => {"html" "<div id=\"root\"><span>after</span></div>"
      "data" "after"})

^{:refer js.react.ext-box/attachLocalStorage :added "4.0"}
(fact "loads and persists a box path through localStorage"
  (helper-source/test
   (fn [] (return nil))
   (fn [_]
     (. localStorage (setItem "box-storage" "{\"account\":\"stored\"}"))
     (var box (ext-box/createBox {"account" "initial"}))
     (ext-box/attachLocalStorage "box-storage" box "storage-listener" ["account"])
     (return {"box" box}))
   (fn [props _ _]
     (var box (. props ["box"]))
     (var loaded (event-box/get-data box ["account"]))
     (event-box/set-data box ["account"] "updated")
     (return
      (new Promise
           (fn [resolve]
             (setTimeout
              (fn []
                (resolve {"loaded" loaded
                          "saved" (. localStorage (getItem "box-storage"))}))
              0))))))
  => {"loaded" "stored"
      "saved" "{\"account\":\"updated\"}"})

^{:refer js.react.ext-box/getData :added "4.0"}
(fact "gets nested data"
  (!.js
    (var box (ext-box/createBox {"profile" {"name" "Ada"}}))
    (return [(ext-box/getData box [])
             (ext-box/getData box ["profile" "name"])]))
  => [{"profile" {"name" "Ada"}} "Ada"])

^{:refer js.react.ext-box/setData :added "4.0"}
(fact "sets nested data"
  (!.js
    (var box (ext-box/createBox {"value" 1}))
    (ext-box/setData box ["value"] 2)
    (return (ext-box/getData box ["value"])))
  => 2)

^{:refer js.react.ext-box/delData :added "4.0"}
(fact "deletes nested data"
  (!.js
    (var box (ext-box/createBox {"value" 1}))
    (ext-box/delData box ["value"])
    (return (xt/x:get-key (ext-box/getData box []) "value")))
  => nil)

^{:refer js.react.ext-box/resetData :added "4.0"}
(fact "resets the box to its initial value"
  (!.js
    (var box (ext-box/createBox {"value" 1}))
    (ext-box/setData box ["value"] 2)
    (ext-box/resetData box)
    (return (ext-box/getData box [])))
  => {"value" 1})

^{:refer js.react.ext-box/mergeData :added "4.0"}
(fact "merges data at a path"
  (!.js
    (var box (ext-box/createBox {"profile" {"name" "Ada" "role" "user"}}))
    (ext-box/mergeData box ["profile"] {"role" "admin"})
    (return (ext-box/getData box ["profile"])))
  => {"name" "Ada" "role" "admin"})

^{:refer js.react.ext-box/appendData :added "4.0"}
(fact "appends data to an array path"
  (!.js
    (var box (ext-box/createBox {"items" [1]}))
    (ext-box/appendData box ["items"] 2)
    (return (ext-box/getData box ["items"])))
  => [1 2])

^{:refer js.react.ext-box/addListener :added "4.0"}
(fact "adds a listener for matching paths"
  (!.js
    (var box (ext-box/createBox {"value" 0}))
    (var calls [])
    (ext-box/addListener box "listener" ["value"]
                         (fn [_ payload _ _]
                           (. calls (push (. payload ["value"]))))
                         nil)
    (ext-box/setData box ["value"] 1)
    (return calls))
  => [1])

^{:refer js.react.ext-box/removeListener :added "4.0"}
(fact "removes a listener"
  (!.js
    (var box (ext-box/createBox {"value" 0}))
    (var calls 0)
    (ext-box/addListener box "listener" ["value"]
                         (fn [_ _ _ _] (:= calls (+ calls 1)))
                         nil)
    (ext-box/removeListener box "listener")
    (ext-box/setData box ["value"] 1)
    (return calls))
  => 0)
