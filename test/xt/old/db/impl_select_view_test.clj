(ns xt.old.db.impl-select-view-test
  (:require [std.lang :as l]
             [std.string.prose :as prose]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.old.db.base-schema :as sch]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-promise :as spec-promise]
             [xt.old.db.sql-util :as ut]
             [xt.old.db.sql-graph :as graph]
             [xt.old.db.sql-view :as view]
             [xt.old.db.sql-manage :as manage]
             [xt.old.db.sample-scratch-test :as sample-scratch]
             [xt.old.sys.conn-dbsql :as dbsql]
             [js.lib.driver-postgres :as js-postgres]
             [xt.lang.common-repl :as repl]]})

(defn ^{:lang-exceptions {:lua {:skip true}
                          :python {:skip true}
                          :dart {:skip true}}}
  bootstrap-js
  []
  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
      (dbsql/connect (js-postgres/driver)
                     {:database "test-scratch"})
      (fn [conn]
        (:= (!:G CONN) conn)
        (repl/notify true)))))

(fact:global
 ^{:lang-exceptions {:lua {:skip true}
                     :python {:skip true}
                     :dart {:skip true}}}
 {:setup    [(l/rt:restart)
              (l/rt:scaffold :js)
              (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.old.db.impl-select-view-test/CONNECTION
  :adopt true
  :added "4.0"
  :lang-exceptions {:lua {:skip true}
                    :python {:skip true}
                    :dart {:skip true}}}
(fact "CONNECTED"

  (notify/wait-on :js
    (spec-promise/x:promise-then
      (dbsql/query CONN "SELECT 1;")
      repl/notify))
  => (any nil 1 [{"?column?" 1}]))

^{:refer xt.old.db.impl-select-view-test/VIEW-QUERY
  :added "4.0"
  :lang-exceptions {:lua {:skip true}
                    :python {:skip true}
                    :dart {:skip true}}}
(fact "queries views"

  (view/query-select sample-scratch/Schema
                     {:view {:table "Entry"
                             :type "select"
                             :tag "by-name"
                             :query {"name" "{{i-name}}"}}
                      :input [{:symbol "i-name" :type "text"}]}
                     ["A-1"]
                     {}
                     true)
  => ["Entry" {"custom" [], "where" [{"name" "A-1"}], "links" [], "data" ["id"]}]

  (view/query-select sample-scratch/Schema
                     {:view {:table "Entry"
                             :type "select"
                             :tag "by-name"
                             :query {"name" "{{i-name}}"}}
                      :input [{:symbol "i-name" :type "text"}]}
                     ["A-1"]
                     {})
  => "SELECT id FROM Entry\n  WHERE name = 'A-1'")
