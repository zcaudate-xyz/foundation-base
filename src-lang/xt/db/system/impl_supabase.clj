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
             [xt.net.addon-supabase :as addon]]})

(defn.xt normalise-body
  [response]
  (var out (xt/x:get-key response "body"))
  (cond (and (xt/x:is-object? out)
             (xt/x:not-nil? (xt/x:get-key out "data")))
        (return (xt/x:get-key out "data"))
        
        :else
        (return out)))

(defn.xt sync-session-token!
  [impl]
  (var #{client session} impl)
  (var access-token (or (xt/x:get-key session "access_token")
                        (xt/x:get-key session "access-token")
                        (xt/x:get-key session "token")
                        (xt/x:get-key session "auth_token")
                        nil))
  (when (xt/x:not-nil? access-token)
    (var defaults (or (xt/x:get-key client "defaults") {}))
    (xt/x:set-key defaults "token" access-token)
    (xt/x:set-key client "defaults" defaults))
  (return access-token))

(defn.xt set-session!
  [impl session]
  (xt/x:set-key impl "session" session)
  (-/sync-session-token! impl)
  (return impl))

(defn.xt resolve-refresh-interval
  [impl opts]
  (var session (xt/x:get-key impl "session"))
  (var explicit (or (xt/x:get-key opts "interval")
                    (xt/x:get-key opts "refresh_interval")
                    (xt/x:get-key opts "ms")
                    nil))
  (when (xt/x:not-nil? explicit)
    (return explicit))
  (var expires-in (or (xt/x:get-key session "expires_in")
                      (xt/x:get-key session "expires-in")
                      nil))
  (cond (xt/x:not-nil? expires-in)
        (return (:? (>= (* expires-in 1000) 60000)
                    (- (* expires-in 1000) 60000)
                    1000))

        :else
        (return 300000)))

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

(defn.xt session-info
  "returns the current authenticated user information for the active session"
  {:added "4.1"}
  [impl]
  (-/sync-session-token! impl)
  (var #{client session} impl)
  (cond (xt/x:nil? (xt/x:get-key session "access_token"))
        (return (promise/x:promise-run session))

        :else
        (return
         (-> (http-fetch/request-http client
                                     (addon/cmd-user-get {}))
             (promise/x:promise-then
              (fn [response]
                (return {"session" session
                         "user" (-/normalise-body response)})))
             (promise/x:promise-catch
              (fn [err]
                (return {"session" session
                         "error" (-/normalise-body err)})))))))

(defn.xt refresh-session
  "refreshes the active session token using the stored refresh token"
  {:added "4.1"}
  [impl]
  (var #{client session} impl)
  (var refresh-token (or (xt/x:get-key session "refresh_token")
                         (xt/x:get-key session "refresh-token")
                         nil))
  (cond (xt/x:nil? refresh-token)
        (return (promise/x:promise-run session))

        :else
        (return
         (-> (http-fetch/request-http client
                                     (addon/cmd-token-refresh
                                      {"refresh_token" refresh-token}
                                      {}))
             (promise/x:promise-then
              (fn [response]
                (var refreshed (-/normalise-body response))
                (when (xt/x:is-object? refreshed)
                  (-/set-session! impl refreshed))
                (return refreshed)))
             (promise/x:promise-catch
              (fn [err]
                (return (promise/x:promise-run err))))))))

(defn.xt stop-auto-refresh
  "stops the session refresh timer"
  {:added "4.1"}
  [impl]
  (var refresh (or (xt/x:get-key impl "refresh") {}))
  (var timer (xt/x:get-key refresh "timer"))
  (when (xt/x:not-nil? timer)
    (clearInterval timer))
  (xt/x:set-key refresh "timer" nil)
  (xt/x:set-key impl "refresh" refresh)
  (return impl))

(defn.xt start-auto-refresh
  "starts a session refresh timer using the current refresh token"
  {:added "4.1"}
  [impl opts]
  (:= opts (or opts {}))
  (var session (xt/x:get-key impl "session"))
  (when (xt/x:nil? (xt/x:get-key session "refresh_token"))
    (return impl))
  (-/stop-auto-refresh impl)
  (var delay (-/resolve-refresh-interval impl opts))
  (var refresh (or (xt/x:get-key impl "refresh") {}))
  (var timer
       (setInterval
        (fn []
          (-> (-/refresh-session impl)
              (promise/x:promise-catch
               (fn [err]
                 (return err)))))
        delay))
  (xt/x:set-key refresh "timer" timer)
  (xt/x:set-key refresh "interval" delay)
  (xt/x:set-key impl "refresh" refresh)
  (return impl))

(defimpl.xt ImplSupabase
  [client schema lookup session refresh opts]
  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async})

(defn.xt impl-supabase
  [client schema lookup session opts]
  (return
   (-/ImplSupabase client schema lookup session nil (or opts {}))))
