(ns xt.db.runtime.model-view
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt source-base
  "creates the base structural definition for a source role"
  {:added "4.1"}
  [source-id]
  (return {"id" source-id
           "data" []
           "updated_at" nil
           "synced_at" nil
           "sync_from" (:? (== source-id "caching")
                          "primary"
                          nil)}))

(defn.xt normalize-source
  "normalizes a named model source"
  {:added "4.1"}
  [source-id current source]
  (:= current (or current {}))
  (:= source (or source {}))
  (var out
       (xt/x:obj-assign
        (-/source-base source-id)
        (xt/x:obj-assign
         {"id" source-id
          "sync_from" (or (xt/x:get-key source "sync_from")
                         (xt/x:get-key current "sync_from")
                         (:? (== source-id "caching")
                             "primary"
                             nil))}
         (xt/x:obj-assign current source))))
  (var config
       (xt/x:obj-assign
        (or (xt/x:get-key current "config") {})
        (or (xt/x:get-key source "config") {})))
  (when (> (xt/x:len (xt/x:obj-keys config)) 0)
    (xt/x:set-key out "config" config))
  (var setup
       (xt/x:obj-assign
        (or (xt/x:get-key current "setup") {})
        (or (xt/x:get-key source "setup") {})))
  (when (> (xt/x:len (xt/x:obj-keys setup)) 0)
    (xt/x:set-key out "setup" setup))
  (return out))

(defn.xt normalize-sources
  "normalizes model sources with primary and caching defaults"
  {:added "4.1"}
  [defaults sources]
  (var out {"primary" (-/source-base "primary")
            "caching" (-/source-base "caching")})
  (xt/for:object [[source-id source] (or defaults {})]
    (xt/x:set-key out
                  source-id
                  (-/normalize-source
                   source-id
                   (xt/x:get-key out source-id)
                   source)))
  (xt/for:object [[source-id source] (or sources {})]
    (xt/x:set-key out
                  source-id
                  (-/normalize-source
                   source-id
                   (xt/x:get-key out source-id)
                   source)))
  (return out))

(defn.xt normalize-view-source
  "normalizes the source role declared by a view"
  {:added "4.1"}
  [view]
  (return
   (or (xt/x:get-key view "source")
       (xtd/get-in view ["use" "source"])
       "caching")))
