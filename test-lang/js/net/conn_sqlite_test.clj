(ns js.net.conn-sqlite-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [js.net.conn-sqlite :as js-sqlite]))

(l/script- :js
  {:runtime :basic
   :require [[xt.net.conn-sql :as conn-sql]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]]})

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
    (-> (conn-sql/create-base nil (js-sqlite/client-methods))
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

^{:refer js.net.conn-sqlite/client-methods :added "4.1"}
(fact "create client methods for sqlite"

  (!.js
    (tree/tree-get-spec
     (js-sqlite/client-methods)))
  => {"query_async" "function", "disconnect" "function", "query" "function", "connect" "function"})

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
