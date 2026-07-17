(ns xt.db.system.main-client
  (:require [hara.lang :as l]))


(l/script :xtalk)

(defabstract.xt create-client
  [type defaults])

(defn.xt create-client
  "fallback client factory - returns nil for types without a runtime adapter"
  {:added "4.1"}
  [type defaults]
  (return nil))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.addon-supabase :as addon]
             [xt.net.conn-sql :as conn-sql]
             [js.net.http-fetch :as js-fetch]
             [js.net.conn-sqlite :as js-sqlite]
             [js.net.conn-postgres :as js-postgres]]})

(defn.js create-client
  [type defaults]
  (cond (== type "sqlite")
        (return
         (js-sqlite/create defaults))

        (== type "postgres")
        (return
         (js-postgres/create defaults))

        (== type "supabase")
        (return
         (js-fetch/create defaults (addon/middleware-supabase)))))
