(ns code.ai.jules
  (:require [net.http :as http]
            [net.http.client :as client]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]))

(def +base-url+ "https://jules.googleapis.com")

(defn- api-request
  ([method path {:keys [api-key body query] :as opts}]
   (let [url (str +base-url+ path (if query (str "?" (http/encode-form-params query)) ""))
         headers (cond-> {"X-Goog-Api-Key" api-key}
                   body (assoc "Content-Type" "application/json"))
         req-opts {:headers headers
                   :method method
                   :body (when body (json/write body))}]
     (-> (client/request url req-opts)
         (client/endpoint-data)))))

(defn get-sources
  "Lists all sources connected to Jules.

   (get-sources {:api-key \"YOUR_API_KEY\"})
   => {:sources [{:name \"sources/github/...\" ...}]}"
  ([{:keys [api-key page-size page-token] :as opts}]
   (api-request :get "/v1alpha/sources"
                (assoc opts :query (cond-> {}
                                     page-size (assoc :pageSize page-size)
                                     page-token (assoc :pageToken page-token))))))

(defn get-source
  "Gets a single source.

   (get-source \"sources/github/user/repo\" {:api-key \"...\"})"
  ([name {:keys [api-key] :as opts}]
   (api-request :get (str "/v1alpha/" name) opts)))

(defn create-session
  "Creates a new session.

   (create-session {:prompt \"Create a boba app!\"
                    :sourceContext {...}}
                   {:api-key \"...\"})"
  ([body {:keys [api-key] :as opts}]
   (api-request :post "/v1alpha/sessions"
                (assoc opts :body body))))

(defn get-session
  "Gets a single session.

   (get-session \"sessions/123...\" {:api-key \"...\"})"
  ([name {:keys [api-key] :as opts}]
   (api-request :get (str "/v1alpha/" name) opts)))

(defn list-sessions
  "Lists all sessions.

   (list-sessions {:api-key \"...\"})"
  ([{:keys [api-key page-size page-token filter] :as opts}]
   (api-request :get "/v1alpha/sessions"
                (assoc opts :query (cond-> {}
                                     page-size (assoc :pageSize page-size)
                                     page-token (assoc :pageToken page-token)
                                     filter (assoc :filter filter))))))

(defn send-message
  "Sends a message from the user to a session.

   (send-message \"sessions/123...\"
                 {:prompt \"Make it corgi themed\"}
                 {:api-key \"...\"})"
  ([session body {:keys [api-key] :as opts}]
   (api-request :post (str "/v1alpha/" session ":sendMessage")
                (assoc opts :body body))))

(defn approve-plan
  "Approves a plan in a session.

   (approve-plan \"sessions/123...\" {:api-key \"...\"})"
  ([session {:keys [api-key] :as opts}]
   (api-request :post (str "/v1alpha/" session ":approvePlan")
                (assoc opts :body {}))))

(defn list-activities
  "Lists activities for a session.

   (list-activities \"sessions/123...\" {:api-key \"...\"})"
  ([session {:keys [api-key page-size page-token] :as opts}]
   (api-request :get (str "/v1alpha/" session "/activities")
                (assoc opts :query (cond-> {}
                                     page-size (assoc :pageSize page-size)
                                     page-token (assoc :pageToken page-token))))))

(defn get-activity
  "Gets a single activity.

   (get-activity \"sessions/123.../activities/456...\" {:api-key \"...\"})"
  ([name {:keys [api-key] :as opts}]
   (api-request :get (str "/v1alpha/" name) opts)))
