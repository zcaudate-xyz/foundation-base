(ns lib.supabase.auth
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]))

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
      ([body]
       (api-signup body {}))
      ([body opts]
       (common/auth-call (route/route-request :auth/signup opts) body)))

(defn api-signin
      "Signs in a user through the auth API."
      ([body]
       (api-signin body {}))
      ([body opts]
       (common/auth-call (route/route-request :auth/signin-password opts) body)))

(defn api-impersonate
      "Impersonates a user through the auth API."
      ([uid]
       (api-impersonate uid {}))
      ([uid opts]
       (common/auth-call (route/route-request :auth/impersonate opts) {"user_id" uid})))

(defn sign-up
      "Signs up a user and stores returned auth state when present."
      ([client body]
       (sign-up client body {}))
      ([client body opts]
       (let [response (api-signup body (merge opts {:client client}))] (if-let [data (:body response)] (update response :body (fn* [p1__6400#] (auth-result client p1__6400#))) response))))

(defn sign-in
      "Signs in a user and stores the returned session."
      ([client body]
       (sign-in client body {}))
      ([client body opts]
       (let [response (api-signin body (merge opts {:client client}))] (if-let [data (:body response)] (update response :body (fn* [p1__6401#] (auth-result client p1__6401#))) response))))

(defn refresh-session
      "Refreshes the stored session using the current refresh token."
      ([client]
       (refresh-session client {}))
      ([client opts]
       (let [refresh-token (or (:refresh_token (common/raw-state client)) (:refresh_token opts))] (when-not refresh-token (throw (ex-info "Supabase refresh token not configured" {:client client :opts opts}))) (let [response (common/auth-call (route/route-request :auth/refresh-session (merge opts {:client client})) {"refresh_token" refresh-token})] (update response :body (fn* [p1__6402#] (auth-result client p1__6402#)))))))

(defn sign-out
      "Signs out the current auth session."
      ([client]
       (sign-out client {}))
      ([client opts]
       (let [opts (or opts {}) scope (or (:scope opts) "global") response (common/auth-call (route/route-request :auth/sign-out (merge opts {:client client}) scope) {})] (when (not= scope "others") (clear-session! client)) response)))

(def sign-in-with-password sign-in)

(def logout sign-out)
