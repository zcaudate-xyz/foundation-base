(ns lib.rabbitmq.request
  (:require [net.http :as http]
            [std.json :as json]
            [std.lib.collection :as collection]
            [std.lib.encode :as encode]
            [std.string.case :as case]))

(def ^:dynamic *default-request-options*
  {:headers {"Accept" "application/json"
             "Content-Type" "application/json"}})

(defn create-url
  "Creates the RabbitMQ management url."
  {:added "4.1.4"}
  [{:keys [protocol host management-port]} suburl]
  (str protocol "://" host ":" management-port "/api/" suburl))

(defn wrap-parse-json
  "Parses JSON management responses into keyword-keyed Clojure data."
  {:added "4.1.4"}
  [f]
  (fn [request]
    (let [res (f request)
          status (:status res)
          body (:body res)]
      (cond (<= 200 status 299)
            (if (or (nil? body) (= "" body))
              (if (= status 204) true res)
              (json/read body json/+keyword-mapper+))

            (<= 400 status)
            (throw (ex-info "HTTP Error" res))

            :else
            res))))

(defn update-nested-keys
  "Recursively updates map keys."
  {:added "4.1.4"}
  [m func]
  (cond (map? m)
        (reduce-kv (fn [out k v]
                     (assoc out
                            (func k)
                            (if (coll? v)
                              (update-nested-keys v func)
                              v)))
                   {}
                   m)

        (coll? m)
        (into (empty m) (map #(update-nested-keys % func) m))

        :else
        m))

(defn wrap-generate-json
  "Encodes request bodies as JSON using snake_case keys."
  {:added "4.1.4"}
  [f]
  (fn [request]
    (let [body (:body request)
          body (cond (nil? body) nil
                     (string? body) body
                     :else (-> body
                               (update-nested-keys (comp case/snake-case name))
                               (json/write)))
          request (cond-> request
                    body (assoc :body body))]
      (f request))))

(defn basic-auth-header
  "Creates the HTTP Basic auth header for RabbitMQ management requests."
  {:added "4.1.4"}
  [{:keys [username password]}]
  (str "Basic "
       (encode/to-base64 (.getBytes (str username ":" password) "UTF-8"))))

(defn request
  "Performs a RabbitMQ management API request."
  {:added "4.1.4"}
  ([rabbit suburl]
   (request rabbit suburl :get))
  ([rabbit suburl method]
   (request rabbit suburl method {}))
  ([rabbit suburl method opts]
   (let [req (collection/merge-nested
              *default-request-options*
              opts
              {:method method
               :headers {"Authorization" (basic-auth-header rabbit)}})
         req (assoc req :uri (create-url rabbit suburl))]
     ((-> http/request
          wrap-generate-json
          wrap-parse-json)
      req))))
