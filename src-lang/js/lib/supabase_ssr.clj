(ns js.lib.supabase-ssr
  (:require [net.http :as http]
            [lang.core :as l]))

(l/script :js
  {:import [["@supabase/ssr" :as #{createServerClient}]]})

