(ns std.contract.sketch
  (:require [malli.core :as mc]
            [malli.util :as mu]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.function :as fn]
            [std.lib.impl :as impl]
            [std.lib.os :as os])
  (:refer-clojure :exclude [remove]))

(defn optional-string
  "string for optional"
  {:added "3.0"}
  [m]
  (str (:v m) "@?"))

(impl/defimpl Optional [v]
  :string optional-string)

(defn maybe-string
  "string for maybe"
  {:added "3.0"}
  [m]
  (str "?@" (:v m)))

(impl/defimpl Maybe [v]
  :string maybe-string)

(defn as:optional
  "creates an optional"
  {:added "3.0"}
  [k]
  (->Optional k))

(defn optional?
  "checks if optional type"
  {:added "3.0"}
  [x]
  (instance? Optional x))

(defn as:maybe
  "creates a maybe"
  {:added "3.0"}
  [v]
  (->Maybe v))

(defn maybe?
  "checks if maybe type"
  {:added "3.0"}
  [x]
  (instance? Maybe x))

(declare func-string
         func-invoke)

(impl/defimpl Func [form pred]
  :type  deftype
  :string func-string
  :invoke func-invoke)

(defn func-string
  "string for func"
  {:added "3.0"}
  [^Func f]
  (str "#spec " (.form f)))

(defn func-invoke
  "invokes the func"
  {:added "3.0"}
  [^Func f data]
  ((.pred f) data))

(defn fn-sym
  "gets function symbol"
  {:added "3.0"}
  ([f]
   (f/-> (re-find #"#(function|object)\[(.*?)(--\d+)?\]"
                  (prn-str f))
         (nth 2)
         (if % (symbol %)))))

(defn func-form
  "constructs a func"
  {:added "3.0"}
  ([args]
   (let [args (cond (vector? args) args

                    (integer? args)
                    (vec (repeat args '_))

                    :else (throw (ex-info "Not supported" {:input args})))
         num (count args)
         form (list 'fn args)
         pred (fn [f]
                (if (os/native?)
                  true
                  (try (fn/arg-check f num) (catch Throwable t false))))]
     (Func. form pred))))

(defmacro func
  "macro for constructing a func"
  {:added "3.0"}
  ([args]
   `(func-form (quote ~args))))

(defn func?
  "checks if instance is a func"
  {:added "3.0"}
  [x]
  (instance? Func x))

(defn from-schema-map
  "sketch from malli's map syntax"
  {:added "3.0"}
  ([m]
   (let [{:keys [type properties children]} m]
     (case type
       :map  (with-meta
               (reduce (fn [acc [id {:keys [value properties]}]]
                         (assoc acc
                                (if (:optional properties)
                                  (as:optional id)
                                  id)
                                (from-schema-map value)))
                       {}
                       (:keys m))
               properties)
       :multi  (mc/from-ast m)
       :fn     (first children)
       :=      (:value m)
       :enum   (set (:values m))
       :maybe  (with-meta (as:maybe (from-schema-map (:child m)))
                 properties)
       :string  (filterv identity [:string properties])

       (if (keyword? type)
         (with-meta (apply vector type (map from-schema-map children)) properties)
         (if (f/iobj? type)
           (with-meta type properties)
           type))))))

(defn from-schema
  "sketch from schema"
  {:added "3.0"}
  [schema]
  (cond (map? schema) schema
        
        (vector? schema) (from-schema (mc/schema schema))

        (mc/schema? schema) (from-schema-map (mc/ast schema))

        :else
        (from-schema (mc/schema schema))))

(defmulti to-schema-extend
  "extending schema conversion"
  {:added "3.0"}
  (fn [t] (type t)))

(defmethod to-schema-extend :default
  [x]
  (mc/schema [:= x]))

(defn to-schema
  "converts object to schema"
  {:added "3.0"}
  ([x]
   (cond (mc/schema? x) x

         (func? x)
         (mc/schema [:fn x])

         (fn? x)
         (let [sym  (fn-sym x)]
           (if (.endsWith (str sym) "/fn")
             (mc/schema [:fn x])
             (try (mc/schema x)
                  (catch clojure.lang.ExceptionInfo e
                    (case (:type (ex-data e))
                      :malli.core/invalid-schema (mc/schema [:fn x])
                      (throw e))))))

         (list? x)
         (mc/schema (apply vector :enum x))
         
         (maybe? x)
         (mc/schema [:maybe (to-schema (:v x))])

         (set? x)
         (mc/schema (apply vector :enum (seq x)))

         (vector? x)
         (mc/schema x)

         (collection/hash-map? x)
         (mc/schema (apply vector
                           :map
                           (or (meta x) {})
                           (map (fn [[k v]]
                                  (if (optional? k)
                                    [(:v k) {:optional true} (to-schema v)]
                                    [k (to-schema v)]))
                                x)))

         :else (to-schema-extend x))))

(defn lax
  "relaxes a map (optional keys and maybe vals)"
  {:added "3.0"}
  ([schema]
   (let [m (from-schema schema)]
     (if (map? m)
       (lax schema (keys m))
       (f/error "Not Supported" {:input schema}))))
  ([schema ks]
   (let [schema (to-schema schema)
         vs     (mapv (fn [v]
                        (if (= (mc/type v) :maybe)
                          v
                          (mc/schema [:maybe v])))
                      (map (partial mu/get schema) ks))]
     (-> (reduce (fn [schema i]
                   (mu/assoc schema (nth ks i) (nth vs i)))
                 schema
                 (range (count ks)))
         (mu/optional-keys ks)))))

(defn norm
  "gets rid of optional keys"
  {:added "3.0"}
  ([schema]
   (let [m (from-schema schema)]
     (if (map? m)
       (norm schema (map (fn [k]
                           (if (optional? k)
                             (:v k)
                             k))
                         (keys m)))
       (f/error "Not Supported" {:input schema}))))
  ([schema ks]
   (let [schema (to-schema schema)]
     (mu/required-keys schema ks))))

(defn closed
  "closes the map"
  {:added "3.0"}
  ([schema]
   (let [schema (to-schema schema)]
     (mu/closed-schema schema))))

(defn opened
  "opens the map"
  {:added "3.0"}
  ([schema]
   (let [schema (to-schema schema)]
     (mu/open-schema schema))))

(defn tighten
  "tightens a map (no optionals or maybes)"
  {:added "3.0"}
  ([schema]
   (let [m (from-schema schema)]
     (if (map? m)
       (tighten schema
                (map (fn [k]
                       (if (optional? k)
                         (:v k)
                         k))
                     (keys m)))
       (f/error "Not Supported" {:input schema}))))
  ([schema ks]
   (let [schema (to-schema schema)
         vs     (mapv (fn [v]
                        (if (= (mc/type v) :maybe)
                          (first (mc/children v))
                          v))
                      (map (partial mu/get schema) ks))]
     (-> (reduce (fn [schema i]
                   (mu/assoc schema (nth ks i) (nth vs i)))
                 schema
                 (range (count ks)))
         (mu/required-keys ks)))))

(defn remove
  "removes a key from map"
  {:added "3.0"}
  ([schema ks]
   (let [schema (to-schema schema)]
     (reduce mu/dissoc schema ks))))

(comment

  (mc/type (mc/schema [:maybe [:= 1]]))
  (mc/schema [:fn (func [1 2 3])])

  (from-schema
   (-> [:map {:closed true}
        [:id  {:optional true} [:maybe string?]]
        [:tags [:set keyword?]]]
       (tighten-schema [:id])
       (lax-schema [:tags])))

  (mu/required-keys [:map {:closed true}
                     [:id  {:optional true} [:maybe string?]]
                     [:tags [:set keyword?]]])
  (mu/optional-keys [:map {:closed true}
                     [:id  {:optional true} [:maybe string?]]
                     [:tags [:set keyword?]]])

  (mc/schema (mc/schema [:map {:closed true}
                         [:id  [:maybe string?]]
                         [:tags [:set keyword?]]]))

  (mu/to-map-syntax
   (mc/type (mu/get

              :id))
   "oeu")

  (mu/to-map-syntax [:or pos?])
  {:type :map, :children [[:id nil {:type string?}] [:tags nil {:type :set, :children [{:type keyword?}]}]]}

  (defn to-schema
    [sketch]))
