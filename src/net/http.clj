^{:no-test true}
(ns net.http
  (:require [clojure.string]
            [net.http.client :as client]
            [net.http.websocket :as ws]
            [std.json :as json]
            [std.lib.collection :as collection]
            [std.lib.encode :as enc]
            [std.lib.foundation :as f]
            [std.lib.function :as fn]
            [std.object :as obj])
  (:import java.net.URLEncoder)
  (:refer-clojure :exclude [get]))

(f/intern-in client/request
             client/get
             client/post
             client/put
             client/patch
             client/head
             client/delete
             client/remote
             client/mock-endpoint
             client/stream-lines
             client/http-client
             client/http-request
             client/+http-response-handler+
	     
	     ws/websocket
	     ws/send!
             ws/close!
             ws/abort!)

(defn url-encode [^String s]
  (.replace (URLEncoder/encode s "UTF-8") "+" "%20"))

(defn decode-jwt
  [^String jwt]
  (->> (clojure.string/split
        jwt
        #"\.")
       (take 2)
       (mapv (fn [s]
               (json/read
                (String. (enc/from-base64 s)))))))

(defn encode-form-params
  [params]
  (->> params
       (keep (fn [[k v]]
               (cond (nil? v)
                     nil

                     (vector? v)
                     (->> (map-indexed
                           (fn [i x]
                             (str (url-encode (f/strn k)) "[" (+ i 1) "]" "=" (url-encode (f/strn x))))
                           v)
                          (interpose "&")
                          (apply str))
                     
                     :else
                     (str (url-encode (f/strn k)) "=" (url-encode (f/strn v))))))
       (interpose "&")
       (apply str)))

(defn event-stream
  "creates a data-stream for checking errors"
  {:added "4.0"}
  ([url & [opts]]
   (let [events (atom [])
         lines  (-> (client/get url
                                (collection/merge-nested
                                 opts
                                 {:headers {"Accept" "text/event-stream"}
                                  :as :lines}))
                    :body)
         foreach-fn (obj/query-instance lines ["forEach" :#])
         thread (future
                  (foreach-fn
                   lines
                   (fn/fn:consumer [e]
                                  (let [out (re-find #"data: (.*)" e)]
                                    (if out
                                      (swap! events conj (nth out 1)))))))]
     {:events events
      :lines lines
      :thread thread})))
