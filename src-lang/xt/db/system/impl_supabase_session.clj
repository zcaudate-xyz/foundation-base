(ns xt.db.system.impl-supabase-session
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.net.http-fetch :as http-fetch]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.net.addon-supabase :as addon]]})

(defn.xt get-session
  "returns the current session stored on the supabase impl"
  {:added "4.1"}
  [impl]
  (return (xtd/get-in impl ["state" "session"])))

(defn.xt set-session
  "sets the active session on the supabase impl and syncs the bearer token"
  {:added "4.1"}
  [impl session]
  (return (xtd/set-in impl ["state" "session"] session)))

(defn.xt resolve-refresh-interval
  "resolves the auto-refresh interval in milliseconds"
  {:added "4.1"}
  [impl opts]
  (var session  (xtd/get-in impl ["state" "session"]))
  
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

(defn.xt refresh-session
  "refreshes the active session token using the stored refresh token"
  {:added "4.1"}
  [impl]
  (var client (xt/x:get-key impl "client"))
  (var session  (or (xtd/get-in impl ["state" "session"])
                    {}))
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
                (return (-/set-session impl (impl-supabase/normalise-body response)))))
             (promise/x:promise-catch
              (fn [err]
                (return err)))))))

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
