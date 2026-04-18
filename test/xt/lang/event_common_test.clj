(ns xt.lang.event-common-test
  (:require [net.http :as http]
            [rt.basic :as basic]
            [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
              [xt.lang.event-common :as event]
              [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
              [xt.lang.event-common :as event]
              [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
               [xt.lang.event-common :as event]
               [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-common/blank-container :added "4.0"}
(fact "creates a blank container")

^{:refer xt.lang.event-common/make-container :added "4.0"}
(fact "makes a container"
  ^:hidden

  (!.js
   (event/make-container
    (fn:> 1)
    "custom.container"
    {:info {:name "hello"}}))
  => {"info" {"name" "hello"},
      "::" "custom.container",
      "listeners" {},
      "data" 1}

  (!.lua
   (k/to-string
    (event/make-container
     (fn:> 1)
     "custom.container"
     {:info {:name "hello"}})))
  
  (!.lua
   (xtd/tree-get-spec
    (event/make-container
     (fn:> 1)
     "custom.container"
     {:info {:name "hello"}})))
  => {"::" "string",
      "info" {"name" "string"},
      "initial" "function",
      "listeners" {},
      "data" "number"}
  
  (!.py
   (xtd/tree-get-spec
    (event/make-container
                (fn:> 1)
                "custom.container"
               {:info {:name "hello"}})))
  => {"::" "string",
      "info" {"name" "string"},
      "initial" "function",
      "listeners" {},
      "data" "number"})

^{:refer xt.lang.event-common/make-listener-entry :added "4.0"
  :setup [(def +out+
            (contains-in
             {"callback" "<function>",
              "meta"
              {"hello" "world",
               "listener/id" "abc",
               "listener/type" "custom"}}))]}
(fact "makes a listener entry"
  ^:hidden
  
  (!.js
   (xtd/tree-get-data
    (event/make-listener-entry
     "abc"
     "custom"
     (fn:>)
     {:hello "world"})))
  => +out+

  (!.lua
   (xtd/tree-get-data
    (event/make-listener-entry
     "abc"
     "custom"
     (fn:>)
     {:hello "world"})))
  => +out+

  (!.py
   (xtd/tree-get-data
    (event/make-listener-entry
     "abc"
     "custom"
     (fn:>)
     {:hello "world"}
     nil)))
  => +out+)

^{:refer xt.lang.event-common/clear-listeners :added "4.0"}
(fact "clears all listeners")

^{:refer xt.lang.event-common/add-listener :added "4.0"}
(fact "adds a listener to container"
  ^:hidden
  
  (notify/wait-on :js
    (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
    (event/add-listener c "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"})
    (event/trigger-listeners c {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"}
  

  (notify/wait-on :lua
    (var c (event/make-container
            (fn:> 1)
            "custom.container"
            {:info {:name "hello"}}))
    (event/add-listener c "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"})
    (event/trigger-listeners c {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"}

  (notify/wait-on :python
    (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
    (event/add-listener c "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"}
                            nil)
    (event/trigger-listeners c {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"})

^{:refer xt.lang.event-common/remove-listener :added "4.0"}
(fact "removes a listener"
  ^:hidden
  
  (!.js
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   [(event/remove-listener c "b2")
    (event/list-listeners c)])
  => [{"pred" nil,
       "meta" {"listener/id" "b2", "listener/type" "custom"}}
      ["a1" "c3"]]
  

  (!.lua
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/remove-listener c "b2")
   (event/list-listeners c))
  => (contains ["a1" "c3"] :in-any-order)

  (!.py
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom"
                           (fn:>)
                           nil
                           nil)
   (event/remove-listener c "b2")
   (event/list-listeners c))
  => ["a1" "c3"])

^{:refer xt.lang.event-common/list-listeners :added "4.0"}
(fact "lists all current listeners")

^{:refer xt.lang.event-common/list-listener-types :added "4.0"}
(fact "lists listeners by their type"
  ^:hidden
  
  (!.js
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom.2"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/list-listener-types c))
  => {"custom.2" ["b2"], "custom.1" ["a1" "c3"]}

  (!.lua
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom.2"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/list-listener-types c))
  => (contains {"custom.2" ["b2"], "custom.1"
                (contains ["a1" "c3"] :in-any-order)})

  (!.py
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-listener c
                           "a1"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "b2"
                           "custom.2"
                           (fn:>)
                           nil
                           nil)
   (event/add-listener c
                           "c3"
                           "custom.1"
                           (fn:>)
                           nil
                           nil)
   (event/list-listener-types c))
  => {"custom.2" ["b2"], "custom.1" ["a1" "c3"]})

^{:refer xt.lang.event-common/trigger-entry :added "4.0"}
(fact "triggers the individual entry"
  ^:hidden
  
  (notify/wait-on :js
    (var entry (event/make-listener-entry
                "abc"
                "custom"
                (repl/>notify)))
    (event/trigger-entry entry {}))
  => {"meta" {"listener/id" "abc", "listener/type" "custom"}}

  (notify/wait-on :lua
    (var entry (event/make-listener-entry
                "abc"
                "custom"
                (repl/>notify)))
    (event/trigger-entry entry {}))
  => {"meta" {"listener/id" "abc", "listener/type" "custom"}}

  (notify/wait-on :python
    (var entry (event/make-listener-entry
                "abc"
                "custom"
                (repl/>notify)
                nil
                nil))
    (event/trigger-entry entry {}))
  => {"meta" {"listener/id" "abc", "listener/type" "custom"}})

^{:refer xt.lang.event-common/trigger-listeners :added "4.0"}
(fact "triggers listeners given event")

^{:refer xt.lang.event-common/add-keyed-listener :added "4.0"}
(fact "adds a keyed entry"
  ^:hidden
  
  (notify/wait-on :js
    (var c (event/make-container
            (fn:> 1)
            "custom.container"
            {:info {:name "hello"}}))
    (event/add-keyed-listener c "key/common"
                            "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"})
    (event/trigger-keyed-listeners c "key/common" {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"}

  (notify/wait-on :lua
    (var c (event/make-container
            (fn:> 1)
            "custom.container"
            {:info {:name "hello"}}))
    (event/add-keyed-listener c "key/common"
                            "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"})
    (event/trigger-keyed-listeners c "key/common" {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"}

  (notify/wait-on :python
    (var c (event/make-container
            (fn:> 1)
            "custom.container"
            {:info {:name "hello"}}))
    (event/add-keyed-listener c "key/common"
                            "abc" "custom"
                            (repl/>notify)
                            {:custom/label "hello"}
                            nil)
    (event/trigger-keyed-listeners c "key/common" {:data "hello"}))
  => {"meta"
      {"listener/id" "abc",
       "custom/label" "hello",
       "listener/type" "custom"},
      "data" "hello"})

^{:refer xt.lang.event-common/remove-keyed-listener :added "4.0"}
(fact "removes a keyed listener"
  ^:hidden
  
  (!.js
   (var c (event/make-container
           (fn:> 1)
           "custom.container"
           {:info {:name "hello"}}))
   (event/add-keyed-listener
    c
    "key/common"
    "a1"
    "custom"
    (fn:>)
    nil
    nil)
   (event/add-keyed-listener
    c
    "key/common"
    
    "b2"
    "custom"
    (fn:>)
    nil
    nil)
   (event/add-keyed-listener
    c
    "key/common"
    "c3"
    "custom"
    (fn:>)
    nil
    nil)
   [(event/remove-keyed-listener c "key/common" "b2")
    (event/list-keyed-listeners c "key/common")])
  => [{"pred" nil,
       "meta" {"listener/id" "b2", "listener/type" "custom"}}
      ["a1" "c3"]])

^{:refer xt.lang.event-common/list-keyed-listeners :added "4.0"}
(fact "lists all listeners under and key")

^{:refer xt.lang.event-common/all-keyed-listeners :added "4.0"}
(fact "lists all listeners")

^{:refer xt.lang.event-common/trigger-keyed-listeners :added "4.0"}
(fact "triggers listeners under a key")
