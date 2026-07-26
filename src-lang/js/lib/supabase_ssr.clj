(ns js.lib.supabase-ssr
  (:require [net.http :as http]
            [tahto.core :as l]))

(l/script :js
  {:import [["@supabase/ssr" :as #{createServerClient}]]})

