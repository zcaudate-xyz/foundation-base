(ns xt.cell.service
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]
   :export  [MODULE]})

(defn.xt service?
  "checks if the value is a service registry"
  {:added "4.0"}
  [service]
  (return (and (xt/x:is-object? service)
               (xt/x:has-key? service "dbs"))))

(defn.xt create-service
  "creates a service registry"
  {:added "4.0"}
  [dbs]
  (return {"dbs" (or dbs {})}))

(defn.xt get-dbs
  "gets all registered dbs"
  {:added "4.0"}
  [service]
  (return (or (xt/x:get-key service "dbs")
              {})))

(defn.xt get-db
  "gets a registered db by id"
  {:added "4.0"}
  [service db-id]
  (return (xt/x:get-key (-/get-dbs service) db-id)))

(defn.xt assoc-db
  "associates a db into the registry"
  {:added "4.0"}
  [service db-id db]
  (var out (xt/x:obj-clone (or service {})))
  (var dbs (xt/x:obj-clone (-/get-dbs out)))
  (xt/x:set-key dbs db-id db)
  (xt/x:set-key out "dbs" dbs)
  (return out))

(defn.xt resolve-db
  "resolves a db reference from a descriptor or context"
  {:added "4.0"}
  [service descriptor view-context]
  (var db-ref (or (xt/x:get-key descriptor "db")
                  (xt/x:get-key view-context "db")))
  (cond (xt/x:is-string? db-ref)
        (return (-/get-db service db-ref))

        (xt/x:is-object? db-ref)
        (return db-ref)

        :else
        (return nil)))

(def.xt MODULE (!:module))
