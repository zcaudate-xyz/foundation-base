(ns xt.db.runtime.supabase-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.client-supabase :as system-supabase]
             [xt.lib.supabase :as supabase]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.xt client?
  {:added "4.1.3"}
  [obj]
  (return (supabase/client? obj)))

(defn.xt raw-client
  {:added "4.1.3"}
  [client]
  (return (supabase/raw-client client)))

(defn.xt resolve-transport
  {:added "4.1.3"}
  [client]
  (return (supabase/resolve-transport client)))

(defn.xt resolve-base-url
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-base-url db client opts)))

(defn.xt resolve-schema-name
  {:added "4.1.3"}
  [db client opts]
  (return (system-supabase/resolve-schema-name client opts)))

(defn.xt resolve-api-key
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-api-key db client opts)))

(defn.xt resolve-auth-token
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-auth-token db client opts)))

(defn.xt create-scaffold
  {:added "4.1.3"}
  [db client opts]
  (return (system-supabase/create-scaffold client opts)))

(defn.xt join-url
  {:added "4.1.3"}
  [base_url path]
  (return (supabase/join-url base_url path)))

(defn.xt resolve-request-headers
  {:added "4.1.3"}
  [db client request opts]
  (return (system-supabase/resolve-request-headers client request opts)))

(defn.xt prepare-request
  {:added "4.1.3"}
  [db client input opts]
  (return (system-supabase/prepare-request client input opts)))

(defn.xt dispatch-request
  {:added "4.1.3"}
  [raw input opts]
  (var transport (-/resolve-transport raw))
  (var request (-/prepare-request nil raw input (or opts {})))
  (return (fetch/request transport request opts)))

(defn.xt unwrap-response
  {:added "4.1.3"}
  [response]
  (return (system-supabase/unwrap-response response)))

(defn.xt client
  {:added "4.1.3"}
  [raw]
  (when (-/client? raw)
    (return raw))
  (var source nil)
  (if (fetch/client? raw)
    (:= source {"transport" raw})
    (if (xt/x:nil? raw)
      (:= source {})
      (:= source (xt/x:obj-clone raw))))
  (xt/x:set-key source "::supabase" "supabase.client")
  (return
   (fetch/client-create
    source
    {"request" -/dispatch-request})))

(defn.xt resolve-client
  {:added "4.1.3"}
  [db opts]
  (return (system-supabase/resolve-client db opts)))

(defn.xt pull
  {:added "4.1.3"}
  [db schema query-plan opts]
  (return (system-supabase/pull db schema query-plan opts)))
