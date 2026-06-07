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
             [js.net.conn-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

                  

^{:refer js.net.conn-sqlite/decode-json-scalar :added "4.1"}
(fact "TODO"

  (!.js
    (js-sqlite/decode-json-scalar "hello"))
  => "hello")

^{:refer js.net.conn-sqlite/raw-query :added "4.1"}
(fact "TODO"
  
  (!.js
    (-> (conn-sql/create-base nil (js-sqlite/client-methods))
        (conn-sql/connect)))
  
  (!.js
    (xt/x:obj-keys
     (js-sqlite/client-methods)))
  []
  (notify/wait-on :js
    (-> (conn-sql/create-base nil (js-sqlite/client-methods))
        (conn-sql/connect)
        (promise/x:promise-then
         (fn [client]
           (repl/notify client)))))
  )

^{:refer js.net.conn-sqlite/make-instance :added "4.1"}
(fact "TODO")

^{:refer js.net.conn-sqlite/create-db :added "4.1"}
(fact "TODO")

^{:refer js.net.conn-sqlite/client-methods :added "4.1"}
(fact "TODO")
