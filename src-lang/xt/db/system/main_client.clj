(ns xt.db.system.main-client
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt get-provider
  "Resolves an explicitly selected provider; never loads drivers implicitly."
  [type backends]
  (var selected (or backends {}))
  (var provider (xt/x:get-key selected type nil))
  (when (xt/x:nil? provider)
    (throw (xt/x:ex "Database backend is not selected"
                    {"type" type "selected" (xt/x:obj-keys selected)})))
  (return provider))

(defn.xt create-client
  [type defaults (:.. optional)]
  (var backends (:? (> (xt/x:len optional) 0) (xt/x:first optional) nil))
  (when (== type "memory") (return nil))
  (var provider (-/get-provider type backends))
  (var create (xt/x:get-key provider "create_client" nil))
  (when (not (xt/x:is-function? create)) (xt/x:err "Backend provider is missing create_client"))
  (var client (create defaults))
  (when (xt/x:nil? client) (xt/x:err "Selected backend has no client for this runtime"))
  (return client))
