(ns std.object.framework.struct
  (:require [std.lib :refer [definvoke]]
            [std.object.query :as reflect]
            [std.object.element :as element]
            [std.string :as str]))

(def ^:dynamic *lead-class* nil)

(definvoke getter-function
  "creates a getter function for a keyword
 
   (getter-function :class 'Class)
   => fn?"
  {:added "3.0"}
  [:memoize]
  ([k cls]
   (let [label  (name k)
         [prefix label] (if (.endsWith label "?")
                          ["is-" (subs label 0 (dec (count label)))]
                          ["get-" label])
         label   (str/camel-case (str prefix label))]
     (eval `(fn ~(symbol label) [~(with-meta 'obj {:tag cls})]
              (and ~'obj
                   (~(symbol (str "." label)) ~'obj)))))))

(definvoke field-function
  "creates a field access function
 
   ((field-function :value String) \"hello\")
   => bytes"
  {:added "3.0"}
  [:memoize]
  ([k ^Class cls]
   (or (try (element/to-element (.getDeclaredField cls (str/camel-case (name k))))
            (catch NoSuchFieldException e))
       (if-let [super (.getSuperclass cls)]
         (binding [*lead-class* (or *lead-class*
                                    cls)]
           (field-function k super)))
       (throw (ex-info "No Field." {:field k
                                    :class *lead-class*})))))

(defn struct-getters
  "creates a struct given an object and a getter map
 
   (struct-getters {:value [:data]
                    :message []
                    :class {:name []}}
                   (ex-info \"hello\" {:a 1}))
   => {:value {:a 1},
       :message \"hello\",
       :class {:name \"clojure.lang.ExceptionInfo\"}}"
  {:added "3.0"}
  ([spec obj]
   (let [clssym (symbol (.getName (class obj)))]
     (cond (vector? spec)
           ((apply comp (map #(getter-function % clssym)
                             (reverse spec))) obj)

           (map? spec)
           (reduce (fn [out [k entry]]
                     (cond (nil? obj)
                           out

                           (vector? entry)
                           (let [entry (if (empty? entry)
                                         [k]
                                         entry)]
                             (assoc out k (struct-getters entry obj)))

                           (map? entry)
                           (assoc out k (struct-getters entry ((getter-function k clssym) obj)))))
                   {}
                   spec)
           :else (throw (ex-info "Not valid." {:spec spec}))))))

(defn struct-field-functions
  "constructs fields access functions
 
   (struct-field-functions [:detail-message :value]
                           clojure.lang.ExceptionInfo)"
  {:added "3.0"}
  ([ks cls]
   (if-not (empty? ks)
     (let [[k & more] ks
           lead (field-function k cls)]
       (cons lead (struct-field-functions more (:type lead)))))))

(defn struct-fields
  "creates a struct given an object and a field map
 
   (struct-fields {:depth []}
                  (ex-info \"hello\" {:a 1}))
   => (contains {:depth number?})
 
   (struct-fields {:msg [:detail-message :value]}
                  (ex-info \"hello\" {:a 1}))
   => (contains {:msg bytes?})"
  {:added "3.0"}
  ([spec obj]
   (let [cls (type obj)]
     (cond (vector? spec)
           ((apply comp (reverse (struct-field-functions spec cls))) obj)

           (map? spec)
           (reduce (fn [out [k entry]]
                     (cond (nil? obj)
                           out

                           (vector? entry)
                           (let [entry (if (empty? entry)
                                         [k]
                                         entry)]
                             (assoc out k (struct-fields entry obj)))

                           (map? entry)
                           (assoc out k (struct-fields entry ((field-function k cls) obj)))))
                   {}
                   spec)))))

(defn struct-accessor
  "creates an accessor function
 
   ((struct-accessor {:value [:data]
                      :msg [:detail-message :value]}
                     :field)
    (ex-info \"hello\" {:a 1}))
   => (contains {:value {:a 1},
                 :msg bytes?})"
  {:added "3.0"}
  ([spec]
   (struct-accessor spec :getter))
  ([spec access]
   (case access
     :field  (fn field  [obj] (struct-fields spec obj))
     :getter (fn getter [obj] (struct-getters spec obj)))))

(defn dir
  "explores the fields of a object given a path
 
   (dir \"string\" [])
 
   (dir \"string\" [:hash])"
  {:added "3.0"}
  ([obj]
   (let [values (->> (map (juxt (comp keyword :name)
                                (fn [elem]
                                  (let [value (elem obj)
                                        t ^Class (:type elem)]
                                    {:type  (if (.isInterface t)
                                              (type value)
                                              t)
                                     :value value})))
                          (reflect/query-hierarchy (type obj) [:field]))
                     (into {}))]
     values))
  ([obj path]
   (cond (empty? path)
         (dir obj)

         :else
         (let [res (dir obj)
               sres (get res (first path))]
           (cond (nil? sres)
                 (throw (ex-info "No field." {:object obj
                                              :key (first path)}))

                 (.isPrimitive ^Class (:type sres))
                 sres

                 (nil? (:value sres))
                 sres

                 :else
                 (recur (:value sres) (rest path)))))))
