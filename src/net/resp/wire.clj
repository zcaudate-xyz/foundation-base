(ns net.resp.wire
  (:require [std.json :as json]
            [std.lib.foundation]
            [std.lib.impl]
            [std.protocol.wire :as protocol.wire])
  (:refer-clojure :exclude [read]))

(std.lib.impl/build-impl {}
              protocol.wire/IWire)

(defn call
  "completes a request on the wire"
  {:added "3.0"}
  ([remote command]
   (write remote command)
   (read remote)))

(defn ^String as-input
  "creates an input from value
 
   (as-input {:a 1} :json)
   => \"{\\\"a\\\":1}\""
  {:added "3.0"}
  ([val format]
   (protocol.wire/-as-input val format)))

(defmethod protocol.wire/-as-input :default
  ([val _]
   (str val)))

(defmethod protocol.wire/-as-input :raw
  ([val _]
   val))

(defmethod protocol.wire/-as-input :edn
  ([val _]
   (pr-str val)))

(defmethod protocol.wire/-as-input :json
  ([val _]
   (json/write val)))

(defn serialize-bytes
  "serializes objects (data) to bytes
 
   (h/string (serialize-bytes \"HELLO\" :string))
   => \"HELLO\""
  {:added "3.0"}
  ([val format]
   (protocol.wire/-serialize-bytes val format)))

(defmethod protocol.wire/-serialize-bytes :default
  ([val format]
   (.getBytes (as-input val format))))

(defn deserialize-bytes
  "converts bytes back to data structure
 
   (deserialize-bytes (serialize-bytes {:a 1} :json)
                      :json)
   => {:a 1}"
  {:added "3.0"}
  ([bytes format]
   (protocol.wire/-deserialize-bytes bytes format)))

(defmethod protocol.wire/-deserialize-bytes :default
  ([bytes _]
   (std.lib.foundation/string bytes)))

(defmethod protocol.wire/-deserialize-bytes :bytes
  ([bytes _]
   bytes))

(defmethod protocol.wire/-deserialize-bytes :string
  ([bytes _]
   (std.lib.foundation/string bytes)))

(defmethod protocol.wire/-deserialize-bytes :json
  ([bytes _]
   (let [s (std.lib.foundation/string bytes)]
     (if (not-empty s)
       (json/read s json/+keyword-mapper+)))))

(defmethod protocol.wire/-deserialize-bytes :edn
  ([bytes _]
   (let [s (std.lib.foundation/string bytes)]
     (if (not-empty s)
       (read-string s)))))

(defn coerce-bytes
  "coerces bytes to data
 
   (coerce-bytes (.getBytes \"OK\") :json)
   => \"OK\"
 
   (coerce-bytes (serialize-bytes {:a 1} :json) :json)
   => {:a 1}"
  {:added "3.0"}
  ([^bytes bytes format]
   (cond (and (= 2 (count bytes))
              (= '(79 75)
                 [(aget  bytes 0)
                  (aget bytes 1)]))
         "OK"

         (and (= 6 (count bytes))
              (= '(81 85 69 85 69 68)
                 (map #(aget bytes %) (range 6))))
         "QUEUED"

         :else
         (deserialize-bytes bytes format))))

(defn coerce
  "coerces redis return to bytes"
  {:added "3.0"}
  ([output]
   (coerce output :bytes))
  ([output format]
   (cond (nil? output)
         nil

         (string? output)
         output

         (bytes? output)
         (coerce-bytes output format)

         (and (seqable? output)
              (not-empty output))
         (map #(coerce % format) (seq output))

         :else output)))
