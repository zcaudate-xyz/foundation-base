(ns xt.cell.service
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.xt service?
  "checks if the value is a service registry"
  {:added "4.0"}
  [service]
  (return (and (k/obj? service)
               (k/has-key? service "dbs"))))

(defn.xt create-service
  "creates a service registry"
  {:added "4.0"}
  [dbs]
  (return {"dbs" (or dbs {})}))

(defn.xt get-dbs
  "gets all registered dbs"
  {:added "4.0"}
  [service]
  (return (or (k/get-key service "dbs")
              {})))

(defn.xt get-db
  "gets a registered db by id"
  {:added "4.0"}
  [service db-id]
  (return (k/get-key (-/get-dbs service) db-id)))

(defn.xt assoc-db
  "associates a db into the registry"
  {:added "4.0"}
  [service db-id db]
  (var out (k/obj-clone (or service {})))
  (var dbs (k/obj-clone (-/get-dbs out)))
  (k/set-key dbs db-id db)
  (k/set-key out "dbs" dbs)
  (return out))

(defn.xt resolve-db
  "resolves a db reference from a descriptor or context"
  {:added "4.0"}
  [service descriptor view-context]
  (var db-ref (or (k/get-key descriptor "db")
                  (k/get-key view-context "db")))
  (cond (k/is-string? db-ref)
        (return (-/get-db service db-ref))

        (k/obj? db-ref)
        (return db-ref)

        :else
        (return nil)))

(def.xt MODULE (!:module))
