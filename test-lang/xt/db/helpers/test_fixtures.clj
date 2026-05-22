(ns xt.db.helpers.test-fixtures
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(def.xt Schema
  {"Task"
   {"id" {"ident" "id", "type" "uuid", "order" 0, "primary" true}
     "status" {"ident" "status", "type" "text", "order" 1}
     "name" {"ident" "name", "type" "text", "order" 2}}})

(def.xt Lookup
  {"Task" {"position" 0}})

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

(def.xt ModelSpec
  {"views"
   {"main"
    {"query" {:table "Task"
             :return-entry {"input" [{"symbol" "i_task_id", "type" "uuid"}]
                            "return" "jsonb"
                            "view" {"table" "Task"
                                    "type" "return"
                                    "tag" "default"
                                    "access" {"roles" {}}
                                    "guards" []
                                    "query" ["status"]}}
             :return-id "00000000-0000-0000-0000-0000000000a1"}
    "input" []}
    "open"
    {"query" {:table "Task"
              :select-entry {"input" [{"symbol" "i_status", "type" "text"}]
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
    {"query" {:table "Task"
             :return-method "default"
             :return-id "00000000-0000-0000-0000-0000000000a1"}
    "input" []}
    "open"
    {"query" {:table "Task"
             :select-method "by_status"}
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
