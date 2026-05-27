(ns lib.supabase.common
  (:require [clojure.string :as str]
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
  (join-url (:base_url client) (str "/auth/v1" path)))

(defn rest-url
  "Returns a PostgREST URL."
  {:added "4.1.4"}
  [client path]
  (join-url (:base_url client) (str "/rest/v1" path)))

(defn admin-url
  "Returns an auth admin URL."
  {:added "4.1.4"}
  [client path]
  (join-url (:base_url client) (str "/auth/v1/admin" path)))

(defn resolve-host
  [client {:keys [host]}]
  (or host
      (:base_url client)
      (System/getenv "DEFAULT_SUPABASE_API_ENDPOINT")))

(defn resolve-key
  [client {:keys [key type]}]
  (or key
      (:api_key client)
      (case type
        :anon (System/getenv "DEFAULT_SUPABASE_API_KEY_ANON")
        :service (System/getenv "DEFAULT_SUPABASE_API_KEY_SERVICE")
        :public "")))

(defn resolve-auth
  [client {:keys [auth]} key]
  (or auth
      (when (client? client)
        (:auth_token (raw-state client)))
      key))

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

(defn api-call
  "Calls a Supabase HTTP API route."
  {:added "4.1.4"}
  ([opts body]
   (let [{:keys [client
                 route
                 method
                 type
                 headers]
          :or {method :post
               type :anon}} opts
         client (or client {})
         host (resolve-host client opts)
         key (resolve-key client opts)
         auth (resolve-auth client opts key)
         headers-default (case type
                           :public {"Content-Type" "application/json"}
                           {"apikey" key
                            "Authorization" (str "Bearer " auth)
                            "Content-Type" "application/json"})
         headers (merge (:headers client)
                        headers-default
                        headers)
         headers (if (and (= method :get)
                          (get headers "Content-Profile")
                          (not (get headers "Accept-Profile")))
                   (assoc headers
                          "Accept-Profile"
                          (get headers "Content-Profile"))
                   headers)
         request {:host host
                  :route route
                  :method method
                  :type type}]
     (when (and (not= type :public)
                (not (get headers "apikey")))
       (f/error "Supabase API key not configured"
                (assoc request
                       :env (case type
                              :anon "DEFAULT_SUPABASE_API_KEY_ANON"
                              :service "DEFAULT_SUPABASE_API_KEY_SERVICE"
                              nil))))
     (let [response (-> ((request-fn method)
                         (str host route)
                         {:headers headers
                          :body    (json/write (or body {}))})
                        (update :body decode-body)
                        (select-keys [:status :body]))]
       (response-error request response)
       response))))

(defn next-ref!
  "Returns the next websocket frame ref for the client."
  {:added "4.1.4"}
  [client]
  (:ref_counter
   (swap-state! client update :ref_counter (fnil inc 0))))
