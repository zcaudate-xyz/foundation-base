(ns js.lib.driver-postgres
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-runtime :as rt]
             [js.core.util :as ut]]
   :import  [["pg" :as [* Postgres]]]})

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
  (var env (k/obj-assign (k/obj-clone (-/default-env)) m))
  (rt/xt-config-set "js.lib.driver-postgres" env)
  (return env))

(h/template-entries [l/tmpl-entry {:type :fragment
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
        (return (new Promise
                     (fn [resolve]
                       (. conn (query input
                                      (fn [err res]
                                        (when res
                                          (var #{rows} res)
                                          (when (== 1 rows.length)
                                            (resolve (callback nil (k/obj-first-val
                                                                   (k/first rows)))))
                                          (resolve (callback nil rows)))
                                        (resolve (callback err nil))))))))))
  (:= (. conn ["::query_sync"])
      (fn [query]
        (throw "Not Allowed")))
  (return conn))

(defn.js connect-constructor
  "constructs the postgres instance"
  {:added "4.0"}
  [m callback]
  (:= callback (or callback ut/pass-callback))
  (var env (k/obj-assign (-/default-env)
                         m))
  (var conn (new -/Client env))
  
  (. conn
     (connect)
     (then (fn [] (callback nil conn))))
  (return (-/set-methods conn)))

