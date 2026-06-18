(ns xt.db.system.impl-supabase
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-supabase-pubsub :as pubsub]
             [xt.db.system.impl-supabase-session :as session]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.db.text.pgrest-tree :as pgrest-tree]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.addon-supabase :as addon]]})

(defn.xt cmd-pull-async
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
   (xt/x:obj-assign {:path (xt/x:get-key request "url")
                     :method "GET"}
                    {"headers" headers})))

(defn.xt normalise-body
  [response]
  (var out (xt/x:get-key response "body"))
  (cond (and (xt/x:is-object? out)
             (xt/x:not-nil? (xt/x:get-key out "data")))
        (return (xt/x:get-key out "data"))

        :else
        (return out)))

(defn.xt pull-async
  "runs a tree ir pull with async supabase semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client} impl)
  (var input (-/cmd-pull-async impl tree))
  (return
   (-> (http-fetch/request-http client input)
       (promise/x:promise-then -/normalise-body))))

(defn.xt cmd-rpc-call-async
  [impl rpc-spec args opts]
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
   (addon/cmd-rpc-call (xt/x:get-key rpc-spec "id")
                       body
                       (-> (xt/x:obj-clone opts)
                           (xt/x:obj-assign {"headers" headers})))))

(defn.xt rpc-call-async
  [impl rpc-spec args opts]
  (var #{client} impl)
  (var input (-/cmd-rpc-call-async impl rpc-spec args opts))
  (return
   (-> (http-fetch/request-http client input)
       (promise/x:promise-then -/normalise-body))))

;;
;; SESSION
;;

(defn.xt set-session!
  "sets the active session on the supabase impl and syncs the bearer token"
  {:added "4.1"}
  [impl session]
  (return (session/set-session! impl session)))

(defn.xt get-session
  "returns the current session stored on the supabase impl"
  {:added "4.1"}
  [impl]
  (return (session/get-session impl)))

(defn.xt session-info
  "returns the current authenticated user information for the active session"
  {:added "4.1"}
  [impl]
  (return (session/session-info impl)))

(defn.xt refresh-session
  "refreshes the active session token using the stored refresh token"
  {:added "4.1"}
  [impl]
  (return (session/refresh-session impl)))

(defn.xt start-auto-refresh
  "starts a session refresh timer using the current refresh token"
  {:added "4.1"}
  [impl opts]
  (return (session/start-auto-refresh impl opts)))

(defn.xt stop-auto-refresh
  "stops the session refresh timer"
  {:added "4.1"}
  [impl]
  (return (session/stop-auto-refresh impl)))

;;
;; PUBSUB
;;

(defn.xt subscribe
  "subscribes the supabase impl to realtime changes on a topic"
  {:added "4.1"}
  [impl topic opts callback]
  (return (pubsub/subscribe impl topic opts callback)))

(defn.xt unsubscribe
  "unsubscribes a supabase realtime handle"
  {:added "4.1"}
  [impl handle]
  (return (pubsub/unsubscribe impl handle)))

(defn.xt publish
  "no-op publish for the supabase realtime abstraction"
  {:added "4.1"}
  [impl topic message opts]
  (return (pubsub/publish impl topic message opts)))

(defimpl.xt ImplSupabase
  [client schema lookup session refresh opts]
  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async}

  impl-common/ISession
  {impl-common/set-session      -/set-session!
   impl-common/get-session      -/get-session
   impl-common/session-info     -/session-info
   impl-common/refresh-session  -/refresh-session
   impl-common/start-auto-refresh -/start-auto-refresh
   impl-common/stop-auto-refresh  -/stop-auto-refresh}

  impl-common/IPubSub
  {impl-common/subscribe   -/subscribe
   impl-common/unsubscribe -/unsubscribe
   impl-common/publish     -/publish})

(defn.xt impl-supabase
  [client schema lookup session opts]
  (return
   (-/ImplSupabase client schema lookup session nil (or opts {}))))
