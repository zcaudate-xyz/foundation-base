(ns xt.db.system.impl-supabase-session
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
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
  (xtd/set-in impl ["state" "session"] session)
  (return session))

(defn.xt refresh-session
  "refreshes the active session token using the stored refresh token"
  {:added "4.1"}
  [impl]
  (var #{client}     impl)
  (var session       (xtd/get-in impl ["state" "session"]))
  (var refresh-token (xtd/get-in session "refresh_token"))
  (cond (xt/x:nil? refresh-token)
        (return (promise/x:promise-run nil))

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

(defn.xt auto-refresh-interval
  [impl]
  (var expires-in (xtd/get-in impl ["state" "session" "expires_in"]))
  (cond (xt/x:not-nil? expires-in)
        (return (:? (>= (* expires-in 1000) 60000)
                    (- (* expires-in 1000) 60000)
                    1000))

        :else
        (return (or (xtd/get-in impl ["opts" "auto_refresh_interval"])
                    300000))))

(defn.xt auto-refresh-fn
  [impl delay refresh-id]
  (var current-id (xtd/get-in impl ["state" "auto_refresh" "current"]))
  (cond (== current-id refresh-id)
        (do (-> (-/refresh-session impl)
                (promise/x:promise-catch
                 (fn [err]
                   (return err)))
                (promise/x:promise-then
                 (fn [_]
                   (return (promise/x:with-delay
                            delay
                            (fn []
                              (return (-/auto-refresh-fn impl (-/auto-refresh-interval impl) refresh-id))))))))
            (return refresh-id))
        :else
        (return current-id)))

(defn.xt auto-refresh-stop
  [impl]
  (var current-id (xtd/get-in impl ["state" "auto_refresh" "current"]))
  (xtd/set-in impl ["state" "auto_refresh" "current"] nil)
  (return current-id))

(defn.xt auto-refresh-start
  [impl]
  (var current-id (xtd/get-in impl ["state" "auto_refresh" "current"]))
  (when (xt/x:not-nil? current-id)
    (return current-id))
  (var refresh-id (xts/str-rand 8))
  (xtd/set-in impl ["state" "auto_refresh" "current"] refresh-id)
  (return (-/auto-refresh-fn impl (-/auto-refresh-interval impl) refresh-id)))




