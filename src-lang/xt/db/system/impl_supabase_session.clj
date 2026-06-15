

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
