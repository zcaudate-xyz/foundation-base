(ns xt.db.walkthrough.fixture-00-postgres
  (:require [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]))

(def +scratch-env+
  {"host" "127.0.0.1"
   "port" "5432"
   "user" "postgres"
   "password" "postgres"
   "database" "test-scratch"})

(def +schema+
  {"Entry"
   {"id" {"ident" "id" "type" "text" "order" 0 "primary" true}
    "name" {"ident" "name" "type" "text" "order" 1}
    "tags" {"ident" "tags" "type" "array" "order" 2}
    "time_created" {"ident" "time_created" "type" "long" "order" 3}
    "time_updated" {"ident" "time_updated" "type" "long" "order" 4}
    "op_created" {"ident" "op_created" "type" "text" "order" 5}
    "op_updated" {"ident" "op_updated" "type" "text" "order" 6}
    "__deleted__" {"ident" "__deleted__" "type" "boolean" "order" 7}}})

(def +lookup+
  {"Entry" {"position" 0
            "schema" "scratch"}})

(def +inline-query+
  {:table "Entry"
   :select-entry {"input" [{"symbol" "i_name" "type" "text"}]
                  "view" {"query" {"name" "{{i_name}}"
                                   "__deleted__" false}}}
   :select-args ["alpha"]
   :return-entry {"input" [{"symbol" "i_entry_id" "type" "text"}]
                  "view" {"query" ["name" "tags"]}}})

(def +model-query+
  {:table "Entry"
   :select-entry {"input" []
                  "view" {"query" {"__deleted__" false}}}
   :return-entry {"input" [{"symbol" "i_entry_id" "type" "text"}]
                  "view" {"query" ["id"
                                  "name"
                                  "tags"
                                  "time_created"
                                  "time_updated"]}}})

(def +model-spec+
  {"views"
   {"entries"
    {"query" +model-query+
     "input" []}}})

(defn seed-entry-rows
  []
  (pg/t:delete scratch/Entry)
  (scratch/insert-entry "alpha" ^:js ["guide" "sql"] {})
  (scratch/insert-entry "beta" ^:js ["guide"] {}))
