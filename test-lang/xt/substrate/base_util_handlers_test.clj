(ns xt.substrate.base-util-handlers-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as main]
             [xt.substrate.base-util-handlers :as handlers]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as main]
             [xt.substrate.base-util-handlers :as handlers]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as main]
             [xt.substrate.base-util-handlers :as handlers]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-util-handlers/install-util-handlers :added "4.1"}
(fact "installs the @/* util handlers on a node"

  (!.js
    (var n (main/node-create {"id" "install-node"}))
    (handlers/install-util-handlers n)
    (main/list-handlers n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"]

  (!.lua
    (var n (main/node-create {"id" "install-node"}))
    (handlers/install-util-handlers n)
    (main/list-handlers n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"]

  (!.py
    (var n (main/node-create {"id" "install-node"}))
    (handlers/install-util-handlers n)
    (main/list-handlers n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"])

^{:refer xt.substrate.base-util-handlers/ping :added "4.1"}
(fact "returns pong and node id"

  (!.js
    (var n (main/node-create {"id" "ping-node"}))
    (handlers/ping nil nil nil n))
  => {"pong" true "node" "ping-node"}

  (!.lua
    (var n (main/node-create {"id" "ping-node"}))
    (handlers/ping nil nil nil n))
  => {"pong" true "node" "ping-node"}

  (!.py
    (var n (main/node-create {"id" "ping-node"}))
    (handlers/ping nil nil nil n))
  => {"pong" true "node" "ping-node"})

^{:refer xt.substrate.base-util-handlers/echo :added "4.1"}
(fact "returns the supplied arguments"

  (!.js
    (handlers/echo nil [1 2 3] nil nil))
  => [1 2 3]

  (!.lua
    (handlers/echo nil [1 2 3] nil nil))
  => [1 2 3]

  (!.py
    (handlers/echo nil [1 2 3] nil nil))
  => [1 2 3])

^{:refer xt.substrate.base-util-handlers/list-handlers :added "4.1"}
(fact "returns registered handler action ids"

  (!.js
    (var n (main/node-create {"id" "handlers-node"}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (handlers/list-handlers nil nil nil n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"
      "echo"]

  (!.lua
    (var n (main/node-create {"id" "handlers-node"}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (handlers/list-handlers nil nil nil n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"
      "echo"]

  (!.py
    (var n (main/node-create {"id" "handlers-node"}))
    (main/register-handler n "echo" (fn [ctx arg] (return arg)) nil)
    (handlers/list-handlers nil nil nil n))
  => ["@/echo"
      "@/get-service"
      "@/list-handlers"
      "@/list-spaces"
      "@/list-transports"
      "@/list-triggers"
      "@/node-info"
      "@/ping"
      "echo"])

^{:refer xt.substrate.base-util-handlers/list-triggers :added "4.1"}
(fact "returns registered trigger signal ids"

  (!.js
    (var n (main/node-create {"id" "triggers-node"}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (handlers/list-triggers nil nil nil n))
  => ["event/ping"]

  (!.lua
    (var n (main/node-create {"id" "triggers-node"}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (handlers/list-triggers nil nil nil n))
  => ["event/ping"]

  (!.py
    (var n (main/node-create {"id" "triggers-node"}))
    (main/register-trigger n "event/ping" (fn [ctx data] (return data)) nil)
    (handlers/list-triggers nil nil nil n))
  => ["event/ping"])

^{:refer xt.substrate.base-util-handlers/list-spaces :added "4.1"}
(fact "returns registered space ids"

  (!.js
    (var n (main/node-create {"id" "spaces-node"
                             "spaces" {"room/a" {"state" {}}}}))
    (handlers/list-spaces nil nil nil n))
  => ["room/a"]

  (!.lua
    (var n (main/node-create {"id" "spaces-node"
                             "spaces" {"room/a" {"state" {}}}}))
    (handlers/list-spaces nil nil nil n))
  => ["room/a"]

  (!.py
    (var n (main/node-create {"id" "spaces-node"
                             "spaces" {"room/a" {"state" {}}}}))
    (handlers/list-spaces nil nil nil n))
  => ["room/a"])

^{:refer xt.substrate.base-util-handlers/list-transports :added "4.1"}
(fact "returns attached transport ids"

  (!.js
    (var n (main/node-create {"id" "transports-node"}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (handlers/list-transports nil nil nil n))
  => ["peer-a"]

  (!.lua
    (var n (main/node-create {"id" "transports-node"}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (handlers/list-transports nil nil nil n))
  => ["peer-a"]

  (!.py
    (var n (main/node-create {"id" "transports-node"}))
    (xt/x:set-key (. n ["transports"]) "peer-a" (main/transport-create "peer-a" {}))
    (handlers/list-transports nil nil nil n))
  => ["peer-a"])

^{:refer xt.substrate.base-util-handlers/node-info :added "4.1"}
(fact "returns node id and metadata"

  (!.js
    (var n (main/node-create {"id" "info-node"
                             "meta" {"role" "test"}}))
    (handlers/node-info nil nil nil n))
  => {"id" "info-node"
      "meta" {"role" "test"}}

  (!.lua
    (var n (main/node-create {"id" "info-node"
                             "meta" {"role" "test"}}))
    (handlers/node-info nil nil nil n))
  => {"id" "info-node"
      "meta" {"role" "test"}}

  (!.py
    (var n (main/node-create {"id" "info-node"
                             "meta" {"role" "test"}}))
    (handlers/node-info nil nil nil n))
  => {"id" "info-node"
      "meta" {"role" "test"}})

^{:refer xt.substrate.base-util-handlers/handle-get-service :added "4.1"}
(fact "returns the requested service entry"

  (!.js
    (var n (main/node-create {"id" "service-node"
                             "services" {"cache" {"scope" "local"}}}))
    [(handlers/handle-get-service nil "cache" nil n)
     (xt/x:nil? (handlers/handle-get-service nil "missing" nil n))])
  => [{"scope" "local"} true]

  (!.lua
    (var n (main/node-create {"id" "service-node"
                             "services" {"cache" {"scope" "local"}}}))
    [(handlers/handle-get-service nil "cache" nil n)
     (xt/x:nil? (handlers/handle-get-service nil "missing" nil n))])
  => [{"scope" "local"} true]

  (!.py
    (var n (main/node-create {"id" "service-node"
                             "services" {"cache" {"scope" "local"}}}))
    [(handlers/handle-get-service nil "cache" nil n)
     (xt/x:nil? (handlers/handle-get-service nil "missing" nil n))])
  => [{"scope" "local"} true])
