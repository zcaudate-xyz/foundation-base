(ns xt.isolate-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python :js]}}
(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-space :as rt :with [defsingleton.xt]]
             [xt.isolate :as isolate]
             [xt.isolate.client :as client]
             [xt.isolate.mock :as mock]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-space :as rt :with [defsingleton.js]]
             [xt.isolate :as isolate]
             [xt.isolate.client :as client]
             [xt.isolate.mock :as mock]
             [js.core :as j]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.isolate/GD :added "4.0"}
(fact "returns nil when no default client has been set"

  (!.js
   (isolate/GD))
  => nil)

^{:refer xt.isolate/GX :added "4.0"}
(fact "returns an empty registry when no named clients have been stored"

  (!.js
   (isolate/GX))
  => {})

^{:refer xt.isolate/GX-val :added "4.0"}
(fact "retrieves a named client from the registry"

  (!.js
   (var cl (isolate/make-client {}))
   (isolate/GX-set "main" cl)
   (== (isolate/GX-val "main") cl))
  => true)

^{:refer xt.isolate/GX-set :added "4.0"}
(fact "stores a named client in the registry"

  (!.js
   (var cl (isolate/make-client {}))
   (isolate/GX-set "test" cl)
   (xt/x:not-nil? (isolate/GX-val "test")))
  => true)

^{:refer xt.isolate/make-client :added "4.0"}
(fact "creates a mock-backed client for in-process testing"

  (!.js
   (xt/x:get-key (isolate/make-client {}) "::"))
  => "isolate.client"

  (j/<!
   (client/client-call
    (isolate/make-client {})
    {:op    "call"
     :route "@isolate/ping"
     :body  []}))
  => (contains ["pong" integer?]))

^{:refer xt.isolate/EV_INIT :added "4.0"}
(fact "exports the isolate init event constant"

  (!.js isolate/EV_INIT)
  => "@isolate/::INIT")

^{:refer xt.isolate/EV_STATE :added "4.0"}
(fact "exports the isolate state event constant"

  (!.js isolate/EV_STATE)
  => "@isolate/::STATE")
