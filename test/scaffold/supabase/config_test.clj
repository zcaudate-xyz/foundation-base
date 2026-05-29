(ns scaffold.supabase.config-test
  (:require [scaffold.supabase.config :as supabase-config])
  (:use code.test))

(fact "loads default live supabase test settings through std.config"
  [(supabase-config/setup-type)
   (supabase-config/shell)
   (supabase-config/cli-root)
   (supabase-config/postgres-host)
   (supabase-config/postgres-port)
   (supabase-config/resolved-api-base-url)]
  => [:docker-compose
      "/bin/bash"
      "docker/supabase"
      "127.0.0.1"
      55122
      "http://127.0.0.1:55121"])
