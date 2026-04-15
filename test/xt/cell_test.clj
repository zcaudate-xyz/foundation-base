(ns xt.cell-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-runtime :as rt :with [defvar.js]]
             [js.core :as j]
             [xt.cell :as cell]
             [xt.cell.kernel.base-link :as base-link]
             [xt.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

(defn.js make-link
  []
  (return
   (base-link/link-create
    {:create-fn
     (fn [listener]
       (var worker (worker-mock/create-worker listener {} true))
       (cell/actions-init {} worker)
       (return worker))})))

(defvar.js LINK
  []
  (return nil))

^{:refer xt.cell/SERVICE :added "4.1"}
(fact "returns nil when the worker service registry has not been set"
  ^:hidden

  (!.js
   (cell/SERVICE))
  => nil)

^{:refer xt.cell/BINDINGS :added "4.1"}
(fact "returns an empty map when the worker bindings registry has not been set"
  ^:hidden

  (!.js
   (cell/BINDINGS))
  => {})

^{:refer xt.cell/fn-setup-service :added "4.1"}
(fact "stores the worker service registry in global state"
  ^:hidden

  (!.js
   (cell/fn-setup-service {"dbs" {"local" {"kind" "cache"}}}))
  => {"dbs" {"local" {"kind" "cache"}}})

^{:refer xt.cell/fn-get-service :added "4.1"}
(fact "returns the previously stored worker service registry"
  ^:hidden

  (!.js
   (cell/fn-setup-service {"dbs" {"local" {"kind" "cache"}}})
   (cell/fn-get-service))
  => {"dbs" {"local" {"kind" "cache"}}})

^{:refer xt.cell/fn-setup-bindings :added "4.1"}
(fact "stores the worker bindings registry in global state"
  ^:hidden

  (!.js
   (cell/fn-setup-bindings {"orders" {"list" {}}}))
  => {"orders" {"list" {}}})

^{:refer xt.cell/fn-get-bindings :added "4.1"}
(fact "returns the previously stored worker bindings registry"
  ^:hidden

  (!.js
   (cell/fn-setup-bindings {"orders" {"list" {}}})
   (cell/fn-get-bindings))
  => {"orders" {"list" {}}})

^{:refer xt.cell/actions-cell :added "4.1"}
(fact "returns the cell-specific worker setup actions"
  ^:hidden

  (!.js
   (k/obj-keys (cell/actions-cell)))
  => ["@cell/setup-service"
      "@cell/get-service"
      "@cell/setup-bindings"
      "@cell/get-bindings"])

^{:refer xt.cell/actions-baseline :added "4.1"}
(fact "merges cell actions into the standard worker baseline"
  ^:hidden

  (!.js
   [(xt/x:has-key? (cell/actions-baseline) "@cell/setup-service")
    (xt/x:has-key? (cell/actions-baseline) "@worker/ping")])
  => [true true])

^{:refer xt.cell/actions-init :added "4.1"}
(fact "installs cell actions into a worker action table"
  ^:hidden

  (!.js
   (var worker (worker-mock/create-worker nil {} true))
    (cell/actions-init {"@custom/action" {:handler (fn [x] (return x))
                                         :is_async false
                                         :args ["x"]}}
                      worker)
   [(xt/x:has-key? (. worker ["actions"]) "@cell/setup-service")
    (xt/x:has-key? (. worker ["actions"]) "@custom/action")])
  => [true true])

^{:refer xt.cell/setup-service :added "4.1"}
(fact "sets the worker service registry over a link transport"
  ^:hidden

  (j/<!
   (cell/setup-service
    (-/make-link)
    {"dbs" {"local" {"kind" "cache"}}}))
  => {"dbs" {"local" {"kind" "cache"}}})

^{:refer xt.cell/get-service :added "4.1"}
(fact "gets the worker service registry over a link transport"
  ^:hidden

  (!.js
   (-/LINK-reset (-/make-link))
   true)
  => true

  (j/<!
   (cell/setup-service
    (-/LINK)
    {"dbs" {"local" {"kind" "cache"}}}))
  => {"dbs" {"local" {"kind" "cache"}}}

  (j/<!
   (cell/get-service (-/LINK)))
  => {"dbs" {"local" {"kind" "cache"}}})

^{:refer xt.cell/setup-bindings :added "4.1"}
(fact "sets the worker bindings registry over a link transport"
  ^:hidden

  (j/<!
   (cell/setup-bindings
    (-/make-link)
    {"orders" {"list" {}}}))
  => {"orders" {"list" {}}})

^{:refer xt.cell/get-bindings :added "4.1"}
(fact "gets the worker bindings registry over a link transport"
  ^:hidden

  (!.js
   (-/LINK-reset (-/make-link))
   true)
  => true

  (j/<!
   (cell/setup-bindings
    (-/LINK)
    {"orders" {"list" {}}}))
  => {"orders" {"list" {}}}

  (j/<!
   (cell/get-bindings (-/LINK)))
  => {"orders" {"list" {}}})
