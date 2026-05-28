(ns lib.supabase.auth
  (:require [lib.supabase.common :as common]))

(defn has-session?
  "Checks whether the payload looks like a Supabase token response."
  {:added "4.1.4"}
  [data]
  (boolean
   (and (get data "access_token")
        (get data "refresh_token")
        (get data "expires_in"))))

(defn token-response->session
  "Normalizes a token response into a session map."
  {:added "4.1.4"}
  [data]
  (when (has-session? data)
    (cond-> (assoc data
                   "expires_at"
                   (or (get data "expires_at")
                       (+ (quot (System/currentTimeMillis) 1000)
                          (long (get data "expires_in")))))
      true identity)))

(defn set-session!
  "Stores a Supabase session and user on the client."
  {:added "4.1.4"}
  ([client session]
   (set-session! client session (or (get session "user") nil)))
  ([client session user]
   (common/swap-state! client assoc
                       :session session
                       :user user
                       :auth_token (get session "access_token")
                       :refresh_token (get session "refresh_token"))
   client))

(defn clear-session!
  "Clears the stored auth state."
  {:added "4.1.4"}
  [client]
  (common/swap-state! client assoc
                      :session nil
                      :user nil
                      :auth_token nil
                      :refresh_token nil)
  client)

(defn get-session
  "Returns the stored session."
  {:added "4.1.4"}
  [client]
  {:data {:session (:session (common/raw-state client))}
   :error nil})

(defn get-user
  "Returns the stored user."
  {:added "4.1.4"}
  [client]
  {:data {:user (:user (common/raw-state client))}
   :error nil})

(defn auth-result
  [client data]
  (let [session (token-response->session data)
        user (or (get data "user")
                 (get session "user")
                 data)
        out {:data {:session session
                    :user user}
             :error nil}]
    (when session
      (set-session! client session user))
    out))

(defn api-signup
  "Signs up a user through the auth API."
  {:added "4.1.4"}
  [body & [opts]]
  (common/api-call (merge opts
                          {:route "/auth/v1/signup"})
                   body))

(defn api-signin
  "Signs in a user through the auth API."
  {:added "4.1.4"}
  [body & [opts]]
  (common/api-call (merge opts
                          {:route "/auth/v1/token?grant_type=password"})
                   body))

(defn api-impersonate
  "Impersonates a user through the auth API."
  {:added "4.1.4"}
  [uid & [opts]]
  (common/api-call (merge opts
                          {:route "/auth/v1/token?grant_type=impersonate"
                           :type :service})
                   {"user_id" uid}))

(defn sign-up
  "Signs up a user and stores returned auth state when present."
  {:added "4.1.4"}
  [client body & [opts]]
  (let [response (api-signup body (merge opts {:client client}))]
    (if-let [data (:body response)]
      (update response :body #(auth-result client %))
      response)))

(defn sign-in
  "Signs in a user and stores the returned session."
  {:added "4.1.4"}
  [client body & [opts]]
  (let [response (api-signin body (merge opts {:client client}))]
    (if-let [data (:body response)]
      (update response :body #(auth-result client %))
      response)))

(def sign-in-with-password sign-in)

(defn refresh-session
  "Refreshes the stored session using the current refresh token."
  {:added "4.1.4"}
  [client & [opts]]
  (let [refresh-token (or (:refresh_token (common/raw-state client))
                          (:refresh_token opts))]
    (when-not refresh-token
      (throw (ex-info "Supabase refresh token not configured"
                      {:client client
                       :opts opts})))
    (let [response (common/api-call (merge opts
                                           {:client client
                                            :route "/auth/v1/token?grant_type=refresh_token"})
                                    {"refresh_token" refresh-token})]
      (update response :body #(auth-result client %)))))

(defn sign-out
  "Signs out the current auth session."
  {:added "4.1.4"}
  [client & [opts]]
  (let [opts (or opts {})
        scope (or (:scope opts) "global")
        response (common/api-call (merge opts
                                         {:client client
                                          :route (str "/auth/v1/logout?scope=" scope)})
                                  {})]
    (when (not= scope "others")
      (clear-session! client))
    response))

(def logout sign-out)
