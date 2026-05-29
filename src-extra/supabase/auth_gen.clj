(ns lib.supabase.auth-gen
  (:require [clojure.string :as string]
            [lib.supabase.template :as template]
            [std.block.template :as gen]))

(def +output-path+
  "src/lib/supabase/auth.clj")

(def ^:private HEADER_TEMPLATE
  "(ns ~ns-sym
  (:require [lib.supabase.common :as common]
            [lib.supabase.route :as route]))")

(def ^:private +header-template+
  (gen/get-template HEADER_TEMPLATE
                    (fn [{:keys [ns-sym]}]
                      {'ns-sym ns-sym})))

(def ^:private +helper-block+
  (string/join
   "\n"
   ["(defn has-session?"
    "  \"Checks whether the payload looks like a Supabase token response.\""
    "  {:added \"4.1.4\"}"
    "  [data]"
    "  (boolean"
    "   (and (get data \"access_token\")"
    "        (get data \"refresh_token\")"
    "        (get data \"expires_in\"))))"
    ""
    "(defn token-response->session"
    "  \"Normalizes a token response into a session map.\""
    "  {:added \"4.1.4\"}"
    "  [data]"
    "  (when (has-session? data)"
    "    (cond-> (assoc data"
    "                   \"expires_at\""
    "                   (or (get data \"expires_at\")"
    "                       (+ (quot (System/currentTimeMillis) 1000)"
    "                          (long (get data \"expires_in\")))))"
    "      true identity)))"
    ""
    "(defn set-session!"
    "  \"Stores a Supabase session and user on the client.\""
    "  {:added \"4.1.4\"}"
    "  ([client session]"
    "   (set-session! client session (or (get session \"user\") nil)))"
    "  ([client session user]"
    "   (common/swap-state! client assoc"
    "                       :session session"
    "                       :user user"
    "                       :auth_token (get session \"access_token\")"
    "                       :refresh_token (get session \"refresh_token\"))"
    "   client))"
    ""
    "(defn clear-session!"
    "  \"Clears the stored auth state.\""
    "  {:added \"4.1.4\"}"
    "  [client]"
    "  (common/swap-state! client assoc"
    "                      :session nil"
    "                      :user nil"
    "                      :auth_token nil"
    "                      :refresh_token nil)"
    "  client)"
    ""
    "(defn get-session"
    "  \"Returns the stored session.\""
    "  {:added \"4.1.4\"}"
    "  [client]"
    "  {:data {:session (:session (common/raw-state client))}"
    "   :error nil})"
    ""
    "(defn get-user"
    "  \"Returns the stored user.\""
    "  {:added \"4.1.4\"}"
    "  [client]"
    "  {:data {:user (:user (common/raw-state client))}"
    "   :error nil})"
    ""
    "(defn auth-result"
    "  [client data]"
    "  (let [session (token-response->session data)"
    "        user (or (get data \"user\")"
    "                 (get session \"user\")"
    "                 data)"
    "        out {:data {:session session"
    "                    :user user}"
    "             :error nil}]"
    "    (when session"
    "      (set-session! client session user))"
    "    out))"]))

(def +api-entries+
  [{'fsym 'api-signup
    'doc "Signs up a user through the auth API."
    'base-args '[body]
    'opts-args '[body opts]
    'body-form '(common/auth-call (route/route-request :auth/signup opts)
                                  body)}
   {'fsym 'api-signin
    'doc "Signs in a user through the auth API."
    'base-args '[body]
    'opts-args '[body opts]
    'body-form '(common/auth-call (route/route-request :auth/signin-password opts)
                                  body)}
   {'fsym 'api-impersonate
    'doc "Impersonates a user through the auth API."
    'base-args '[uid]
    'opts-args '[uid opts]
    'body-form '(common/auth-call (route/route-request :auth/impersonate opts)
                                  {"user_id" uid})}
   {'fsym 'sign-up
    'doc "Signs up a user and stores returned auth state when present."
    'base-args '[client body]
    'opts-args '[client body opts]
    'body-form '(let [response (api-signup body (merge opts {:client client}))]
                  (if-let [data (:body response)]
                    (update response :body #(auth-result client %))
                    response))}
   {'fsym 'sign-in
    'doc "Signs in a user and stores the returned session."
    'base-args '[client body]
    'opts-args '[client body opts]
    'body-form '(let [response (api-signin body (merge opts {:client client}))]
                  (if-let [data (:body response)]
                    (update response :body #(auth-result client %))
                    response))}
   {'fsym 'refresh-session
    'doc "Refreshes the stored session using the current refresh token."
    'base-args '[client]
    'opts-args '[client opts]
    'body-form '(let [refresh-token (or (:refresh_token (common/raw-state client))
                                        (:refresh_token opts))]
                  (when-not refresh-token
                    (throw (ex-info "Supabase refresh token not configured"
                                    {:client client
                                     :opts opts})))
                  (let [response (common/auth-call (route/route-request :auth/refresh-session
                                                                     (merge opts {:client client}))
                                                   {"refresh_token" refresh-token})]
                    (update response :body #(auth-result client %))))}
   {'fsym 'sign-out
    'doc "Signs out the current auth session."
    'base-args '[client]
    'opts-args '[client opts]
    'body-form '(let [opts (or opts {})
                      scope (or (:scope opts) "global")
                      response (common/auth-call (route/route-request :auth/sign-out
                                                                   (merge opts {:client client})
                                                                   scope)
                                                 {})]
                  (when (not= scope "others")
                    (clear-session! client))
                  response)}])

(def ^:private +tail-block+
  (string/join
   "\n"
   ["(def sign-in-with-password sign-in)"
    ""
    "(def logout sign-out)"]))

(defn render-entry
  [entry]
  (template/supabase-defn-string entry))

(defn render-auth-file
  "Renders the `lib.supabase.auth` namespace as plain `defn` forms."
  {:added "4.1.4"}
  ([] (render-auth-file 'lib.supabase.auth))
  ([ns-sym]
   (str (gen/fill-template +header-template+ {:ns-sym ns-sym})
        "\n\n"
        +helper-block+
        "\n\n"
        (string/join "\n\n" (map render-entry +api-entries+))
        "\n\n"
        +tail-block+
        "\n")))

(defn write-auth-file
  "Writes the generated `lib.supabase.auth` namespace to disk."
  {:added "4.1.4"}
  ([] (write-auth-file +output-path+ 'lib.supabase.auth))
  ([output-path ns-sym]
   (let [output (render-auth-file ns-sym)]
     (spit output-path output)
     output)))
