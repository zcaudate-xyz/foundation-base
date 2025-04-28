(ns xt.db.impl-select-view-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres.script.scratch :as scratch]
             [rt.postgres :as pg]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-view :as view]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]
             [xt.sys.conn-dbsql :as dbsql]
             [lua.nginx.driver-postgres :as lua-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)
             (l/rt:scaffold :lua)]
  :teardown [(l/rt:stop)]})

(def +select-all+
  (pg/bind-view scratch/entry-all))

(def +select-by-name+
  (pg/bind-view scratch/entry-by-name))

(def +return-default+
  (pg/bind-view scratch/entry-default))

^{:refer xt.db.impl-select-view-test/CONNECTION :adopt true :added "4.0"}
(fact "CONNECTED"
  
  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (dbsql/query conn "SELECT 1;"))
  => 1)


^{:refer xt.db.impl-select-view-test/SQL-QUERY :adopt true :added "4.0"}
(fact "SQL QUERY"
  ^:hidden
  
  (view/query-select sample-scratch/Schema
                     +select-by-name+
                     ["A-1"]
                     {}
                     true)
  => ["Entry" {"custom" {}, "where" [{"name" "A-1"}], "links" {}, "data" ["id"]}]

  (view/query-select sample-scratch/Schema
                     +select-by-name+
                     ["A-1"]
                     {})
  => "SELECT id FROM Entry\n  WHERE name = 'A-1'"

  (view/query-select sample-scratch/Schema
                     (assoc +select-all+
                            :control {:limit 7})
                     []
                     {}
                     true)
  => ["Entry"
      {"custom"
       [{"name" "LIMIT"
         "args" [{"::" "sql/keyword", "name" 7}],
         "::" "sql/keyword"}],
       "where" {},
       "links" {},
       "data" ["id"]}]


  (view/query-select sample-scratch/Schema
                     (assoc +select-all+
                            :control {:limit 7})
                     []
                     {})
  => "SELECT id FROM Entry LIMIT 7"

  (view/query-select sample-scratch/Schema
                     (assoc +select-all+
                            :control {:order-by ["name"]
                                      :order-sort "desc"
                                      :limit 7
                                      :offset 5})
                     []
                     {})
  => "SELECT id FROM Entry ORDER BY name DESC LIMIT 7 OFFSET 5"

  (!.lua
   (view/query-select sample-scratch/Schema
                      (@! (assoc +select-all+
                                 :control {:order-by ["name"]
                                           :order-sort "desc"
                                           :limit 7
                                           :offset 5}))
                      []
                      (ut/postgres-opts sample-scratch/SchemaLookup)))
  => (std.string/|
      "WITH j_ret AS ("
      "  SELECT \"id\" FROM \"scratch\".\"Entry\" ORDER BY \"name\" DESC LIMIT 7 OFFSET 5"
      ") SELECT jsonb_agg(j_ret) FROM j_ret"))


^{:refer xt.db.impl-select-view-test/DATA-QUERY :adopt true :added "4.0"
  :setup [(pg/t:delete scratch/Entry)
          (doseq [i (range 30)]
            (pg/t:insert scratch/Entry
              {:name (str "A-" i)
               :tags '(js ["A"])}
              {:track {}}))]}
(fact "DATA QUERY"
  ^:hidden
  
  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn 
                 (view/query-combined
                  sample-scratch/Schema
                  (@! (assoc +select-all+
                             :control {:order-by ["name"]
                                       :order-sort "desc"
                                       :limit 5
                                       :offset 5}))
                  []
                  {:input [{:symbol "i_entry_id", :type "uuid"}],
                   :return "jsonb", :schema "scratch", :id "entry_default", :flags {},
                   :view {:table "Entry", :type "return", :tag "default",
                          :access {:query nil, :roles {}, :relation nil, :symbol nil},
                          :query ["name"], :guards []}}
                  
                  []
                  nil
                  (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-4"} {"name" "A-3"} {"name" "A-29"} {"name" "A-28"} {"name" "A-27"}]

  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn 
                 (view/query-combined
                  sample-scratch/Schema
                  (@! (assoc +select-all+
                             :control {:order-by ["name"]
                                       :order-sort "desc"
                                       :limit 5
                                       :offset 0}))
                  []
                  {:input [{:symbol "i_entry_id", :type "uuid"}],
                   :return "jsonb", :schema "scratch", :id "entry_default", :flags {},
                   :view {:table "Entry", :type "return", :tag "default",
                          :access {:query nil, :roles {}, :relation nil, :symbol nil},
                          :query ["name"], :guards []}}
                  
                  []
                  nil
                  (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-9"} {"name" "A-8"} {"name" "A-7"} {"name" "A-6"} {"name" "A-5"}])
