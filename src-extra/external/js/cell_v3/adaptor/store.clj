(ns js.cell-v3.adaptor.store
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]]})

(defn.js make-registry
  "creates a store registry"
  {:added "4.0"}
  []
  (return {"::" "cell-v3.store-registry"
           :entries {}}))

(defn.js register-store
  "registers a store adaptor"
  {:added "4.0"}
  [registry key adaptor]
  (var prev (. registry ["entries"] [key]))
  (:= (. registry ["entries"] [key])
      (j/assign {:key key}
                (or adaptor {})))
  (return prev))

(defn.js get-store
  "gets a store adaptor"
  {:added "4.0"}
  [registry key]
  (return (. registry ["entries"] [key])))

(defn.js list-stores
  "lists store keys"
  {:added "4.0"}
  [registry]
  (return (k/obj-keys (. registry ["entries"]))))

(defn.js invoke-store
  "invokes a store adaptor operation"
  {:added "4.0"}
  [registry key op input]
  (var entry (-/get-store registry key))
  (when (k/nil? entry)
    (k/err (k/cat "ERR - Store not found - " key)))
  (var f (or (k/get-key entry op)
             (:? (== op "query")
                 (k/get-key entry "read")
                 nil)))
  (when (k/nil? f)
    (k/err (k/cat "ERR - Store operation not found - " key " " op)))
  (return (f input entry registry)))

(defn.js store-read
  "calls store read"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "read" input)))

(defn.js store-write
  "calls store write"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "write" input)))

(defn.js store-sync
  "calls store sync"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "sync" input)))

(defn.js store-clear
  "calls store clear"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "clear" input)))

(defn.js store-remove
  "calls store remove"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "remove" input)))

(defn.js store-query
  "calls store query"
  {:added "4.0"}
  [registry key input]
  (return (-/invoke-store registry key "query" input)))
