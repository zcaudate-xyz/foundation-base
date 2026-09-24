(ns xt.db.node.site-map
  "Loads and caches the static schema/RPC publication for a worker."
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(defn.xt fetch-json
  "Fetches and decodes one static JSON publication."
  {:added "4.1.5"}
  [url]
  (var fetch-fn (!:G fetch))
  (when (xt/x:nil? fetch-fn)
    (xt/x:err "SharedWorker site-map loading requires fetch"))
  (return
   (-> (fetch-fn url)
       (promise/x:promise-then
        (fn [response]
          (when (and (xt/x:has-key? response "ok")
                     (not (. response ["ok"])))
            (xt/x:err (xt/x:cat "Unable to load SharedWorker site map: " url)))
          (return
           (promise/x:promise-then
            (. response (text))
            (fn [body]
              (return (xt/x:json-decode body))))))))))

(defn.xt load-remote
  "Loads the manifest followed by its schema, lookup, and RPC payloads."
  {:added "4.1.5"}
  [url]
  (return
   (-> (-/fetch-json url)
       (promise/x:promise-then
        (fn [manifest]
          (var schema-url (or (. manifest ["schema_url"])
                              (. manifest ["schema"])))
          (var lookup-url (or (. manifest ["lookup_url"])
                              (. manifest ["lookup"])))
          (var rpc-url (or (. manifest ["rpc_url"])
                           (. manifest ["rpc"])))
          (return
           (-> (promise/x:promise-all
                [(-/fetch-json schema-url)
                 (-/fetch-json lookup-url)
                 (-/fetch-json rpc-url)])
               (promise/x:promise-then
                (fn [values]
                  (var [schema lookup rpc] values)
                  (return {"schema" schema
                           "lookup" lookup
                           "rpc" rpc
                           "manifest" manifest}))))))))))

(defn.xt load-site-map
  "Loads a site map once per worker and shares the in-flight promise."
  {:added "4.1.5"}
  [node config schema lookup]
  (var meta (. node ["meta"]))
  (var url (or (. config ["site_map_url"])
               (. config ["site-map-url"])))
  (var cached (xtd/get-in meta ["xt.db/site-map"]))
  (when (xt/x:not-nil? cached)
    (when (and (xt/x:not-nil? url)
               (not= url (. cached ["url"])))
      (xt/x:err (xt/x:cat "SharedWorker already loaded site map: "
                          (. cached ["url"]))))
    (return (or (. cached ["promise"])
                (promise/x:promise-run (. cached ["data"])))))
  (when (xt/x:nil? url)
    (var fallback {"schema" schema
                   "lookup" lookup
                   "rpc" {}})
    (xt/x:set-key meta
                  "xt.db/site-map"
                  {"url" nil
                   "data" fallback})
    (return
     (promise/x:promise-run fallback)))
  (var pending (-/load-remote url))
  (var guarded
       (-> pending
           (promise/x:promise-then
            (fn [data]
              (xt/x:set-key meta
                            "xt.db/site-map"
                            {"url" url
                             "data" data})
              (return data)))
           (promise/x:promise-catch
            (fn [err]
              (xt/x:del-key meta "xt.db/site-map")
              (xt/x:err err)))))
  (xt/x:set-key meta
                "xt.db/site-map"
                {"url" url
                 "promise" guarded})
  (return guarded))
