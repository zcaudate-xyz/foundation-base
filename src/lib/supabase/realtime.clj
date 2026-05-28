(ns lib.supabase.realtime
  (:require [clojure.string :as str]
            [lib.supabase.common :as common]
            [net.http.websocket :as ws]
            [std.json :as json]))

(defn trim-trailing-slash
  "Trims a trailing slash."
  {:added "4.1.4"}
  [s]
  (if (and (string? s)
           (str/ends-with? s "/"))
    (subs s 0 (dec (count s)))
    s))

(defn derive-websocket-url
  "Derives the Supabase realtime websocket URL."
  {:added "4.1.4"}
  [base-url]
  (when (string? base-url)
    (let [base-url (trim-trailing-slash base-url)
          base-url (if (str/ends-with? base-url "/rest/v1")
                     (subs base-url 0 (- (count base-url) 8))
                     base-url)
          base-url (cond (str/starts-with? base-url "https://")
                         (str "wss://" (subs base-url 8))

                         (str/starts-with? base-url "http://")
                         (str "ws://" (subs base-url 7))

                         :else
                         base-url)]
      (str base-url "/realtime/v1/websocket"))))

(defn prepare-connect-url
  "Builds the websocket connect URL."
  {:added "4.1.4"}
  [client & [opts]]
  (let [opts (or opts {})
        api-key (or (:api_key client) (:api_key opts))
        params (merge {"vsn" "1.0.0"}
                      (:params client)
                      (:params opts)
                      (when api-key {"apikey" api-key}))
        base-url (or (:websocket_url client)
                     (:websocket_url opts)
                     (derive-websocket-url (:base_url client)))
        query (str/join "&"
                        (map (fn [[k v]]
                               (str (java.net.URLEncoder/encode (name k) "UTF-8")
                                    "="
                                    (java.net.URLEncoder/encode (str v) "UTF-8")))
                             params))]
    (str base-url
         (when (seq query)
           (str "?" query)))))

(defn connected?
  "Checks whether a websocket is currently tracked on the client."
  {:added "4.1.4"}
  [client]
  (true? (:socket_open? (common/raw-state client))))

(defn append-message!
  [client data last?]
  (let [message (str data)
        frame-text (:message_buffer
                    (common/swap-state! client update :message_buffer str message))]
    (when last?
      (common/swap-state! client assoc :message_buffer "")
      (json/read frame-text))))

(defn dispatch-frame!
  [client frame]
  (let [topic (get frame "topic")
        event (get frame "event")
        payload (get frame "payload")
        subscriptions (vals (:subscriptions (common/raw-state client)))]
    (doseq [{:keys [topic-match kind event-name handler]} subscriptions
            :when (= topic-match topic)]
      (case kind
        :broadcast
        (let [payload-event (or (get payload "event")
                                (get payload :event))
              payload-data (or (get payload "payload")
                               (get payload :payload)
                               payload)]
          (when (or (nil? event-name)
                    (= event-name payload-event))
            (handler {:frame frame
                      :payload payload-data
                      :event payload-event})))

        :postgres_changes
        (when (= event "postgres_changes")
          (handler {:frame frame
                    :payload payload}))

        nil))))

(defn connect
  "Connects the client to Supabase realtime."
  {:added "4.1.4"}
  [client & [opts]]
  (or (:socket (common/raw-state client))
      (let [socket (.join
                    (ws/websocket
                     (prepare-connect-url client opts)
                     {:on-open (fn [socket]
                                 (common/swap-state! client assoc
                                                     :socket socket
                                                     :socket_open? true))
                      :on-message (fn [_ data last?]
                                    (when-let [frame (append-message! client data last?)]
                                      (dispatch-frame! client frame)))
                      :on-close (fn [_ _ _]
                                  (common/swap-state! client assoc
                                                      :socket nil
                                                      :socket_open? false))
                      :on-error (fn [_ err]
                                  (common/swap-state! client assoc :socket_error err))}))]
        (common/swap-state! client assoc :socket socket :socket_open? true)
        socket)))

(defn disconnect
  "Disconnects the realtime socket."
  {:added "4.1.4"}
  [client]
  (when-let [socket (:socket (common/raw-state client))]
    (.join (ws/close! socket))
    (common/swap-state! client assoc :socket nil :socket_open? false))
  client)

(defn resolve-topic
  "Resolves a realtime topic name."
  {:added "4.1.4"}
  [topic]
  (if (str/starts-with? topic "realtime:")
    topic
    (str "realtime:" topic)))

(defn join-payload
  "Builds the join payload."
  {:added "4.1.4"}
  [client & [opts]]
  (let [opts (or opts {})
        filters (:filters opts)
        payload {"config" {"broadcast" {"ack" false
                                        "self" false}
                           "presence" {"key" ""}}}]
    (cond-> payload
      (seq filters) (assoc-in ["config" "postgres_changes"] filters)
      (:auth_token (common/raw-state client)) (assoc "access_token"
                                                     (:auth_token (common/raw-state client)))
      (:join_payload opts) (merge (:join_payload opts)))))

(defn join-frame
  "Builds a phoenix join frame."
  {:added "4.1.4"}
  [client topic & [opts]]
  (let [ref (common/next-ref! client)]
    {"topic" (resolve-topic topic)
     "event" "phx_join"
     "payload" (join-payload client opts)
     "ref" ref
     "join_ref" ref}))

(defn leave-frame
  "Builds a phoenix leave frame."
  {:added "4.1.4"}
  [client topic]
  (let [ref (common/next-ref! client)]
    {"topic" (resolve-topic topic)
     "event" "phx_leave"
     "payload" {}
     "ref" ref
     "join_ref" ref}))

(defn send-frame!
  "Sends a raw websocket frame."
  {:added "4.1.4"}
  [client frame]
  (let [socket (connect client)]
    (.join (ws/send! socket (json/write frame)))
    frame))

(defn join-channel
  "Joins a realtime channel."
  {:added "4.1.4"}
  [client topic & [opts]]
  (let [frame (join-frame client topic opts)]
    (common/swap-state! client assoc-in [:channels (resolve-topic topic)] frame)
    (send-frame! client frame)))

(defn leave-channel
  "Leaves a realtime channel."
  {:added "4.1.4"}
  [client topic]
  (let [resolved (resolve-topic topic)
        frame (leave-frame client resolved)]
    (common/swap-state! client update :channels dissoc resolved)
    (send-frame! client frame)))

(defn subscribe-broadcast
  "Subscribes to a broadcast topic."
  {:added "4.1.4"}
  [client {:keys [topic event] :as opts} handler]
  (let [topic-match (resolve-topic topic)
        id (common/next-ref! client)]
    (connect client)
    (join-channel client topic opts)
    (common/swap-state! client assoc-in [:subscriptions id]
                        {:kind :broadcast
                         :topic-match topic-match
                         :event-name event
                         :handler handler})
    id))

(defn subscribe-postgres-changes
  "Subscribes to a postgres_changes channel."
  {:added "4.1.4"}
  [client {:keys [topic schema_name table_name event filter] :as opts} handler]
  (let [topic (or topic (str (or schema_name "public") ":" table_name))
        id (common/next-ref! client)
        join-opts (assoc opts
                         :filters [{"event" (or event "*")
                                    "schema" (or schema_name "public")
                                    "table" table_name
                                    "filter" filter}])]
    (connect client)
    (join-channel client topic join-opts)
    (common/swap-state! client assoc-in [:subscriptions id]
                        {:kind :postgres_changes
                         :topic-match (resolve-topic topic)
                         :handler handler})
    id))

(defn unsubscribe
  "Removes a registered subscription."
  {:added "4.1.4"}
  [client subscription-id]
  (common/swap-state! client update :subscriptions dissoc subscription-id)
  subscription-id)

(defn broadcast!
  "Broadcasts a payload on a topic."
  {:added "4.1.4"}
  [client {:keys [topic event payload]}]
  (send-frame! client
               {"topic" (resolve-topic topic)
                "event" "broadcast"
                "payload" {"event" event
                           "payload" payload}
                "ref" (common/next-ref! client)}))
