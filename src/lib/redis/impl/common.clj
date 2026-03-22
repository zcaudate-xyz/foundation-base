(ns lib.redis.impl.common
  (:require [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.foundation :as f]
            [std.lib.future :as future]))

(defonce ^:dynamic *rt* (atom {}))

(defn opts:cache
  "creates a opts map for bulk operations"
  {:added "3.0"}
  ([opts]
   (select-keys opts [:namespace :runtime :format :deserialize :string :default])))

(defn make-key
  "creates a namespaced key
 
   (make-key nil :hello)
   => \"hello\""
  {:added "3.0"}
  ([ns key]
   (if (nil? ns)
     (f/strn key)
     (str (f/strn ns) ":" (f/strn key)))))

(defn unmake-key
  "removes the namespaced portion
 
   (unmake-key \"hello\" \"hello:there\")
   => \"there\""
  {:added "3.0"}
  ([ns key]
   (if (nil? ns)
     key
     (subs key (inc (count ns))))))


;;
;; return and process
;;


(defn return-default
  "return for default values"
  {:added "3.0"}
  ([val {:keys [async chain post]}]
   (cond-> (reduce f/call val post)
     async (-> (future/completed)
               (future/future:chain chain))
     (not async) (f/-> (reduce f/call % chain)))))

(defn return:format
  "constructs a return function"
  {:added "3.0"}
  ([format]
   (fn [data]
     (wire/coerce data format))))

(defn return:string
  "return function for string"
  {:added "3.0"}
  ([data _]
   (wire/coerce data :string)))

(defn return:raw
  "return function for string"
  {:added "3.0"}
  ([data _] data))

(defn return:keys
  "return function for keys"
  {:added "3.0"}
  ([data {:keys [namespace]}]
   (map (partial unmake-key namespace) data)))

(defn return:kv-hash
  "return function for string"
  {:added "3.0"}
  ([data {:keys [format]}]
   (mapcat (fn [[k v]]
             [(wire/coerce k :string)
              (wire/coerce v format)])
           (partition 2 data))))

(defn process:key
  "input function for key"
  {:added "3.0"}
  ([key {:keys [namespace]}]
   (make-key namespace key)))

(defn process:key-multi
  "input function for keys"
  {:added "3.0"}
  ([keys {:keys [namespace]}]
   (map (partial make-key namespace) keys)))

(defn process:unchanged
  "input function to unchange input"
  {:added "3.0"}
  ([data _]
   data))

(defn process:data
  "input function for data"
  {:added "3.0"}
  ([data {:keys [format]}]
   (if data
     (wire/as-input data format))))

(defn process:data-multi
  "input function for multi data"
  {:added "3.0"}
  ([data {:keys [format]}]
   (map #(if %
           (wire/as-input % format)) data)))

(defn process:kv-hash
  "input function for multi hash methods"
  {:added "3.0"}
  ([args {:keys [format]}]
   (->> (partition 2 args)
        (mapcat (fn [[k v]]
                  [k (if v
                       (wire/as-input v format))])))))

(defn process:kv
  "input function for multi key methods"
  {:added "3.0"}
  ([args {:keys [namespace format]}]
   (->> (partition 2 args)
        (mapcat (fn [[k v]]
                  [(make-key namespace k)
                   (if v (wire/as-input v format))])))))

(defn in:hash-args
  "create args from hash-map"
  {:added "3.0"}
  ([args]
   (if (= (count args) 1)
     (mapcat vec (first args))
     args)))
