(ns xt.db.system.impl-supabase
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.db.text.pgrest-tree :as pgrest-tree]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.http-supabase :as lib-supabase]]})

(defn.xt pull-async
  "runs a tree ir pull with async supabase semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         lookup
         opts} impl)
  (var request (pgrest-graph/select schema tree opts))
  (var table-name (xt/x:first tree))
  (var schema-name (xt/x:get-path lookup [table-name "schema"]))
  (var headers (-> {}
                   (xt/x:obj-assign (xt/x:get-key request "headers"))
                   (xt/x:obj-assign (:? schema-name
                                        {"Accept-Profile" schema-name
                                         "Content-Profile" schema-name}))))
  (return
   (-> (http-fetch/request-http client
                                (xt/x:obj-assign {:path (xt/x:get-key request "url")
                                                  :method "GET"}
                                                 {"headers" headers}))
       (promise/x:promise-then
        (fn [response]
          (var out (xt/x:get-key response "body"))
          (cond (and (xt/x:is-object? out)
                     (xt/x:not-nil? (xt/x:get-key out "data")))
                (return (xt/x:get-key out "data"))

                :else
                (return out)))))))

(defn.xt rpc-call-async
  [impl rpc-spec args opts]
  (var #{client} impl)
  (var input-spec (or (xt/x:get-key rpc-spec "input") []))
  (var body {})
  (:= opts (or opts {}))
  (xt/for:array [[i input] input-spec]
    (var key (or (xt/x:get-key input "symbol")
                 (xt/x:get-key input "name")
                 nil))
    (when (xt/x:not-nil? key)
      (xt/x:set-key body key (xt/x:get-idx args i))))
  (var schema  (xt/x:get-key rpc-spec "schema"))
  (var headers (xt/x:obj-clone (xt/x:get-key opts "headers")))
  (when (xt/x:not-nil? schema)
    (xt/x:set-key headers "Content-Profile" schema)
    (xt/x:set-key headers "Accept-Profile" schema))
  (return
   (-> (lib-supabase/rpc-call client
                              (xt/x:get-key rpc-spec "id")
                              body
                              (-> (xt/x:obj-clone opts)
                                  (xt/x:obj-assign {"headers" headers})))
       (promise/x:promise-then
        (fn [response]
          (var out (xt/x:get-key response "body"))
          (cond (and (xt/x:is-object? out)
                     (xt/x:not-nil? (xt/x:get-key out "data")))
                (return (xt/x:get-key out "data"))

                :else
                (return out)))))))

(defimpl.xt ImplSupabase
  [client schema lookup opts]

  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async})

(defn.xt impl-supabase
  [client schema lookup]
  (return
   (-/ImplSupabase client schema lookup {})))


