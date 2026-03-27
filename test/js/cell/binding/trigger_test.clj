(ns js.cell.binding.trigger-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.binding.trigger :as binding-trigger]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +prepared+
  {"model_id" "orders"
   "view_id" "live"
   "deps" [["accounts" "current"]
           "summary"]
   "trigger" {"signal" "db/sync"}
   "stream" {"db" {"target" "supabase-main"}
             "topic" ["orders" "acct-1"]
             "on-event" "refresh"}})

^{:refer js.cell.binding.trigger/normalize-deps :added "4.1"}
(fact "normalizes dependency paths"
  ^:hidden

  (!.js
   (binding-trigger/normalize-deps (@! +prepared+)))
  => [["accounts" "current"]
      ["orders" "summary"]])

^{:refer js.cell.binding.trigger/compile-trigger :added "4.1"}
(fact "passes trigger metadata through"
  ^:hidden

  (!.js
   (binding-trigger/compile-trigger (@! +prepared+)))
  => {"signal" "db/sync"})

^{:refer js.cell.binding.trigger/compile-stream-options :added "4.1"}
(fact "compiles stream metadata into option context"
  ^:hidden

  (!.js
   (binding-trigger/compile-stream-options (@! +prepared+)))
  => {"context"
      {"stream"
       {"key" "supabase-main::[\"orders\",\"acct-1\"]::live::orders"
        "spec" {"db" {"target" "supabase-main"}
                "target" "supabase-main"
                "topic" ["orders" "acct-1"]
                "on-event" "refresh"}}}})

^{:refer js.cell.binding.trigger/compile-view-hooks :added "4.1"}
(fact "compiles deps, trigger, and stream hooks together"
  ^:hidden

  (!.js
   (binding-trigger/compile-view-hooks (@! +prepared+)))
  => {"deps" [["accounts" "current"]
              ["orders" "summary"]]
      "trigger" {"signal" "db/sync"}
      "options"
      {"context"
       {"stream"
        {"key" "supabase-main::[\"orders\",\"acct-1\"]::live::orders"
         "spec" {"db" {"target" "supabase-main"}
                 "target" "supabase-main"
                 "topic" ["orders" "acct-1"]
                 "on-event" "refresh"}}}}})
