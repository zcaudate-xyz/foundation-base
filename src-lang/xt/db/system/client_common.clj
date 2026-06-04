(ns xt.db.system.client-common
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt normalize-client
  "creates the common client record envelope"
  {:added "4.1"}
  [raw tag]
  (var out (xt/x:obj-clone (or raw {})))
  (xt/x:set-key out "::" tag)
  (when (not (xt/x:is-object? (xt/x:get-key out "schema")))
    (xt/x:set-key out "schema" {}))
  (when (not (xt/x:is-object? (xt/x:get-key out "lookup")))
    (xt/x:set-key out "lookup" {}))
  (when (not (xt/x:is-object? (xt/x:get-key out "opts")))
    (xt/x:set-key out "opts" {}))
  (when (not (xt/x:is-object? (xt/x:get-key out "settings")))
    (xt/x:set-key out "settings" {}))
  (return out))

(defn.xt attach-settings
  "copies driver-specific fields into the common settings map"
  {:added "4.1"}
  [client keys]
  (var settings (xt/x:obj-clone (or (xt/x:get-key client "settings") {})))
  (xt/for:array [k keys]
    (when (xt/x:has-key? client k)
      (xt/x:set-key settings k (xt/x:get-key client k))))
  (xt/x:set-key client "settings" settings)
  (return client))

(defn.xt create-client
  "creates a client record with the common shape and settings map"
  {:added "4.1"}
  [raw tag keys]
  (var out (-/normalize-client raw tag))
  (return (-/attach-settings out keys)))

(defn.xt get-setting
  "reads a driver-specific setting from the common settings map or top-level fields"
  {:added "4.1"}
  [client key]
  (return (or (xt/x:get-key (xt/x:get-key client "settings") key)
              (xt/x:get-key client key)
              nil)))
