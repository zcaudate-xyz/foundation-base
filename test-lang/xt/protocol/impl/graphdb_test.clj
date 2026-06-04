(ns xt.protocol.impl.graphdb-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.graphdb :as graphdb]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.graphdb/driver-create :added "4.1"}
(fact "wraps graphdb drivers and runtime db maps"

  (!.js
   (var driver
        (graphdb/driver-create
         {"create" (fn [input]
                     (return {"id" (. input ["seed"])}))
          "pull_sync" (fn [instance schema tree opts]
                        (return [(. instance ["id"]) tree (. opts ["mode"])]))
          "record_add" (fn [instance schema table-name records opts]
                        (return
                         (promise/x:promise-run
                          [(. instance ["id"]) schema table-name records (. opts ["mode"])])))
          "record_delete" (fn [instance schema table-name ids opts]
                           (return
                            (promise/x:promise-run
                             [(. instance ["id"]) schema table-name ids (. opts ["mode"])])))}))
   (var db
        (graphdb/db-create
         (graphdb/create driver {"seed" "db-1"})
         driver))
   (xt/x:set-key db "opts" {"mode" "sync"})
   [(graphdb/driver? driver)
    (graphdb/db? db)
    (graphdb/pull-sync db "schema-b" ["UserAccount"] {"mode" "pull"})])
  => [true
      true
      ["db-1" ["UserAccount"] "pull"]]

  (notify/wait-on [:js 2000]
    (promise/x:promise-then
     (graphdb/record-add db "schema-a" "UserAccount" [{"id" "user-1"}] {"mode" "write"})
     (fn [result]
       (repl/notify result))))
  => ["db-1" "schema-a" "UserAccount" [{"id" "user-1"}] "write"]

  (notify/wait-on [:js 2000]
    (promise/x:promise-then
     (graphdb/record-delete db "schema-a" "UserAccount" ["user-1"] {"mode" "delete"})
     (fn [result]
       (repl/notify result))))
  => ["db-1" "schema-a" "UserAccount" ["user-1"] "delete"])

^{:refer xt.protocol.impl.graphdb/pull :added "4.1"}
(fact "passes async graphdb results through without altering promise semantics"

  (notify/wait-on [:js 2000]
    (var db
         (graphdb/db-create
          {"id" "db-2"
           "opts" {"mode" "async"}}
          {"pull" (fn [instance schema tree opts]
                    (return
                     (promise/x:promise-run
                      [(. instance ["id"]) schema tree (. opts ["mode"])])))}))
    (promise/x:promise-then
     (graphdb/pull db "schema-c" ["Wallet"] {"mode" "async"})
     (fn [result]
       (repl/notify result))))
  => ["db-2" "schema-c" ["Wallet"] "async"])
