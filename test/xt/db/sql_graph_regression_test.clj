(ns xt.db.sql-graph-regression-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.lang.common-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.lang.common-lib :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-graph/select-where.reverse-or :added "4.0"}
(fact "reverse OR clauses keep distinct branches"

  (!.js
   (g/select-where {"UserAccount" {"profile" {:type "ref"
                                              :ref {:type "reverse"
                                                    :ns "UserProfile"
                                                    :rkey "account"}}}
                    "UserProfile" {"first_name" {:type "string"}
                                   "language" {:type "string"}}}
                   "UserAccount"
                   "id"
                   {:profile [{"first_name" "hello"}
                              {"language" "en"}]}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE ((id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")) OR (id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE language = 'en'"
      ")))")

  (!.lua
   (g/select-where {"UserAccount" {"profile" {:type "ref"
                                              :ref {:type "reverse"
                                                    :ns "UserProfile"
                                                    :rkey "account"}}}
                    "UserProfile" {"first_name" {:type "string"}
                                   "language" {:type "string"}}}
                   "UserAccount"
                   "id"
                   {:profile [{"first_name" "hello"}
                              {"language" "en"}]}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE ((id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")) OR (id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE language = 'en'"
      ")))")

  (!.py
   (g/select-where {"UserAccount" {"profile" {:type "ref"
                                              :ref {:type "reverse"
                                                    :ns "UserProfile"
                                                    :rkey "account"}}}
                    "UserProfile" {"first_name" {:type "string"}
                                   "language" {:type "string"}}}
                   "UserAccount"
                   "id"
                   {:profile [{"first_name" "hello"}
                              {"language" "en"}]}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE ((id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")) OR (id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE language = 'en'"
      ")))"))
