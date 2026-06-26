(ns scaffold.supabase.local-min-test
  (:use code.test)
  (:require [scaffold.supabase.local-min :as local-min]
            [std.lib.env :as env]
            [std.json :as json]
            [net.http :as http]))

(defn postgrest-url
  []
  (str "http://" (get-in local-min/+config+ [:api :hostname]) ":"
       (get-in local-min/+config+ [:api :port]) "/rest/v1/"))

(defn postgrest-headers
  []
  {"apikey" (get-in local-min/+config+ [:api :anon-key])
   "Authorization" (str "Bearer " (get-in local-min/+config+ [:api :anon-key]))})

^{:refer scaffold.supabase.local-min/restart-postgrest :added "4.1"
  :setup [(env/program-exists? "docker")]}
(fact "restarting only the postgrest container brings the api back"

  (local-min/restart-postgrest)
  => true

  (let [res (http/get (postgrest-url)
                      {:headers (postgrest-headers)
                       :as :string})]
    (:status res))
  => 200

  (-> (http/get (postgrest-url)
                {:headers (postgrest-headers)
                 :as :string})
      (:body)
      (json/read)
      (get "swagger"))
  => "2.0")
