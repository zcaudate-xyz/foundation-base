(ns xt.db.sql-graph-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
              [xt.db.sql-util :as ut]
              [xt.db.sql-raw :as raw]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-lib :as k]
              [xt.db.base-schema :as sch]
              [xt.db.base-scope :as scope]
              [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-lib :as k]
              [xt.db.sql-util :as ut]
              [xt.db.base-schema :as sch]
              [xt.db.base-scope :as scope]
              [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-lib :as k]
              [xt.db.sql-util :as ut]
              [xt.db.base-schema :as sch]
              [xt.db.base-scope :as scope]
              [xt.db.sample-test :as sample]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.sql-graph :as g]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.sql-graph/select-where.darr :adopt true :added "4.0"}
(fact "multi select"
  ^:hidden
  
  (!.lua
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "nickname"
                        ["in" [["hello" "root" "world"]]]
                        2
                        {}
                        nil))
  => "nickname in ('hello', 'root', 'world')"

  (!.js
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:nickname ["in" [["hello" "root" "world"]]]}
                   0
                   {}))
  => "SELECT id FROM UserAccount\nWHERE nickname in ('hello', 'root', 'world')")

^{:refer xt.db.sql-graph/select-where.more :adopt true :added "4.0"}
(fact "formats the query return"
  ^:hidden
  
  (!.js
   (g/select-where sample/Schema
                   "UserProfile"
                   "id"
                   {:account {:wallets {:entries {:asset "XLM"}}
                              :is-official true}
                    :first-name "hello"}
                   0
                   {}))
   
  => (prose/|
      "SELECT id FROM UserProfile"
      "WHERE account_id IN ("
      "  SELECT id FROM UserAccount"
      "  WHERE id IN ("
      "    SELECT owner_id FROM Wallet"
      "    WHERE id IN ("
      "      SELECT wallet_id FROM WalletAsset"
      "      WHERE asset_id = 'XLM'"
    "    )"
    "  ) AND is_official = TRUE"
    ") AND first_name = 'hello'")

  (!.lua
   (g/select-where sample/Schema
                   "UserProfile"
                   "id"
                   {:account {:wallets {:entries {:asset "XLM"}}
                              :is-official true}
                    :first-name "hello"}
                   0
                   {}))
  => #"(?s)(?=.*SELECT id FROM UserProfile)(?=.*account_id IN \()(?=.*SELECT id FROM UserAccount)(?=.*SELECT owner_id FROM Wallet)(?=.*SELECT wallet_id FROM WalletAsset)(?=.*asset_id = 'XLM')(?=.*is_official = TRUE)(?=.*first_name = 'hello').*"
   
  (!.js
   (g/select-where sample/Schema
                   "Wallet"
                   "id"
                   {:owner {:profile {:first-name "hello"}
                            :is-official true}}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM Wallet"
      "WHERE owner_id IN ("
      "  SELECT id FROM UserAccount"
      "  WHERE id IN ("
      "    SELECT account_id FROM UserProfile"
      "    WHERE first_name = 'hello'"
      "  ) AND is_official = TRUE"
      ")")

  (!.lua
   (g/select-where sample/Schema
                   "Wallet"
                   "id"
                   {:owner {:profile {:first-name "hello"}
                            :is-official true}}
                   0
                   {}))
  => #"(?s)(?=.*SELECT id FROM Wallet)(?=.*owner_id IN \()(?=.*SELECT id FROM UserAccount)(?=.*SELECT account_id FROM UserProfile)(?=.*first_name = 'hello')(?=.*is_official = TRUE).*")

^{:refer xt.db.sql-graph/base-query-inputs :added "4.0"}
(fact "formats the query inputs"
  ^:hidden
  
  (!.js
   (g/base-query-inputs
    ["UserAccount"
     ["id" "nickname"
      ["profile"
       {:id 1}
       ["first_name" "last_name"]]]]))
  => ["UserAccount" {} ["id" "nickname" ["profile" {"id" 1} ["first_name" "last_name"]]]]

  (!.lua
   (g/base-query-inputs
    ["UserAccount"
     ["id" "nickname"
      ["profile"
       {:id 1}
       ["first_name" "last_name"]]]]))
  => ["UserAccount" {} ["id" "nickname" ["profile" {"id" 1} ["first_name" "last_name"]]]]

  (!.py
   (g/base-query-inputs
    ["UserAccount"
     ["id" "nickname"
      ["profile"
       {:id 1}
       ["first_name" "last_name"]]]]))
  => ["UserAccount" {} ["id" "nickname" ["profile" {"id" 1} ["first_name" "last_name"]]]])

^{:refer xt.db.sql-graph/base-format-return :added "4.0"}
(fact "formats the query return"
  ^:hidden
  
  (!.js
   [(g/base-format-return {:expr "count(*)"} nil nil)
    (g/base-format-return {:expr "count(*)"
                           :as "count"}
                          nil nil)])
  => ["count(*)"
      "count(*) AS count"]

  (!.lua
   [(g/base-format-return {:expr "count(*)"} nil nil)
    (g/base-format-return {:expr "count(*)"
                           :as "count"}
                          nil nil)])
  => ["count(*)"
      "count(*) AS count"]

  (!.py
   [(g/base-format-return {:expr "count(*)"} nil nil)
    (g/base-format-return {:expr "count(*)"
                           :as "count"}
                          nil nil)])
  => ["count(*)"
      "count(*) AS count"])

^{:refer xt.db.sql-graph/select-where-pair :added "4.0"}
(fact "formats the query return"
  ^:hidden

  (!.js
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "profile"
                        {:first-name "hello"}
                        2
                        {}
                        g/select-where))
  => (prose/|
      "id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")")
  
  (!.lua
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "profile"
                        {:first-name "hello"}
                        2
                        {}
                        g/select-where))
  => (prose/|
      "id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")")

  (!.py
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "profile"
                        {:first-name "hello"}
                        2
                        {}
                        g/select-where))
  => (prose/|
      "id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")"))

^{:refer xt.db.sql-graph/select-where :added "4.0"}
(fact "formats the query return"
  ^:hidden

  (!.js
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:profile {:first-name "hello"
                              :last-name "hello"}}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello' AND last_name = 'hello'"
      ")")
  
  (!.lua
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:profile {:first-name "hello"}}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello'"
      ")")
  
  (!.py
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:profile {:first-name "hello"
                              :last-name "hello"}}
                   0
                   {}))
  => (prose/|
      "SELECT id FROM UserAccount"
      "WHERE id IN ("
      "  SELECT account_id FROM UserProfile"
      "  WHERE first_name = 'hello' AND last_name = 'hello'"
      ")"))

^{:refer xt.db.sql-graph/select-return-str :added "4.0"
  :setup [(def +result+
            (prose/|
             "(SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
             "  WHERE id = UserProfile.account_id) AS account"))]}
(fact "select return string loop"
  ^:hidden
  
  (!.js
   (g/select-return-str sample/Schema
                        (xtd/second (scope/get-tree sample/Schema
                                                  "UserProfile"
                                                  {}
                                                  [["account"]]
                                                  {}))
                        g/select-return
                        0
                        {}))
  => +result+
  
  

  (!.lua
   (g/select-return-str sample/Schema
                        (xtd/second (scope/get-tree sample/Schema
                                                  "UserProfile"
                                                  {}
                                                  [["account"]]
                                                  {}))
                        g/select-return
                        0
                        {}))
  => +result+

  (!.py
   (g/select-return-str sample/Schema
                    (xtd/second (scope/get-tree sample/Schema
                                              "UserProfile"
                                              {}
                                              [["account"]]
                                              {}))
                    g/select-return
                    0
                    {}))
  => +result+)

^{:refer xt.db.sql-graph/select-return :added "4.0"
  :setup [(def +result+
            (prose/|
             "SELECT (SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
             "  WHERE id = UserProfile.account_id) AS account FROM UserProfile"))]}
(fact "select return call"
  ^:hidden
  
  (!.js
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  
  
  => +result+

  (!.lua
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  => +result+
  

  (!.py
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  => +result+)

^{:refer xt.db.sql-graph/select-tree :added "4.0"
  :setup [(def +output+
            ["UserProfile"
             {"custom" [],
              "where" [],
              "links"
              [["account"
                "forward"
                ["UserAccount"
                 {"custom" [],
                  "where" [{"id" ["eq" ["UserProfile.account_id"]]}],
                  "links" [],
                  "data"
                  ["id"
                   "nickname"
                   "password_updated"
                   "is_super"
                   "is_suspended"
                   "is_official"]}]]],
              "data" []}])]}
(fact "gets the selection tree structure"
  ^:hidden
  
  (!.js
   (g/select-tree sample/Schema
                  ["UserProfile"
                   {}
                   [["account"]]]
                  {}))
  => +output+

  (!.lua
   (g/select-tree sample/Schema
                  ["UserProfile"
                   {}
                   [["account"]]]
                  {}))
  => ["UserProfile"
      {"custom" {}
       "where" {},
       "links"
       [["account"
         "forward"
         ["UserAccount"
          {"custom" {}
           "where" [{"id" ["eq" ["UserProfile.account_id"]]}],
           "links" {}
           "data"
           ["id"
            "nickname"
            "password_updated"
            "is_super"
            "is_suspended"
            "is_official"]}]]],
       "data" {}}]

  (!.py
   (g/select-tree sample/Schema
                  ["UserProfile"
                   {}
                   [["account"]]]
                  {}))
  => +output+)

^{:refer xt.db.sql-graph/select :added "4.0"}
(fact "encodes a select state given schema and graph"
  ^:hidden

  (!.js
   (g/select sample/Schema
             ["UserAccount"
              [{"::" "sql/count"}]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => "WITH j_ret AS (\n  SELECT count(*) FROM UserAccount\n) SELECT jsonb_agg(j_ret) FROM j_ret"
  
  (!.js
   (g/select sample/Schema
             ["UserAccount"
              ["*/data"
               (ut/ORDER-BY ["hello"])
               (ut/ORDER-SORT "asc")
               (ut/LIMIT 1)
               ["wallets"]]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, nickname, password_updated, is_super, is_suspended, is_official, (WITH j_ret AS ("
      "    SELECT id, slug FROM Wallet"
      "      WHERE owner_id = UserAccount.id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS wallets FROM UserAccount ORDER BY hello ASC LIMIT 1"
      ") SELECT jsonb_agg(j_ret) FROM j_ret")
  
  
  (!.js
   (g/select sample/Schema
             ["UserAccount"
              ["*/data"
               ["profile"]
               ["wallets"]]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, nickname, password_updated, is_super, is_suspended, is_official, (WITH j_ret AS ("
      "    SELECT id, first_name, last_name, city, about, language FROM UserProfile"
      "      WHERE account_id = UserAccount.id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS profile, (WITH j_ret AS ("
      "    SELECT id, slug FROM Wallet"
      "      WHERE owner_id = UserAccount.id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS wallets FROM UserAccount"
      ") SELECT jsonb_agg(j_ret) FROM j_ret")
  
  (def +out+
    (!.js
     (g/select sample/Schema
               ["UserProfile"
                ["*/data"
                 ["account"]]]
               {:wrapper-fn ut/postgres-wrapper-fn})))
  +out+
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, first_name, last_name, city, about, language, (WITH j_ret AS ("
      "    SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
      "      WHERE id = UserProfile.account_id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS account FROM UserProfile"
      ") SELECT jsonb_agg(j_ret) FROM j_ret")

  (!.lua
   (g/select sample/Schema
             ["UserProfile"
              ["*/data"
               ["account"]]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => +out+
  
  (!.py
   (g/select sample/Schema
             ["UserProfile"
              ["*/data"
               ["account"]]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => +out+)
