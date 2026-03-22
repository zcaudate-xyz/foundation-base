(ns std.object.framework.access
  (:require [std.lib.collection :as collection]
            [std.lib.invoke :as invoke]
            [std.lib.memoize :as memoize]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.protocol.object :as protocol.object])
  (:refer-clojure :exclude [get set get-in keys]))

(defn meta-clear
  "clears all meta-read and meta-write definitions for a class"
  {:added "3.0"}
  ([class]
   (invoke/multi:remove protocol.object/-meta-read class)
   (invoke/multi:remove protocol.object/-meta-write class)
   (memoize/memoize:remove read/meta-read-exact class)
   (memoize/memoize:remove write/meta-write-exact class)))

(defn get-with-keyword
  "access the fields of an object with keyword
 
   (get-with-keyword {:a 1} :a)
   => 1
 
   (get-with-keyword (test.Cat. \"spike\")
                     :name)
   => \"spike\""
  {:added "3.0"}
  ([obj k]
   (if (instance? java.util.Map obj)
     (clojure.core/get obj k)
     (if-let [getter (-> obj type read/meta-read :methods k)]
       ((:fn getter) obj)))))

(defn get-with-array
  "access the fields of an object with an array of keywords
 
   (get-with-array {:a 1} [:a])
   => {:a 1}
 
   (get-with-array (test.Cat. \"spike\")
                   [:name])
   => {:name \"spike\"}"
  {:added "3.0"}
  ([obj arr]
   (if (instance? java.util.Map obj)
     (select-keys obj arr)
     (let [getters (-> obj type read/meta-read :methods (select-keys arr))]
       (->> getters
            (collection/map-vals #((:fn %) obj)))))))

(defn get
  "accessor with either keyword or array lookup
 
   (access/get (test.Cat. \"spike\") :name)
   => \"spike\""
  {:added "3.0"}
  ([obj k]
   (cond (keyword? k)
         (get-with-keyword obj k)

         (.isArray ^Class (type obj))
         (nth obj k)

         (sequential? k)
         (get-with-array obj k))))

(defn get-in
  "accesses the nested object using specifiedb path
 
   (access/get-in (test.Cat. \"spike\") [:name])"
  {:added "3.0"}
  ([obj ks]
   (cond (empty? ks)
         obj

         :else
         (get-in (get obj (first ks)) (rest ks)))))

(defn keys
  "gets all keys of an object
 
   (access/keys (test.Cat. \"spike\"))
   => (contains [:name])"
  {:added "3.0"}
  ([obj]
   (-> obj type read/meta-read :methods clojure.core/keys)))

(defn set-with-keyword
  "sets the fields of an object with keyword
 
   (std.object.framework/map-like
    test.Cat
    {:tag \"cat\"
     :read :all
     :write :all})
 
   (-> (doto (test.Cat. \"spike\")
         (set-with-keyword :name \"fluffy\"))
       (access/get :name))
   => (any \"fluffy\"
           (throws))"
  {:added "3.0"}
  ([obj k v]
   (if-let [setter (-> obj type write/meta-write :methods k)]
     (try ((:fn setter) obj v)
          (catch ClassCastException c
            (println (str "Entry '" v "' not set for key " k ", require type:"))
            (throw (ex-info "Entry could not be set" {:object obj
                                                      :key k
                                                      :value v
                                                      :setter setter}))))
     (throw (ex-info "Key does not exist on object" {:object obj
                                                     :key k
                                                     :meta (-> obj type write/meta-write)})))))

(defn set
  "sets the fields of an object with a map
 
   (-> (doto (test.Cat. \"spike\")
         (access/set {:name \"fluffy\"}))
       (access/get :name))
   => (any \"fluffy\"
           (throws))"
  {:added "3.0"}
  ([obj m]
   (reduce-kv (fn [obj k v]
                (set-with-keyword obj k v)
                obj)
              obj
              m)
   obj)
  ([obj k v]
   (set-with-keyword obj k v)
   obj))

