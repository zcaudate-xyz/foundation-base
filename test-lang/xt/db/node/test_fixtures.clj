(ns xt.db.node.test-fixtures
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(def.xt Schema
  {"Order"
   {"id" {"ident" "id", "type" "text", "order" 0, "primary" true}
     "status" {"ident" "status", "type" "text", "order" 1}}})

(def.xt Lookup
  {"Order" {"position" 0}})

(def.xt Views
  {"Order"
   {"select"
    {"by_status"
     {"input" [{"symbol" "i_status", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "select"
              "tag" "by_status"
              "access" {"roles" {}}
              "guards" []
              "query" {"status" "{{i_status}}"}}}}
    "return"
    {"default"
     {"input" [{"symbol" "i_order_id", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
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
    {"query" {:table "Order"
             :return-entry {"input" [{"symbol" "i_order_id", "type" "text"}]
                            "return" "jsonb"
                            "view" {"table" "Order"
                                    "type" "return"
                                    "tag" "default"
                                    "access" {"roles" {}}
                                    "guards" []
                                    "query" ["status"]}}
             :return-id "ord-1"}
    "input" []}
    "open"
    {"query" {:table "Order"
              :select-entry {"input" [{"symbol" "i_status", "type" "text"}]
                             "return" "jsonb"
                             "view" {"table" "Order"
                                     "type" "select"
                                     "tag" "by_status"
                                     "access" {"roles" {}}
                                     "guards" []
                                     "query" {"status" "{{i_status}}"}}}}
     "default_input" ["open"]}}})

(def.xt Seed
  {"Order"
   [{"id" "ord-1" "status" "open"}
    {"id" "ord-2" "status" "closed"}]})
