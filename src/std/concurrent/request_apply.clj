(ns std.concurrent.request-apply
  (:require [std.concurrent.request :as req]
            [std.lib.apply :as apply]
            [std.lib.impl :refer [defimpl]]
            [std.protocol.apply :as protocol.apply]
            [std.protocol.request :as protocol.request]))

(defmulti req-call
  "extensible function for a request applicative"
  {:added "3.0"}
  (fn [{:keys [type]} _client _args _opts]
    type))

(defmethod req-call :single
  ([{:keys [function]} client args opts]
   (let [applicative (function args opts)]
     (req/req client applicative opts))))

(defmethod req-call :bulk
  ([{:keys [function options]} client args opts]
   (let [applicatives (function args opts)]
     (req/bulk:map client
                   #(req/req-fn %1 %2 (:bulk options))
                   applicatives opts))))

(defmethod req-call :transact
  ([{:keys [function options]} client args opts]
   (let [applicatives (function args opts)]
     (req/transact:map client
                       #(req/req-fn %1 %2 (:bulk options))
                       applicatives opts))))

(defmethod req-call :retry
  ([{:keys [function retry]} client args opts]
   (let [applicative   (function args opts)
         {:keys [pred]} retry
         retry-fn (fn [val]
                    (cond (instance? Throwable val)
                          (if (pred val)
                            ((:fn retry) client args opts)
                            (throw val))

                          :else val))]
     (->> (req/req:opts opts {:post [retry-fn]})
          (req/req client applicative)))))

(defn req-apply-in
  "runs a request applicative"
  {:added "3.0"}
  ([{:keys [options transform] :as app} rt args]
   (if (nil? rt)
     (req/req:in (req-call app rt args options))
     (let [opts (if (:out transform)
                  (req/req:opts options {:post [(fn [ret]
                                                  ((:out transform) app rt args ret))]})
                  options)]
       (req-call app rt args opts)))))

(defimpl ReqApplicative
  [type check client options transform]
  :prefix    "req-"
  :invoke    apply/invoke-as
  :protocols [std.protocol.apply/IApplicable
              :method {-transform-in  (:in transform)}
              :body {-transform-out return
                     -apply-default client}])

(defn req:applicative
  "constructs a request applicative"
  {:added "3.0"}
  ([m]
   (map->ReqApplicative m)))
