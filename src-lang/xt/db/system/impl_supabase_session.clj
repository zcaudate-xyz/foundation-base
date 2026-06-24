(ns xt.db.system.impl-supabase-session
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.addon-supabase :as addon]]})

(defn.xt normalise-body
  "extracts the data payload from a supabase http response"
  {:added "4.1"}
  [response]
  (var out (xt/x:get-key response "body"))
  (cond (and (xt/x:is-object? out)
             (xt/x:not-nil? (xt/x:get-key out "data")))
        (return (xt/x:get-key out "data"))

        :else
        (return out)))

(defn.xt sync-session-token!
  "syncs the session access token into the wrapped http client defaults"
  {:added "4.1"}
  [impl]
  (var state (xt/x:get-key impl "state"))
  (var session (xt/x:get-key state "session"))
  (var client (xt/x:get-key impl "client"))
  (var access-token (or (xt/x:get-key session "access_token")
                        (xt/x:get-key session "token")
                        nil))
  (when (xt/x:not-nil? access-token)
    (var defaults (or (xt/x:get-key client "defaults") {}))
    (xt/x:set-key defaults "token" access-token)
    (xt/x:set-key client "defaults" defaults))
  (return access-token))

(defn.xt set-session!
  "sets the active session on the supabase impl and syncs the bearer token"
  {:added "4.1"}
  [impl session]
  (var state (xt/x:get-key impl "state"))
  (xt/x:set-key state "session" session)
  (-/sync-session-token! impl)
  (return impl))

(defn.xt get-session
  "returns the current session stored on the supabase impl"
  {:added "4.1"}
  [impl]
  (return (xt/x:get-key (xt/x:get-key impl "state") "session")))

(defn.xt resolve-refresh-interval
  "resolves the auto-refresh interval in milliseconds"
  {:added "4.1"}
  [impl opts]
  (var session (xt/x:get-key (xt/x:get-key impl "state") "session"))
  (var explicit (or (xt/x:get-key opts "refresh_interval")
                    (xt/x:get-key opts "interval")
                    nil))
  (when (xt/x:not-nil? explicit)
    (return explicit))
  (var expires-in (xt/x:get-key session "expires_in"))
  (cond (xt/x:not-nil? expires-in)
        (return (:? (>= (* expires-in 1000) 60000)
                    (- (* expires-in 1000) 60000)
                    1000))

        :else
        (return 300000)))

(defn.xt session-info
  "returns the current authenticated user information for the active session"
  {:added "4.1"}
  [impl]
  (-/sync-session-token! impl)
  (var client (xt/x:get-key impl "client"))
  (var session (xt/x:get-key (xt/x:get-key impl "state") "session"))
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
  (var client (xt/x:get-key impl "client"))
  (var session (xt/x:get-key (xt/x:get-key impl "state") "session"))
  (var refresh-token (xt/x:get-key session "refresh_token"))
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
  (var state (xt/x:get-key impl "state"))
  (var refresh (or (xt/x:get-key state "refresh") {}))
  (var timer (xt/x:get-key refresh "timer"))
  (when (xt/x:not-nil? timer)
    (clearInterval timer))
  (xt/x:set-key refresh "timer" nil)
  (xt/x:set-key state "refresh" refresh)
  (return impl))

(defn.xt start-auto-refresh
  "starts a session refresh timer using the current refresh token"
  {:added "4.1"}
  [impl opts]
  (:= opts (or opts {}))
  (var state (xt/x:get-key impl "state"))
  (var session (xt/x:get-key state "session"))
  (when (xt/x:nil? (xt/x:get-key session "refresh_token"))
    (return impl))
  (-/stop-auto-refresh impl)
  (var delay (-/resolve-refresh-interval impl opts))
  (var refresh (or (xt/x:get-key state "refresh") {}))
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
  (xt/x:set-key state "refresh" refresh)
  (return impl))
