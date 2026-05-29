(ns lib.supabase.common
  (:require [clojure.string :as str]
            [lib.supabase.route :as route]
            [net.http :as http]
            [std.json :as json]
            [std.lib.foundation :as f]))

(def ^:private +default-state+
  {:session nil
   :user nil
   :auth_token nil
   :refresh_token nil
   :socket nil
   :socket_open? false
   :message_buffer ""
   :channels {}
   :subscriptions {}
   :ref_counter 0})

(defn client?
  "Checks if the value is a lib.supabase client."
  {:added "4.1.4"}
  [obj]
  (and (map? obj)
       (string? (:base_url obj))
       (contains? obj :state)
       (instance? clojure.lang.IDeref (:state obj))))

(defn create-client
  "Creates a native Clojure Supabase client descriptor."
  {:added "4.1.4"}
  [base_url api_key & [opts]]
  (let [opts (or opts {})
        state (or (:state opts) (atom +default-state+))]
    (-> opts
        (assoc :base_url base_url
               :api_key api_key
               :state state)
        (update :headers #(or % {}))
        (update :schema_name #(or % "public")))))

(defn state-atom
  "Returns the client's state atom."
  {:added "4.1.4"}
  [client]
  (or (:state client)
      (f/error "Supabase client missing state atom" {:client client})))

(defn raw-state
  "Returns the client's current mutable state."
  {:added "4.1.4"}
  [client]
  @(state-atom client))

(defn swap-state!
  "Swaps the client's internal state atom."
  {:added "4.1.4"}
  [client f & args]
  (apply swap! (state-atom client) f args))

(defn decode-body
  "Decodes a JSON response body when present."
  {:added "4.1.4"}
  [body]
  (cond (nil? body) nil
        (string? body) (if (str/blank? body)
                         nil
                         (json/read body))
        :else body))

(defn join-url
  "Joins a base URL and path."
  {:added "4.1.4"}
  [base path]
  (cond (or (nil? base)
            (not (string? base)))
        path

        (and (str/ends-with? base "/")
             (str/starts-with? path "/"))
        (str base (subs path 1))

        (and (not (str/ends-with? base "/"))
             (not (str/starts-with? path "/")))
        (str base "/" path)

        :else
        (str base path)))

(defn auth-url
  "Returns an auth API URL."
  {:added "4.1.4"}
  [client path]
  (join-url (:base_url client)
            (str (route/root-path :auth) path)))

(defn rest-url
  "Returns a PostgREST URL."
  {:added "4.1.4"}
  [client path]
  (join-url (:base_url client)
            (str (route/root-path :rest) path)))

(defn admin-url
  "Returns an auth admin URL."
  {:added "4.1.4"}
  [client path]
  (join-url (:base_url client)
            (str (route/root-path :admin) path)))

(defn host-url?
  [host]
  (and (string? host)
       (re-find #"^https?://" host)))

(defn resolve-host
  [client {:keys [host]}]
  (or host
      (:base_url client)
      (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")))

(defn resolve-server-url
  "Resolves the effective Supabase server URL."
  {:added "4.1.4"}
  [client {:keys [server-url port secured] :as opts}]
  (or server-url
      (let [host (resolve-host client opts)]
        (cond
          (nil? host)
          nil

          (host-url? host)
          host

          :else
          (str (if (false? secured) "http" "https")
               "://"
               host
               (when port
                 (str ":" port)))))))

(defn resolve-key
  [client {:keys [key type]}]
  (or key
      (:api_key client)
      (case type
        :anon (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
        :service (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE")
        :public "")))

(defn resolve-token
  [client {:keys [token auth]} key]
  (or token
      auth
      (when (client? client)
        (:auth_token (raw-state client)))
      key))

(defn resolve-auth
  [client {:keys [auth]} key]
  (resolve-token client {:auth auth} key))

(defn resolve-route
  [group route]
  (if group
    (str (route/root-path group) route)
    route))

(defn request-headers
  [client {:keys [type headers method content-type accepts]} key token]
  (let [headers-default (case type
                         :public (cond-> {"Content-Type" (or content-type "application/json")}
                                   (seq accepts) (assoc "Accept" accepts))
                         (cond-> {"apikey" key
                                  "Authorization" (str "Bearer " token)
                                  "Content-Type" (or content-type "application/json")}
                           (seq accepts) (assoc "Accept" accepts)))
        headers (merge (:headers client)
                      headers-default
                      headers)]
    (if (and (= method :get)
            (get headers "Content-Profile")
            (not (get headers "Accept-Profile")))
      (assoc headers
            "Accept-Profile"
            (get headers "Content-Profile"))
      headers)))

(defn request-fn
  [method]
  (case method
    :delete http/delete
    :get    http/get
    :head   http/head
    :patch  http/patch
    :post   http/post
    :put    http/put
    (f/error "Unsupported Supabase API method" {:method method})))

(defn response-error
  "Throws a normalized error when the API request fails."
  {:added "4.1.4"}
  [request response]
  (when (and (:status response)
             (<= 400 (:status response)))
    (f/error "Supabase API request failed"
             (merge request response))))

(defn supabase-call
 "Calls a Supabase HTTP route using explicit server and request parameters."
 {:added "4.1.4"}
 ([opts]
  (supabase-call opts (:body opts)))
 ([opts body]
  (let [{:keys [client
                route
                group
                method
                type
                query-params
                content-type
                accepts]
         :or {method :post
              type :anon}} opts
        client (or client {})
        server-url (resolve-server-url client opts)
        key (resolve-key client opts)
        token (resolve-token client opts key)
        headers (request-headers client
                                 (assoc opts
                                        :method method
                                        :type type
                                        :content-type content-type
                                        :accepts accepts)
                                 key
                                 token)
        route (resolve-route group route)
        request {:server-url server-url
                 :route route
                 :method method
                 :type type
                 :query-params query-params}]
    (when (and (not= type :public)
               (not (get headers "apikey")))
      (f/error "Supabase API key not configured"
               (assoc request
                      :env (case type
                             :anon "DEFAULT_SUPABASE_API_KEY_ANON"
                             :service "DEFAULT_SUPABASE_API_KEY_SERVICE"
                             nil))))
    (let [response (-> (http/call-api server-url
                                      route
                                      method
                                      {:query-params query-params
                                       :header-params headers
                                       :body body
                                       :content-type (or content-type "application/json")
                                       :accepts accepts})
                       (update :body decode-body)
                       (select-keys [:status :body]))]
      (response-error request response)
      response))))

(defn api-call
 "Compatibility wrapper over `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (supabase-call opts))
 ([opts body]
  (supabase-call opts body)))

(defn auth-call
 "Calls an auth endpoint through `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (auth-call opts (:body opts)))
 ([opts body]
  (supabase-call (assoc opts :group :auth) body)))

(defn admin-call
 "Calls an admin endpoint through `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (admin-call opts (:body opts)))
 ([opts body]
  (supabase-call (assoc opts :group :admin) body)))

(defn sqlrest-call
 "Calls a PostgREST endpoint through `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (sqlrest-call opts (:body opts)))
 ([opts body]
  (supabase-call (assoc opts :group :rest) body)))

(defn rpc-call
 "Calls an RPC endpoint through `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (rpc-call opts (:body opts)))
 ([{:keys [rpc fn-name route] :as opts} body]
  (let [route (or route
                  (when rpc
                    (route/rest-route :rpc rpc))
                  (when fn-name
                    (route/rest-route :rpc fn-name)))]
    (supabase-call (assoc opts
                          :group :rest
                          :method (or (:method opts) :post)
                          :route route)
                   body))))

(defn realtime-call
 "Calls a realtime HTTP endpoint through `supabase-call`."
 {:added "4.1.4"}
 ([opts]
  (realtime-call opts (:body opts)))
 ([opts body]
  (supabase-call (assoc opts :group :realtime) body)))

(defn next-ref!
  "Returns the next websocket frame ref for the client."
  {:added "4.1.4"}
  [client]
  (:ref_counter
   (swap-state! client update :ref_counter (fnil inc 0))))
