(ns js.net.conn-sqlite-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.net.conn-sql :as conn-sql]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [js.net.conn-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.conn-sqlite/decode-json-scalar :added "4.1"}
(fact "decodes a json scalar"

  (!.js
    (js-sqlite/decode-json-scalar "hello"))
  => "hello")

^{:refer js.net.conn-sqlite/raw-query :added "4.1"}
(fact "creates a raw-query"
  
  (notify/wait-on :js
    (-> (js-sqlite/create)
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (var #{raw} client)
           (repl/notify (js-sqlite/raw-query raw "SELECT 1;"))))))
  => 1)

^{:refer js.net.conn-sqlite/raw-init :added "4.1"}
(fact "creates an sqlite instance")

^{:refer js.net.conn-sqlite/client-connect :added "4.1"}
(fact "method for connecting the client"
  
  (notify/wait-on :js
    (-> (js-sqlite/client-connect {})
        (promise/x:promise-then
         (fn [client]
           (repl/notify client)))))
  => {"raw" {"filename" ":memory:"}})

^{:refer js.net.conn-sqlite/client-disconnect :added "4.1"}
(fact "disconnects the raw sqlite handle"

  (notify/wait-on :js
    (-> (js-sqlite/create)
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (js-sqlite/client-disconnect client))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => true)

^{:refer js.net.conn-sqlite/client-query :added "4.1"}
(fact "runs a synchronous query on the raw sqlite handle"

  (notify/wait-on :js
    (-> (js-sqlite/create)
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (var #{raw} client)
           (repl/notify (js-sqlite/client-query client "SELECT 1;"))))))
  => 1)

^{:refer js.net.conn-sqlite/client-query-async :added "4.1"}
(fact "runs an async query on the raw sqlite handle"

  (notify/wait-on :js
    (-> (js-sqlite/create)
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (js-sqlite/client-query-async client "SELECT 1;"))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => 1)

^{:refer js.net.conn-sqlite/create :added "4.1"}
(fact "creates a sqlite entry"

  (notify/wait-on :js
    (-> (js-sqlite/create)
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (return
            (conn-sql/query-async client "SELECT 1;"))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => 1)
