(ns xt.db.system.client-sql-parity-test
  (:require [hara.lang :as l]
            [std.string.prose :as prose]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true
                 :langs [:lua.nginx :python :dart]
                 :js           {:extra [[js.lib.driver-sqlite :as js-sqlite]]}
                 :lua.nginx    {:extra [[lua.nginx.driver-sqlite :as lua-sqlite]]}
                 :python       {:extra [[python.lib.driver-sqlite :as py-sqlite]]}
                 :dart         {:extra [[dart.lib.driver-sqlite :as dart-sqlite]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.base-sql :as client]
             [xt.db.helpers.data-main-test :as sample]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             ^{:seedgen/extra true}
             [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.base-sql/process-event-sync :added "4.1"
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
          (def +sql-touched-output+
            ["UserAccount" "UserProfile"])
          (def +nested-user-output+
            [{"nickname" "root"
              "profile" [{"first_name" "Root"}]}])]}
(fact "syncs and pulls sql data through the js sqlite driver"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var db (client/client {"instance" conn}))
           (repl/notify
            [(xtd/arr-sort
              (client/process-event-sync db
                                         "add"
                                         {"UserAccount" [sample/RootUser]}
                                         sample/Schema
                                         sample/SchemaLookup
                                         (ut/sqlite-opts nil))
              k/identity
              k/lt)
             (client/pull-sync db
                               sample/Schema
                               (@! +user-profile-tree+)
                               (ut/sqlite-opts nil))])))))
  => [+sql-touched-output+
      +nested-user-output+])

^{:refer xt.db.system.base-sql/process-event-remove :added "4.1"
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
          (def +sql-touched-output+
            ["UserAccount" "UserProfile"])
          (def +sql-remove-output+
            (prose/|
             "DELETE FROM \"UserAccount\" WHERE \"id\" = '00000000-0000-0000-0000-000000000000';"
             ""
             "DELETE FROM \"UserProfile\" WHERE \"id\" = 'c4643895-b0ce-44cc-b07b-2386bf18d43b';"))]}
(fact "emits remove sql and deletes synced rows through the js sqlite driver"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var db (client/client {"instance" conn}))
           (client/process-event-sync db
                                      "add"
                                      {"UserAccount" [sample/RootUser]}
                                      sample/Schema
                                      sample/SchemaLookup
                                      (ut/sqlite-opts nil))
           (repl/notify
            [(client/prepare-event-input
              {"UserAccount" [sample/RootUser]}
              sample/Schema
              sample/SchemaLookup
              (ut/sqlite-opts nil))
             (xtd/arr-sort
              (client/process-event-remove db
                                           "remove"
                                           {"UserAccount" [sample/RootUser]}
                                           sample/Schema
                                           sample/SchemaLookup
                                           (ut/sqlite-opts nil))
              k/identity
              k/lt)
             (xt/x:len
              (client/pull-sync db
                                sample/Schema
                                (@! +user-profile-tree+)
                                (ut/sqlite-opts nil)))])))))
  => [+sql-remove-output+
      +sql-touched-output+
      0])
