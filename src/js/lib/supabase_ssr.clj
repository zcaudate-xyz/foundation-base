(ns js.lib.supabase-ssr
  (:require [std.lang :as l]
            [std.lib :as h]
            [net.http :as http]))

(l/script :js
  {:import [["@supabase/ssr" :as #{createServerClient}]]
   :export [MODULE]})

(def.js MODULE
  (!:module))
