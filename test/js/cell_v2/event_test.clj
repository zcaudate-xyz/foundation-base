(ns js.cell-v2.event-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2.event :as event]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.event/event-group :added "4.0" :unchecked true}
(fact "extracts generic event groups"
  (event/event-group event/EV_INIT)
  => "lifecycle"

  (event/event-group event/EV_LOCAL)
  => "local"

  (event/event-group event/EV_REMOTE)
  => "remote"

  (event/event-group event/EV_DB_SYNC)
  => "store"

  (event/event-group "custom/topic")
  => "custom")

^{:refer js.cell-v2.event/event :added "4.0" :unchecked true}
(fact "constructs and emits events"
  (event/event event/EV_REMOTE {:ok true} {:source "test"})
  => {"signal" "cell/::REMOTE"
      "topic" "cell/::REMOTE"
      "status" "ok"
      "group" "remote"
      "body" {"ok" true}
      "meta" {"source" "test"}}

  (!.js
   (var bus (event/make-bus))
   (var seen [])
   (event/add-listener bus
                       "lifecycle"
                       event/EV_INIT
                       (fn [input topic]
                          (x:arr-push seen [topic (k/get-key input "group") (k/get-key input "signal")])))
   (event/add-listener bus
                       "store"
                       event/event-store?
                       (fn [input topic]
                         (x:arr-push seen [topic (k/get-key input "body")])))
   [(event/emit bus (event/event event/EV_INIT {:ready true} nil))
    (event/emit bus (event/event event/EV_DB_SYNC {:tables ["User"]} nil))
    seen])
  => [["lifecycle"]
      ["store"]
      [["@/::INIT" "lifecycle" "@/::INIT"]
       ["db/::SYNC" {"tables" ["User"]}]]])
