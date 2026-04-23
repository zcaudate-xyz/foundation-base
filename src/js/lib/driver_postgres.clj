(ns js.lib.driver-postgres
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:import [["pg" :as [* Postgres]]] :require [[xt.lang.common-lib :as k] [xt.lang.common-data :as xtd] [xt.lang.common-runtime :as rt] [js.core.util :as ut] [xt.lang.spec-base :as xt]]})

(defn.js default-env
  "gets the default env"
  {:added "4.0"}
  []
  (return (or (rt/xt-config "js.lib.driver-postgres")
              {:host     "127.0.0.1"
               :port     "5432"
               :user     "postgres"
               :password "postgres"
               :database "test"})))

(defn.js default-env-set
  "sets the default env"
  {:added "4.0"}
  [m]
  (var env (xtd/obj-assign (xtd/obj-clone (-/default-env)) m))
  (rt/xt-config-set "js.lib.driver-postgres" env)
  (return env))

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Postgres"
                                   :tag "js"}]
  [Client])

(defn.js set-methods
  "sets the methods for the object"
  {:added "4.0"}
  [conn]
  (:= (. conn ["::disconnect"])
      (fn [callback]
        (:= callback (or callback ut/pass-callback))
        (return (ut/wrap-callback (. conn (end))
                                  callback))))
  (:= (. conn ["::query"])
      (fn [input callback]
        (:= callback (or callback ut/pass-callback))
        (return
         (ut/wrap-callback
          (. conn (query input))
          (fn [err res]
            (when err
              (return (callback err nil)))
            (var #{rows} res)
            (if (and (== 1 rows.length)
                     (== 1 (xt/x:len (xtd/obj-keys (xtd/first rows)))))
              (return (callback nil
                                (xtd/obj-first-val
                                 (xtd/first rows))))
              (return (callback nil rows))))))))
  (:= (. conn ["::query_sync"])
      (fn [query]
        (throw "Not Allowed")))
  (return conn))

(defn.js connect-constructor
  "constructs the postgres instance"
  {:added "4.0"}
  [m callback]
  (:= callback (or callback ut/pass-callback))
  (var env (xtd/obj-assign (-/default-env)
                          m))
  (var conn (new -/Client env))
  
  (. conn
     (connect)
     (then (fn [] (callback nil conn))
           (fn [err] (callback err nil))))
  (return (-/set-methods conn)))
