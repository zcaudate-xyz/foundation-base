(ns xt.db.schema.sql-graph-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.schema.sql-graph :as g]
             [xt.db.schema.sql-util :as ut]
             [xt.db.schema.sql-raw :as raw]
             [xt.lang.common-data :as xtd]
             [xt.db.schema.base-schema :as sch]
             [xt.db.schema.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.schema.sql-graph :as g]
             [xt.db.schema.sql-util :as ut]
             [xt.db.schema.sql-raw :as raw]
             [xt.lang.common-data :as xtd]
             [xt.db.schema.base-schema :as sch]
             [xt.db.schema.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.schema.sql-graph :as g]
             [xt.db.schema.sql-util :as ut]
             [xt.db.schema.sql-raw :as raw]
             [xt.lang.common-data :as xtd]
             [xt.db.schema.base-schema :as sch]
             [xt.db.schema.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.schema.sql-graph/select-where.darr :added "4.0" :adopt true}
(fact "multi select"

  (!.js
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
  => "SELECT id FROM UserAccount\nWHERE nickname in ('hello', 'root', 'world')"

  (!.lua
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "nickname"
                        ["in" [["hello" "root" "world"]]]
                        2
                        {}
                        nil))
  => "nickname in ('hello', 'root', 'world')"

  (!.lua
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:nickname ["in" [["hello" "root" "world"]]]}
                   0
                   {}))
  => "SELECT id FROM UserAccount\nWHERE nickname in ('hello', 'root', 'world')"

  (!.py
   (g/select-where-pair sample/Schema
                        "UserAccount"
                        "nickname"
                        ["in" [["hello" "root" "world"]]]
                        2
                        {}
                        nil))
  => "nickname in ('hello', 'root', 'world')"

  (!.py
   (g/select-where sample/Schema
                   "UserAccount"
                   "id"
                   {:nickname ["in" [["hello" "root" "world"]]]}
                   0
                   {}))
  => "SELECT id FROM UserAccount\nWHERE nickname in ('hello', 'root', 'world')")

^{:refer xt.db.schema.sql-graph/select-where.more :added "4.0" :adopt true}
(fact "formats the query return"

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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT owner_id FROM Wallet"
      "    WHERE id IN ("
      "      SELECT wallet_id FROM WalletAsset"
      "      WHERE asset_id = 'XLM'"
      "    )"
      "  )"
      ") AND first_name = 'hello'")

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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT account_id FROM UserProfile"
      "    WHERE first_name = 'hello'"
      "  )"
      ")")

  (!.lua
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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT owner_id FROM Wallet"
      "    WHERE id IN ("
      "      SELECT wallet_id FROM WalletAsset"
      "      WHERE asset_id = 'XLM'"
      "    )"
      "  )"
      ") AND first_name = 'hello'")

  (!.lua
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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT account_id FROM UserProfile"
      "    WHERE first_name = 'hello'"
      "  )"
      ")")

  (!.py
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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT owner_id FROM Wallet"
      "    WHERE id IN ("
      "      SELECT wallet_id FROM WalletAsset"
      "      WHERE asset_id = 'XLM'"
      "    )"
      "  )"
      ") AND first_name = 'hello'")

  (!.py
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
      "  WHERE is_official = TRUE AND id IN ("
      "    SELECT account_id FROM UserProfile"
      "    WHERE first_name = 'hello'"
      "  )"
      ")"))

^{:refer xt.db.schema.sql-graph/base-query-inputs :added "4.0"}
(fact "formats the query inputs"

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

^{:refer xt.db.schema.sql-graph/base-format-return :added "4.0"}
(fact "formats the query return"

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

^{:refer xt.db.schema.sql-graph/select-where-pair :added "4.0"}
(fact "formats the query return"

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

^{:refer xt.db.schema.sql-graph/select-where :added "4.0"}
(fact "formats the query return"

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

^{:refer xt.db.schema.sql-graph/select-return-str :added "4.0"
  :setup [(def +out+
            (prose/|
             "(SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
             "  WHERE id = UserProfile.account_id) AS account"))]}
(fact "select return string loop"

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
  => +out+

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
  => +out+

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
  => +out+)

^{:refer xt.db.schema.sql-graph/select-return :added "4.0"
  :setup [(def +out+
            (prose/|
             "SELECT (SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
             "  WHERE id = UserProfile.account_id) AS account FROM UserProfile"))]}
(fact "select return call"

  (!.js
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  => +out+

  (!.lua
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  => +out+

  (!.py
   (g/select-return sample/Schema
                    (scope/get-tree sample/Schema
                                    "UserProfile"
                                    {}
                                    [["account"]]
                                    {})
                    0
                    {}))
  => +out+)

^{:refer xt.db.schema.sql-graph/select-tree :added "4.0"
  :setup [(def +out+
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

  ^{:seedgen/base {:lua {:transform {+out+ (l/as-lua +out+)}}}}
  (!.js
    (g/select-tree sample/Schema
                   ["UserProfile"
                    {}
                    [["account"]]]
                   {}))
  => +out+

  (!.lua
    (g/select-tree sample/Schema
                   ["UserProfile"
                    {}
                    [["account"]]]
                   {}))
  => (l/as-lua +out+)

  (!.py
    (g/select-tree sample/Schema
                   ["UserProfile"
                    {}
                    [["account"]]]
                   {}))
  => +out+)

^{:refer xt.db.schema.sql-graph/select :added "4.0"}
(fact "encodes a select state given schema and graph"

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

  (!.js
    (g/select sample/Schema
              ["UserProfile"
               ["*/data"
                ["account"]]]
              {:wrapper-fn ut/postgres-wrapper-fn}))
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, first_name, last_name, city, about, language, (WITH j_ret AS ("
      "    SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
      "      WHERE id = UserProfile.account_id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS account FROM UserProfile"
      ") SELECT jsonb_agg(j_ret) FROM j_ret")

  (!.lua
   (g/select sample/Schema
             ["UserAccount"
              [{"::" "sql/count"}]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => "WITH j_ret AS (\n  SELECT count(*) FROM UserAccount\n) SELECT jsonb_agg(j_ret) FROM j_ret"

  (!.lua
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

  (!.lua
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

  (!.lua
    (g/select sample/Schema
              ["UserProfile"
               ["*/data"
                ["account"]]]
              {:wrapper-fn ut/postgres-wrapper-fn}))
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, first_name, last_name, city, about, language, (WITH j_ret AS ("
      "    SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
      "      WHERE id = UserProfile.account_id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS account FROM UserProfile"
      ") SELECT jsonb_agg(j_ret) FROM j_ret")

  (!.py
   (g/select sample/Schema
             ["UserAccount"
              [{"::" "sql/count"}]]
             {:wrapper-fn ut/postgres-wrapper-fn}))
  => "WITH j_ret AS (\n  SELECT count(*) FROM UserAccount\n) SELECT jsonb_agg(j_ret) FROM j_ret"

  (!.py
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

  (!.py
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

  (!.py
    (g/select sample/Schema
              ["UserProfile"
               ["*/data"
                ["account"]]]
              {:wrapper-fn ut/postgres-wrapper-fn}))
  => (prose/|
      "WITH j_ret AS ("
      "  SELECT id, first_name, last_name, city, about, language, (WITH j_ret AS ("
      "    SELECT id, nickname, password_updated, is_super, is_suspended, is_official FROM UserAccount"
      "      WHERE id = UserProfile.account_id"
      "  ) SELECT jsonb_agg(j_ret) FROM j_ret) AS account FROM UserProfile"
      ") SELECT jsonb_agg(j_ret) FROM j_ret"))

(comment
  (s/pedantic ['xt.db.schema.sql-graph])
  
  (s/run ['xt.db.schema.sql-graph])
  
  (s/seedgen-benchadd   '[xt.db xt.lang xt.event] {:lang [:ruby :dart :elisp] :write true})
  (s/seedgen-langadd    '[xt.db.schema.sql-graph] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.db.schema.sql-graph] {:lang [:lua :python] :write true}))
