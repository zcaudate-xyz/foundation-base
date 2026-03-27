(ns rt.chromedriver.connection
  (:require [std.protocol.context :as protocol.context]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as default]
            [std.lib.encode :as encode]
            [std.lib :as h :refer [defimpl]]
            [std.json :as json]
            [std.string :as str]
            [net.http :as http])
  (:refer-clojure :exclude [send]))

(defrecord DevToolsConnection [websocket requests])

(defn gen-id
  "generates an id"
  {:added "4.0"}
  [requests]
  (let [id (rand-int Integer/MAX_VALUE)]
    (if (requests id)
      (recur requests)
      id)))

(defn send
  "sends a command to the process"
  {:added "4.0"}
  [{:keys [websocket requests session-id]} method & [params timeout opts]]
  (let [[id p] (h/swap-return! requests
                 (fn [m]
                   (let [id  (gen-id m)
                         pi  (h/incomplete)
                         p   (-> pi
                                 (h/future:timeout (or timeout 1000))
                                 (h/on:complete (fn [ret _]
                                                  (swap! requests dissoc id)
                                                  ret)))]
                     [[id p] (assoc m id pi)])))]
    (http/send! websocket
                (json/write
                 (cond-> (merge {:id id
                                 :method method
                                 :params (h/map-keys (comp str/camel-case h/strn)
                                                     (or params {}))}
                                opts)
                   session-id (assoc :sessionId session-id))))
    p))

(defn ws-url
  "gets the ws-url"
  {:added "4.0"}
  [{:keys [scheme host port]
    :or {host "localhost"
         port 9222
         scheme "http"}}]
  (let [body (-> (net.http/get (str scheme "://" host ":" port "/json/version"))
                 :body
                 json/read)]
    (or (get body "webSocketDebuggerUrl")
        (get-in (-> (net.http/get (str scheme "://" host ":" port "/json/list"))
                    :body
                    json/read)
                [0 "webSocketDebuggerUrl"]))))

(defn conn-process
  "processes the return call"
  {:added "4.0"}
  [requests data]
  (let [{:strs [id result]} (json/read data)
        p (get @requests id)
        processed (cond (map? result)
                        (or (if (get result "data")
                              (encode/from-base64 (get result "data")))
                            (get result "result")
                            result)

                        :else
                        result)]
    (when p
      (h/future:force p processed))
    [p processed]))

(defn conn-attach
  "creates a new target and attaches"
  {:added "4.0"}
  [conn & [curr-target]]
  (let [{:strs [targetId]}  (if curr-target
                              (get-in @(send conn "Target.getTargets")
                                      ["targetInfos" 0])
                              @(send conn "Target.createTarget"
                                     {:url "about:blank"}))
        {:strs [sessionId]} @(send conn "Target.attachToTarget"
                                   {:targetId targetId
                                    :flatten true})]
    (assoc conn
           :session-id sessionId
           :target-id targetId)))

(defn conn-create-raw
  "connection function (can error on OSX)"
  {:added "4.0"}
  [& [{:keys [host port attach] :as m
       :or {attach true}}]]
  (let [ws-url (ws-url m)
        requests     (atom {})
        intermediate (atom [])
        websocket  @(net.http/websocket
                     ws-url
                     {:on-message (fn [_ buf _]
                                    (let [out (str buf)]
                                      (try
                                        (conn-process requests
                                                      (str (str/join "" (h/swap-return! intermediate
                                                                          (fn [v]
                                                                            [v []])))
                                                           out))
                                        (catch Throwable _
                                          (swap! intermediate conj out)))))} )]
    (cond-> (->DevToolsConnection websocket requests)
      attach (conn-attach (= attach :current)))))

(defn conn-create
  "creates a devtools connection"
  {:added "4.0"}
  [& [{:keys [host port attach] :as m
       :or {attach true}}]]
  (loop [tries 5]
    (cond (pos? tries)
          (let [conn (try (conn-create-raw m)
                          (catch Throwable t))]
            (if (nil? conn)
              (do (Thread/sleep 100)
                (recur (dec tries)))
              conn))

          :else
          (h/error "Exhausted attempts."))))

(defn conn-close
  "closes the target and disconnects the devtools connection"
  {:added "4.0"}
  [{:keys [websocket target-id attach] :as conn
    :or {attach true}}]
  (when websocket
    @(send conn "Target.detachFromTarget"
           {})
    (when (true? attach)
      @(send conn "Target.closeTarget"
             {:targetId target-id}))
    (net.http/close! websocket))
  (dissoc conn :websocket))
