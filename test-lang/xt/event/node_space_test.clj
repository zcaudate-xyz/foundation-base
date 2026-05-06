(ns xt.event.node-space-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]
             [xt.event.node-space :as space]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]
             [xt.event.node-space :as space]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node :as node]
             [xt.event.node-space :as space]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node/create-space :added "4.1"}
(fact "manages per-space state independently from node-level handlers"

  (!.js
    (var n (node/node-create {}))
    (node/create-space n "alpha" {:state {:count 1}})
    (node/ensure-space n "beta" nil)
    (node/set-space-state n "beta" {:count 2})
    (node/update-space-state
     n
     "beta"
     (fn [state entry node]
       (return (xt/x:obj-assign state {:count 3
                                       :id (. entry ["id"])}))))
    [(xt/x:len (space/list-spaces n))
     (. (space/get-space n "alpha") ["id"])
     (. (space/get-space-state n "alpha") ["count"])
     (. (space/get-space-state n "beta") ["count"])
     (. (space/get-space-state n "beta") ["id"])
     (. (space/remove-space n "alpha") ["id"])
     (space/list-spaces n)])
  => [2 "alpha" 1 3 "beta" "alpha" ["beta"]]

  (!.lua
    (var n (node/node-create {}))
    (node/create-space n "alpha" {:state {:count 1}})
    (node/ensure-space n "beta" nil)
    (node/set-space-state n "beta" {:count 2})
    (node/update-space-state
     n
     "beta"
     (fn [state entry node]
       (return (xt/x:obj-assign state {:count 3
                                       :id (. entry ["id"])}))))
    [(xt/x:len (space/list-spaces n))
     (. (space/get-space n "alpha") ["id"])
     (. (space/get-space-state n "alpha") ["count"])
     (. (space/get-space-state n "beta") ["count"])
     (. (space/get-space-state n "beta") ["id"])
     (. (space/remove-space n "alpha") ["id"])
     (space/list-spaces n)])
  => [2 "alpha" 1 3 "beta" "alpha" ["beta"]]

  (!.py
    (var n (node/node-create {}))
    (node/create-space n "alpha" {:state {:count 1}})
    (node/ensure-space n "beta" nil)
    (node/set-space-state n "beta" {:count 2})
    (node/update-space-state
     n
     "beta"
     (fn [state entry node]
       (return (xt/x:obj-assign state {:count 3
                                       :id (. entry ["id"])}))))
    [(xt/x:len (space/list-spaces n))
     (. (space/get-space n "alpha") ["id"])
     (. (space/get-space-state n "alpha") ["count"])
     (. (space/get-space-state n "beta") ["count"])
     (. (space/get-space-state n "beta") ["id"])
     (. (space/remove-space n "alpha") ["id"])
     (space/list-spaces n)])
  => [2 "alpha" 1 3 "beta" "alpha" ["beta"]])

^{:refer xt.event.node-space/space :added "4.1"}
(fact "constructs a space entry with defaults"

  (!.js
    (var entry (space/space "room/a" {:state {:count 1}}))
    [(. entry ["id"])
     (. entry ["state"] ["count"])
     (. entry ["meta"])])
  => ["room/a" 1 {}]

  (!.lua
    (var entry (space/space "room/a" {:state {:count 1}}))
    [(. entry ["id"])
     (. entry ["state"] ["count"])
     (. entry ["meta"])])
  => ["room/a" 1 {}]

  (!.py
    (var entry (space/space "room/a" {:state {:count 1}}))
    [(. entry ["id"])
     (. entry ["state"] ["count"])
     (. entry ["meta"])])
  => ["room/a" 1 {}])

^{:refer xt.event.node-space/get-space :added "4.1"}
(fact "gets spaces by id"

  (!.js
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(. (space/get-space n "room/a") ["id"])
     (space/get-space n "missing")])
  => ["room/a" nil]

  (!.lua
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(. (space/get-space n "room/a") ["id"])
     (xt/x:nil? (space/get-space n "missing"))])
  => ["room/a" true]

  (!.py
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(. (space/get-space n "room/a") ["id"])
     (space/get-space n "missing")])
  => ["room/a" nil])

^{:refer xt.event.node-space/ensure-space :added "4.1"}
(fact "ensures a space exists without replacing existing entries"

  (!.js
    (var n (node/node-create {}))
    (var first (space/ensure-space n "room/a" {:state {:count 1}}))
    (var second (space/ensure-space n "room/a" {:state {:count 9}}))
    [(. first ["state"] ["count"])
     (. second ["state"] ["count"])
     (xt/x:len (space/list-spaces n))])
  => [1 1 1]

  (!.lua
    (var n (node/node-create {}))
    (var first (space/ensure-space n "room/a" {:state {:count 1}}))
    (var second (space/ensure-space n "room/a" {:state {:count 9}}))
    [(. first ["state"] ["count"])
     (. second ["state"] ["count"])
     (xt/x:len (space/list-spaces n))])
  => [1 1 1]

  (!.py
    (var n (node/node-create {}))
    (var first (space/ensure-space n "room/a" {:state {:count 1}}))
    (var second (space/ensure-space n "room/a" {:state {:count 9}}))
    [(. first ["state"] ["count"])
     (. second ["state"] ["count"])
     (xt/x:len (space/list-spaces n))])
  => [1 1 1])

^{:refer xt.event.node-space/remove-space :added "4.1"}
(fact "removes a space entry and returns it"

  (!.js
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(.
      (space/remove-space n "room/a")
      ["id"])
     (space/get-space n "room/a")])
  => ["room/a" nil]

  (!.lua
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(.
      (space/remove-space n "room/a")
      ["id"])
     (xt/x:nil? (space/get-space n "room/a"))])
  => ["room/a" true]

  (!.py
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 1}})
    [(.
      (space/remove-space n "room/a")
      ["id"])
     (space/get-space n "room/a")])
  => ["room/a" nil])

^{:refer xt.event.node-space/list-spaces :added "4.1"}
(fact "lists active spaces"

  (!.js
    (var n (node/node-create {}))
    (space/create-space n "a" nil)
    (space/create-space n "b" nil)
    [(xt/x:len (space/list-spaces n))
     (space/list-spaces n)])
  => [2 ["a" "b"]]

  (!.lua
    (var n (node/node-create {}))
    (space/create-space n "a" nil)
    (space/create-space n "b" nil)
    [(xt/x:len (space/list-spaces n))
     (space/list-spaces n)])
  => [2 ["a" "b"]]

  (!.py
    (var n (node/node-create {}))
    (space/create-space n "a" nil)
    (space/create-space n "b" nil)
    [(xt/x:len (space/list-spaces n))
     (space/list-spaces n)])
  => [2 ["a" "b"]])

^{:refer xt.event.node-space/get-space-state :added "4.1"}
(fact "gets space state and creates default space state on demand"

  (!.js
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 2}})
    [(. (space/get-space-state n "room/a") ["count"])
     (space/get-space-state n "room/b")])
  => [2 {}]

  (!.lua
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 2}})
    [(. (space/get-space-state n "room/a") ["count"])
     (space/get-space-state n "room/b")])
  => [2 {}]

  (!.py
    (var n (node/node-create {}))
    (space/create-space n "room/a" {:state {:count 2}})
    [(. (space/get-space-state n "room/a") ["count"])
     (space/get-space-state n "room/b")])
  => [2 {}])

^{:refer xt.event.node-space/set-space-state :added "4.1"}
(fact "sets a space state value"

  (!.js
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 5})
    [(. (space/get-space-state n "room/a") ["count"])])
  => [5]

  (!.lua
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 5})
    [(. (space/get-space-state n "room/a") ["count"])])
  => [5]

  (!.py
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 5})
    [(. (space/get-space-state n "room/a") ["count"])])
  => [5])

^{:refer xt.event.node-space/update-space-state :added "4.1"}
(fact "updates a space state value with the current entry"

  (!.js
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 1})
    (space/update-space-state
     n
     "room/a"
     (fn [state entry node]
       (return {:count (+ (. state ["count"]) 4)
                :id (. entry ["id"])})))
    [(. (space/get-space-state n "room/a") ["count"])
     (. (space/get-space-state n "room/a") ["id"])])
  => [5 "room/a"]

  (!.lua
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 1})
    (space/update-space-state
     n
     "room/a"
     (fn [state entry node]
       (return {:count (+ (. state ["count"]) 4)
                :id (. entry ["id"])})))
    [(. (space/get-space-state n "room/a") ["count"])
     (. (space/get-space-state n "room/a") ["id"])])
  => [5 "room/a"]

  (!.py
    (var n (node/node-create {}))
    (space/set-space-state n "room/a" {:count 1})
    (space/update-space-state
     n
     "room/a"
     (fn [state entry node]
       (return {:count (+ (. state ["count"]) 4)
                :id (. entry ["id"])})))
    [(. (space/get-space-state n "room/a") ["count"])
     (. (space/get-space-state n "room/a") ["id"])])
  => [5 "room/a"])
