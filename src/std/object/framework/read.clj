(ns std.object.framework.read
  (:require [std.lib.class]
            [std.lib.collection]
            [std.lib.invoke :refer [definvoke]]
            [std.object.query :as query]
            [std.protocol.object :as protocol.object]
            [std.string.case]))

(defn meta-read
  "access read attributes
 
   (read/meta-read Pet)
   => (contains-in {:class test.Pet
                    :methods {:name {:fn fn?}
                              :species {:fn fn?}}})"
  {:added "3.0"}
  ([^Class cls]
   (assoc (protocol.object/-meta-read cls) :class cls)))

(definvoke meta-read-exact
  "access read attributes for the exact class
 
   (read/meta-read-exact Object)
   => nil"
  {:added "3.0"}
  [:memoize]
  ([^Class cls]
   (let [table (std.lib.invoke/multi:list protocol.object/-meta-read)]
     (if-let [f (get table cls)]
       (assoc (f cls) :class cls)))))

(defn meta-read-exact?
  "checks if read attributes are avaliable
 
   (read/meta-read-exact? Object)
   => false"
  {:added "3.0"}
  ([^Class cls]
   (boolean (get (std.lib.invoke/multi:list protocol.object/-meta-read) cls))))

(defn read-fields
  "fields of an object from reflection
   (-> (read/read-fields Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls]
   (read-fields cls query/query-class))
  ([cls query-fn]
   (->> (query-fn cls [:field])
        (std.lib.collection/map-juxt [(comp keyword std.string.case/spear-case :name)
                     (fn [ele]
                       {:type (:type ele)
                        :fn ele})]))))

(defn read-all-fields
  "all fields of an object from reflection
 
   (-> (read/read-all-fields {})
       keys)
   => [:-hash :-hasheq :-meta :array]"
  {:added "3.0"}
  ([cls]
   (read-fields cls query/query-instance-hierarchy)))

(defn +read-template+
  "returns a function template given tag and name
 
   (read/+read-template+ 'String 'hello)
   => '(clojure.core/fn hello [obj] (. obj (hello)))"
  {:added "3.0"}
  ([<tag> <method>]
   `(fn ~<method> [~(with-meta 'obj {:tag <tag>})] (. ~'obj (~<method>)))))

(defonce +read-has-opts+
  {:prefix "has" :template +read-template+ :extra "?"})

(defonce +read-is-opts+
  {:prefix "is" :template +read-template+ :extra "?"})

(defonce +read-get-opts+
  {:prefix "get" :template +read-template+})

(defn create-read-method-form
  "creates a method based on a template
   (eval (read/create-read-method-form (reflect/query-class Dog [\"getName\" :#])
                                       \"get\"
                                       read/+read-template+
                                       nil))
   => (contains-in [:name {:type String :fn fn?}])"
  {:added "3.0"}
  ([ele prefix template extra]
   (let [name  (-> (:name ele)
                   (subs (count prefix))
                   std.string.case/spear-case
                   (str (or extra ""))
                   keyword)
         ^Class cls  (:type ele)
         clsname  (.getName cls)
         clssym   (or (std.lib.class/primitive clsname :clssym)
                      (if (or (.isPrimitive cls)
                              (.isArray cls))
                        `(Class/forName ~clsname))
                      (symbol clsname))
         tag    (symbol (.getName ^Class (:container ele)))
         form   (template tag (symbol (:name ele)))]
     [name {:type clssym
            :fn   form}])))

(defn read-getters-form
  "creates the form for read-getters
 
   (read/read-getters-form Dog)
   => '{:name {:type java.lang.String,
               :fn (clojure.core/fn getName [obj] (. obj (getName)))},
        :species {:type java.lang.String,
                  :fn (clojure.core/fn getSpecies [obj] (. obj (getSpecies)))}}"
  {:added "3.0"}
  ([cls] (read-getters-form cls +read-get-opts+))
  ([cls opts] (read-getters-form cls opts query/query-class))
  ([cls {:keys [prefix template extra] :as opts} query-fn]
   (->> [:method :instance :public (re-pattern (str "^" prefix ".+")) 1]
        (query-fn cls)
        (reduce (fn [out ele]
                  (conj out (create-read-method-form ele prefix template extra)))
                {}))))

(defn parse-opts
  "returns map for a given input
 
   (def -opts- {:a 1})
   (read/parse-opts '-opts-)
   => {:a 1}"
  {:added "3.0"}
  ([opts]
   (cond (symbol? opts)
         @(resolve opts)

         (map? opts) opts

         :else (throw (ex-info "Cannot parse opts" {:opts opts})))))

(defmacro read-getters
  "returns fields of an object through getter methods
   (-> (read/read-getters Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls]
   (read-getters-form (resolve cls)))
  ([cls opts]
   (read-getters-form (resolve cls) (parse-opts opts)))
  ([cls opts query-fn]
   (read-getters-form (resolve cls) (parse-opts opts) query-fn)))

(defmacro read-all-getters
  "returns fields of an object and base classes
   (-> (read/read-all-getters Dog)
       keys)
   => [:class :name :species]"
  {:added "3.0"}
  ([cls] (read-getters-form (resolve cls) +read-get-opts+ query/query-hierarchy))
  ([cls opts]
   (read-getters-form (resolve cls) (parse-opts opts) query/query-hierarchy)))

(defn read-ex
  "creates a getter method that throws an an exception
 
   ((read/read-ex :hello) nil)
   => (throws)"
  {:added "3.0"}
  ([k]
   (fn [obj] (throw (ex-info "Not avaliable" {:key k
                                              :input obj})))))

(defn to-data
  "creates the object from a string or map
 
   (read/to-data \"hello\")
   => \"hello\"
 
   (read/to-data (write/from-map {:name \"hello\" :species \"dog\"} Pet))
   => (contains {:name \"hello\"})"
  {:added "3.0"}
  ([obj]
   (let [cls (type obj)
         {:keys [to-clojure to-string to-map to-vector methods]} (or (meta-read-exact cls)
                                                                     (meta-read cls))]
     (cond (nil? obj) nil

           (instance? java.util.Map obj)
           obj

           to-clojure (to-clojure obj)

           to-string (to-string obj)

           to-map (to-map obj)

           to-vector (to-vector obj)

           methods  (reduce-kv
                     (fn [out k getter]
                      ;; (prn :OBJ-TO-DATA k getter)
                       (if-some [v ((:fn getter) obj)]
                         (assoc out k (to-data v))
                         out))
                     {}
                     methods)

           (.isArray ^Class cls)
           (->> (seq obj)
                (mapv to-data))

           (instance? java.lang.Iterable obj)
           (mapv to-data obj)

           (instance? java.util.Iterator obj)
           (->> obj iterator-seq (mapv to-data))

           (instance? java.util.Enumeration obj)
           (->> obj enumeration-seq (mapv to-data))

           (instance? java.util.AbstractCollection obj)
           (to-data (.iterator ^java.util.AbstractCollection obj))

           (instance? java.lang.Enum obj)
           (str obj)

           :else obj))))

(defn to-map
  "creates a map from an object
 
   (read/to-map (Cat. \"spike\"))
   => (contains {:name \"spike\"})"
  {:added "3.0"}
  ([obj]
   (let [cls (type obj)
         {:keys [to-map methods]} (or (meta-read-exact cls)
                                      (meta-read cls))]
     (cond (nil? obj) nil

           (instance? java.util.Map obj)
           obj

           to-map (to-map obj)

           methods (reduce-kv (fn [out k getter]
                                (if-some [v ((:fn getter) obj)]
                                  (assoc out k (to-data v))
                                  out))
                              {}
                              methods)

           :else
           (throw (ex-info "Not Supported" {:input obj}))))))
