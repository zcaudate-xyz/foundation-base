(ns xt.cell.binding.trigger-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.cell.binding.trigger :as binding-trigger]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.cell.binding.trigger :as binding-trigger]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.cell.binding.trigger :as binding-trigger]]})

(fact:global
 {:setup [(l/rt:restart)]
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

^{:refer xt.cell.binding.trigger/normalize-deps :added "4.1"}
(fact "normalizes dependency paths"

  (!.js
   (binding-trigger/normalize-deps (@! +prepared+)))
  => [["accounts" "current"]
      ["orders" "summary"]]

  (!.lua
   (binding-trigger/normalize-deps (@! +prepared+)))
  => [["accounts" "current"]
      ["orders" "summary"]]

  (!.py
   (binding-trigger/normalize-deps (@! +prepared+)))
  => [["accounts" "current"]
      ["orders" "summary"]])

^{:refer xt.cell.binding.trigger/compile-trigger :added "4.1"}
(fact "passes trigger metadata through"

  (!.js
   (binding-trigger/compile-trigger (@! +prepared+)))
  => {"signal" "db/sync"}

  (!.lua
   (binding-trigger/compile-trigger (@! +prepared+)))
  => {"signal" "db/sync"}

  (!.py
   (binding-trigger/compile-trigger (@! +prepared+)))
  => {"signal" "db/sync"})

^{:refer xt.cell.binding.trigger/compile-stream-options :added "4.1"}
(fact "compiles stream metadata into option context"

  (!.js
   (binding-trigger/compile-stream-options (@! +prepared+)))
  => {"context"
      {"stream"
       {"key" "supabase-main::[\"orders\",\"acct-1\"]::live::orders"
        "spec" {"db" {"target" "supabase-main"}
                "target" "supabase-main"
                "topic" ["orders" "acct-1"]
                "on-event" "refresh"}}}}

  (!.lua
   (binding-trigger/compile-stream-options (@! +prepared+)))
  => {"context"
      {"stream"
       {"key" "supabase-main::[\"orders\",\"acct-1\"]::live::orders"
        "spec" {"db" {"target" "supabase-main"}
                "target" "supabase-main"
                "topic" ["orders" "acct-1"]
                "on-event" "refresh"}}}}

  (!.py
   (binding-trigger/compile-stream-options (@! +prepared+)))
  => {"context"
      {"stream"
       {"key" "supabase-main::[\"orders\",\"acct-1\"]::live::orders"
        "spec" {"db" {"target" "supabase-main"}
                "target" "supabase-main"
                "topic" ["orders" "acct-1"]
                "on-event" "refresh"}}}})

^{:refer xt.cell.binding.trigger/compile-view-hooks :added "4.1"}
(fact "compiles deps, trigger, and stream hooks together"

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
                 "on-event" "refresh"}}}}}

  (!.lua
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
                 "on-event" "refresh"}}}}}

  (!.py
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
