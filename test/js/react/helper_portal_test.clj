(ns js.react.helper-portal-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.react.helper-portal :as portal]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.helper-portal/newRegistry :added "4.0" :unchecked true}
(fact "creates a new portal registry"
  ^:hidden
  
  (portal/newRegistry)
  => (contains
      {"id" string?
       "sinks" {},
       "initial" {}, "sources" {}}))

^{:refer js.react.helper-portal/captureSink :added "4.0" :unchecked true}
(fact "captures the sink ref")

^{:refer js.react.helper-portal/triggerSink :added "4.0" :unchecked true}
(fact "triggers the registry"
  ^:hidden
  
  (notify/wait-on :js
    (portal/triggerSink
     {:sinks {:hello {:setSource (repl/>notify)}}
      :sources   {:hello {:hello-1 123
                          :hello-2 456
                          :hello-3 789}}}
     "hello"))
  => [123 456 789])

^{:refer js.react.helper-portal/addSink :added "4.0" :unchecked true}
(fact "adds a sink to the registry"
  ^:hidden
  
  (notify/wait-on :js
    (portal/addSink
     {:sinks  {}
      :initial   {}
      :sources   {:hello {:hello-1 123
                          :hello-2 456
                          :hello-3 789}}}
     "hello"
     {:setSource (repl/>notify)}))
  => [123 456 789])

^{:refer js.react.helper-portal/removeSink :added "4.0" :unchecked true}
(fact "removes a sink"

  (!.js
   (portal/removeSink
    {:sinks  {:hello {:setSource (fn:>)}}
     :initial   {}
     :sources   {:hello {:hello-1 123
                         :hello-2 456
                         :hello-3 789}}}
    "hello"))
  => {})

^{:refer js.react.helper-portal/addSource :added "4.0" :unchecked true}
(fact "adds a source for render"
  ^:hidden
  
  (notify/wait-on :js
    (portal/addSource
     {:sinks {:hello {:setSource (repl/>notify)}}
      :initial   {}
      :sources   {:hello {:hello-1 123
                          :hello-2 456
                          :hello-3 789}}}
     "hello"
     "hello-4"
     "abc"))
  => [123 456 789 "abc"])

^{:refer js.react.helper-portal/removeSource :added "4.0" :unchecked true}
(fact "removes a source for render"
  ^:hidden
  
  (notify/wait-on :js
    (portal/removeSource
     {:sinks {:hello {:setSource (repl/>notify)}}
      :initial   {}
      :sources   {:hello {:hello-1 123
                          :hello-2 456
                          :hello-3 789}}}
     "hello"
     "hello_1"))
  => [456 789])
