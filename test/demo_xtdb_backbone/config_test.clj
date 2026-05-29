(ns demo-xtdb-backbone.config-test
  (:require [demo-xtdb-backbone.app.config :as config])
  (:use code.test))

(fact "loads the demo supabase config through std.config"
  (let [cfg (config/supabase-config)]
    [(config/supabase-base-url)
     (get cfg "schema_name")
     (string? (config/supabase-api-key))
     (string? (config/supabase-auth-token))])
  => ["http://127.0.0.1:55121"
      "scratch_v0"
      true
      true])
