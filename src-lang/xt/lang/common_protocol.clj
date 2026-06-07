(ns xt.lang.common-protocol
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(defn.xt iface-combine
  "combines interface vectors into a stable, deduped protocol surface"
  {:added "4.1"}
  [interfaces]
  (var seen {})
  (var out [])
  (xt/for:array [iface interfaces]
    (xt/for:array [key iface]
      (when (xt/x:nil? (xt/x:get-key seen key))
        (xt/x:set-key seen key true)
        (xt/x:arr-push out key))))
  (return out))

(defn.xt proto-group
  "creates a grouped protocol entry from interface vectors and an implementation map"
  {:added "4.1"}
  [interfaces spec-map]
  (return [(-/iface-combine interfaces) spec-map]))

(defn.xt proto-spec
  "creates a validated protocol map from grouped protocol entries"
  {:added "4.1"}
  [spec-arr]
  (var acc {})
  (xt/for:array [entry spec-arr]
    (var required (xt/x:first entry))
    (var spec-map (xt/x:second entry))
    (xt/for:array [key required]
      (when (xt/x:nil? (xt/x:get-key spec-map key))
        (throw (xt/x:ex "Invalid Key"
                        {:required key
                         :actual (xtd/obj-keys spec-map)}))))
    (:= acc (xt/x:obj-assign acc spec-map)))
  (return acc))


(defn.xt ensure-promise
  "wraps sync values in a native host promise while passing promises through"
  {:added "4.1.3"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))
