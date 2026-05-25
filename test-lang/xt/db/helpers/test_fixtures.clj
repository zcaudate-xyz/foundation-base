(ns xt.db.helpers.test-fixtures
  (:require [hara.lang :as l]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch])
  (:use code.test))

(def +scratch-env+
  {"host" "127.0.0.1"
   "port" "5432"
   "user" "postgres"
   "password" "postgres"
   "database" "test-scratch"})

(def +app+
  (pg/app "scratch"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(def +task-tree+
  {"Task"
   (select-keys (get +tree+ "Task")
                ["id" "status" "name"])})

(def +app-lookup+
  (pg/bind-app +app+))

(def +task-lookup+
  {"Task" {"position" 0}})

(def +schema+
  {"Entry"
   (get +tree+ "Entry")})

(def +lookup+
  {"Entry"
   {"position" (get-in +app-lookup+ ["Entry" :position])
    "schema"   (get-in +app-lookup+ ["Entry" :schema])}})

(def +inline-query+
  {"table" "Entry"
   "select_entry" {"input" [{"symbol" "i_name" "type" "text"}]
                   "view" {"query" {"name" "{{i_name}}"
                                    "__deleted__" false}}}
   "select_args" ["alpha"]
   "return_entry" {"input" [{"symbol" "i_entry_id" "type" "text"}]
                   "view" {"query" ["name" "tags"]}}})

(def +model-query+
  {"table" "Entry"
   "select_entry" {"input" []
                   "view" {"query" {"__deleted__" false}}}
   "return_entry" {"input" [{"symbol" "i_entry_id" "type" "text"}]
                   "view" {"query" ["id"
                                    "name"
                                    "tags"
                                    "time_created"
                                    "time_updated"]}}})

(def +resolver-inline-query+
  {"type" "db/query"
   "table" "Entry"
   "select_entry" {"input" [{"symbol" "i_name" "type" "text"}]
                   "view" {"query" {"name" "{{i_name}}"
                                   "__deleted__" false}}}
   "select_args" ["alpha"]
   "return_entry" {"input" [{"symbol" "i_entry_id" "type" "text"}]
                   "view" {"query" ["name" "tags"]}}})

(def +resolver-model-query+
  {"type" "db/query"
   "table" "Entry"
   "select_entry" {"input" []
                   "view" {"query" {"__deleted__" false}}}
   "return_entry" {"input" [{"symbol" "i_entry_id" "type" "text"}]
                   "view" {"query" ["id"
                                   "name"
                                   "tags"
                                   "time_created"
                                   "time_updated"]}}})

(def +model-spec+
  {"views"
   {"entries"
    {"resolver" +resolver-model-query+
     "input" []}}})

(def +entry-seed+
  {"Entry"
   [{"id" "00000000-0000-0000-0000-0000000000c1"
     "name" "alpha"
     "tags" ["guide" "sql"]
     "__deleted__" false}
    {"id" "00000000-0000-0000-0000-0000000000c2"
     "name" "beta"
     "tags" ["guide"]
     "__deleted__" false}]})

(defn seed-entry-rows
  []
  (pg/t:delete scratch/Entry)
  [(scratch/insert-entry "alpha" ^:js ["guide" "sql"] {})
   (scratch/insert-entry "beta" ^:js ["guide"] {})])

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(def.xt Schema
  (@! +task-tree+))

(def.xt Lookup
  (@! +task-lookup+))

(def.xt SchemaLookup
  (@! (pg/bind-app +app+)))

(def.xt Views
  {"Task"
   {"select"
     {"by_status"
      {"input" [{"symbol" "i_status", "type" "text"}]
       "return" "jsonb"
       "view" {"table" "Task"
               "type" "select"
               "tag" "by_status"
               "access" {"roles" {}}
               "guards" []
               "query" {"status" "{{i_status}}"}}}}
     "return"
     {"default"
      {"input" [{"symbol" "i_task_id", "type" "uuid"}]
       "return" "jsonb"
       "view" {"table" "Task"
               "type" "return"
               "tag" "default"
               "access" {"roles" {}}
               "guards" []
               "query" ["status"]}}}}})

(def.xt InstallOpts
  {"schema" -/Schema
   "lookup" -/Lookup
   "views" -/Views})

(def.xt EntrySeed
  (@! +entry-seed+))

(def.xt ModelSpec
  {"views"
   {"main"
    {"resolver" {"type" "db/query"
                "table" "Task"
                "return_entry" {"input" [{"symbol" "i_task_id", "type" "uuid"}]
                                "return" "jsonb"
                                "view" {"table" "Task"
                                        "type" "return"
                                        "tag" "default"
                                        "access" {"roles" {}}
                                        "guards" []
                                        "query" ["status"]}}
                "return_id" "00000000-0000-0000-0000-0000000000a1"}
    "input" []}
    "open"
    {"resolver" {"type" "db/query"
                 "table" "Task"
                 "select_entry" {"input" [{"symbol" "i_status", "type" "text"}]
                                 "return" "jsonb"
                                 "view" {"table" "Task"
                                         "type" "select"
                                         "tag" "by_status"
                                         "access" {"roles" {}}
                                         "guards" []
                                         "query" {"status" "{{i_status}}"}}}}
     "default_input" ["open"]}}})

(def.xt DependentModelSpec
  {"views"
   {"main"
    {"resolver" {"type" "db/query"
                "table" "Task"
                "return_method" "default"
                "return_id" "00000000-0000-0000-0000-0000000000a1"}
    "input" []}
    "open"
    {"resolver" {"type" "db/query"
                 "table" "Task"
                 "select_method" "by_status"}
    "default_input" ["open"]
    "deps" ["main"]}}})

(def.xt Seed
  {"Task"
   [{"id" "00000000-0000-0000-0000-0000000000a1"
    "status" "open"
    "name" "alpha-task"}
    {"id" "00000000-0000-0000-0000-0000000000a2"
    "status" "closed"
    "name" "beta-task"}]})

(fact "exposes shared node and postgres fixtures"
  [(keys +schema+)
   (keys +lookup+)
   (keys +task-tree+)]
  => [["Entry"]
     ["Entry"]
     ["Task"]])
