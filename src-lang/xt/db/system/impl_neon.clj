(ns xt.db.system.impl-neon
  (:require [lang.core :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as common]
             [xt.db.text.pgrest-graph :as graph]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as http]
             [xt.net.addon-neon :as addon]
             [xt.db.system.impl-neon-realtime :as realtime]
             [xt.db.system.impl-neon-session :as session]]})

(defn.xt response-data
  [response]
  (return (addon/response-data response)))

(defn.xt request-input
  [impl input]
  (var defaults (xtd/get-in impl ["client" "defaults"]))
  (var path (xt/x:get-key input "path" nil))
  (when (== (str/substring path 0 9) "/rest/v1/")
    (:= path (str/substring path 8 (xt/x:len path))))
  (var headers (-> {"Accept" "application/json" "Content-Type" "application/json"}
                    (xt/x:obj-assign (xt/x:get-key defaults "headers" nil))
                    (xt/x:obj-assign (xt/x:get-key input "headers" nil))))
  (var token (xt/x:get-key defaults "token" nil))
  (when token (xt/x:set-key headers "Authorization" (xt/x:cat "Bearer " token)))
  (return (xt/x:obj-assign (xt/x:obj-clone input) {"headers" headers "url" (addon/join-url (xt/x:get-key defaults "data_url" nil) path)})))

(defn.xt request
  [impl input]
  (var refresh (:? (xtd/get-in impl ["state" "session"])
                    (session/refresh-session impl)
                    (promise/x:promise-run nil)))
  (return (-> refresh
    (promise/x:promise-then (fn [_] (return (fetch/request-http (xt/x:get-key impl "client" nil) (-/request-input impl input)))))
    (promise/x:promise-then -/response-data))))

(defn.xt cmd-pull-async
  [impl tree]
  (var #{schema lookup opts} impl)
  (var query (graph/select schema tree opts))
  (var schema-name (xt/x:get-path lookup [(xt/x:first tree) "schema"]))
  (var headers (xt/x:obj-assign {} (xt/x:get-key query "headers" nil)))
  (when schema-name
    (xt/x:set-key headers "Accept-Profile" schema-name))
  (return {"path" (xt/x:get-key query "url" nil) "method" "GET" "headers" headers}))

(defn.xt pull-async
  [impl tree]
  (return (-/request impl (-/cmd-pull-async impl tree))))

(defn.xt cmd-rpc-call-async
  [impl rpc-spec args]
  (var body {})
  (xt/for:array [[i input] (or (xt/x:get-key rpc-spec "input" nil) [])]
    (var key (or (xt/x:get-key input "symbol" nil) (xt/x:get-key input "name" nil)))
    (when key (xt/x:set-key body key (xt/x:get-idx args i))))
  (var headers {})
  (var schema-name (xt/x:get-key rpc-spec "schema" nil))
  (when schema-name
    (xt/x:set-key headers "Content-Profile" schema-name)
    (xt/x:set-key headers "Accept-Profile" schema-name))
  (return {"path" (xt/x:cat "/rpc/" (str/replace (xt/x:get-key rpc-spec "id" nil) "-" "_"))
                           "method" "POST" "headers" headers
                           "body" (xt/x:json-encode body)}))

(defn.xt rpc-call-async
  [impl rpc-spec args]
  (return (-/request impl (-/cmd-rpc-call-async impl rpc-spec args))))

(defn.xt stop-db
  [impl]
  (return (session/reset-session impl)))

(defimpl.xt ImplNeon
  [client schema lookup state listeners opts metadata]
  common/ISourceRemote
  {common/pull-async -/pull-async
   common/rpc-call-async -/rpc-call-async}
  common/ISourceRealtime
  {common/subscribe-db realtime/subscribe
   common/unsubscribe-db realtime/unsubscribe}
  common/ISourceLifecycle
  {common/stop-db -/stop-db}
  common/ISourceListener
  {common/add-db-listener common/add-db-listener-default
   common/remove-db-listener common/remove-db-listener-default
   common/get-db-listener common/get-db-listener-default})

(defn.xt impl-neon
  [client schema lookup]
  (return (-/ImplNeon client schema lookup {"connections" {}} {} {} {})))
