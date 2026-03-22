(ns js.lib.supabase-ssr
  (:require [net.http :as http]
            [std.lang :as l]))

(l/script :js
  {:import [["@supabase/ssr" :as #{createServerClient}]]})

