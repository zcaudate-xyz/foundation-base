(ns std.object.framework.write
  (:require [std.lib.class]
            [std.lib.collection]
            [std.lib.enum :as enum]
            [std.lib.invoke :refer [definvoke]]
            [std.object.element.util :as element.util]
            [std.object.framework.read :as read]
            [std.object.query :as query]
            [std.protocol.object :as protocol.object]
            [std.string.case])
  (:import (java.lang.reflect Field)))

(def ^:dynamic *transform* nil)

(defn +write-template+
  "constructs the write function template"
  {:added "3.0"}
  ([<cls> <method> <return>]
   `(fn ~<method> [~(with-meta 'obj {:tag <cls>}) ~(with-meta 'val {:tag <return>})]
      (or (. ~'obj (~<method> ~'val))
          ~'obj))))

(defn meta-write
  "access read-attributes with caching
 
   (write/meta-write DogBuilder)
   => (contains-in {:class test.DogBuilder
                    :empty fn?,
                    :methods {:name {:type java.lang.String, :fn fn?}}})"
  {:added "3.0"}
  ([^Class cls]
   (assoc (protocol.object/-meta-write cls) :class cls)))

(definvoke meta-write-exact
  "access write attributes for the exact class
 
   (write/meta-write-exact Object)
   => nil"
  {:added "3.0"}
  [:memoize]
  ([^Class cls]
   (let [table (std.lib.invoke/multi:list protocol.object/-meta-write)]
     (if-let [f (get table cls)]
       (assoc (f cls) :class cls)))))

(defn meta-write-exact?
  "checks if write attributes are avaliable
 
   (write/meta-write-exact? Object)
   => false"
  {:added "3.0"}
  ([^Class cls]
   (boolean (get (std.lib.invoke/multi:list protocol.object/-meta-write) cls))))

(declare from-data)

(defn write-fields
  "write fields of an object from reflection
 
   (-> (write/write-fields Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls]
   (write-fields cls query/query-class))
  ([cls query-fn]
   (->> (query-fn cls [:field :instance])
        (reduce (fn [out ele]
                  (let [k (-> ele :name std.string.case/spear-case keyword)
                        cls (.getType ^Field (get-in ele [:all :delegate]))]
                    (assoc out k {:type cls :fn ele})))
                {}))))

(defn write-all-fields
  "all write fields of an object from reflection
 
   (-> (write/write-all-fields {})
       keys)
   => [:-hash :-hasheq :-meta :array]"
  {:added "3.0"}
  ([cls]
   (write-fields cls query/query-instance-hierarchy)))

(defn create-write-method-form
  "create a write method from the template
 
   (-> ^test.Cat
    ((eval (-> (write/create-write-method-form (reflect/query-class Cat [\"setName\" :#])
                                               \"set\"
                                               write/+write-template+
                                               {})
               second
               :fn)) (test.Cat. \"spike\") \"fluffy\")
       (.getName))
  => \"fluffy\""
  {:added "3.0"}
  ([ele prefix template select]
   (let [name  (-> (:name ele) (subs (count prefix)) std.string.case/spear-case keyword)
         ^Class cls (second (:params ele))
         clsname    (.getName cls)
         clssym     (or (std.lib.class/primitive clsname :clssym)
                        (if (or (.isPrimitive cls)
                                (.isArray cls))
                          `(Class/forName ~clsname))
                        (symbol clsname))
         clstag     (or (if (.isArray cls)
                          clsname)
                        (if-let [^Class boxed (std.lib.class/primitive clsname :container)]
                          (symbol (.getName boxed)))
                        (symbol clsname))
         tag    (symbol (.getName ^Class (:container ele)))
         form   (template tag (symbol (:name ele)) clstag)
         selected (get select name)]
     (if (or (nil? selected)
             (= selected clstag))
       [name {:type clssym :fn form}]))))

(defn write-setters-form
  "constructs forms for a given object
 
   (write/write-setters-form DogBuilder)
   => '{:name {:type java.lang.String,
               :fn (clojure.core/fn setName [obj val]
                     (clojure.core/or (. obj (setName val)) obj))}}"
  {:added "3.0"}
  ([cls] (write-setters-form cls {}))
  ([cls opts] (write-setters-form cls opts query/query-class))
  ([cls {:keys [prefix template create select]
         :or {prefix "set"
              template +write-template+
              create create-write-method-form}} query-fn]
   (->> [:method :instance :public (re-pattern (str "^" prefix ".+")) 2]
        (query-fn cls)
        (reduce (fn [out ele]
                  (if-let [pair (create ele prefix template select)]
                    (conj out pair)
                    out))
                {}))))

(defmacro write-setters
  "write fields of an object through setter methods
 
   (write/write-setters Dog)
   => {}
 
   (keys (write/write-setters DogBuilder))
   => [:name]"
  {:added "3.0"}
  ([cls]
   (write-setters-form (resolve cls)))
  ([cls opts]
   (write-setters-form (resolve cls) (read/parse-opts opts)))
  ([cls opts query-fn]
   (write-setters-form (resolve cls) (read/parse-opts opts) query-fn)))

(defmacro write-all-setters
  "write all setters of an object and base classes
 
   (write/write-all-setters Dog)
   => {}
 
   (keys (write/write-all-setters DogBuilder))
   => [:name]"
  {:added "3.0"}
  ([cls] (write-setters-form (resolve cls) {}))
  ([cls opts]
   (write-setters-form (resolve cls) (read/parse-opts opts) query/query-hierarchy)))

(defn write-ex
  "writes a getter method that throws an an exception
 
   ((write/write-ex :hello) nil nil)
   => (throws)"
  {:added "3.0"}
  ([k]
   (fn [obj val] (throw (ex-info "Not avaliable" {:key k
                                                  :input obj})))))

(defn write-constructor
  "returns constructor element for a given class
 
   (write/write-constructor Cat [:name])
   ;;#elem[new :: (java.lang.String) -> test.Cat]
   => ifn?"
  {:added "3.0"}
  ([cls params]
   (query/query-class cls [(count params) "new" :#])))

(defn write-setter-element
  "also constructs array elements
 
   (write/write-setter-element {:element Dog} [{:name \"fido\"}])
   => (contains [test.Dog])"
  {:added "3.0"}
  ([{:keys [element]} v]
   (if (and element (vector? v))
     (mapv #(from-data % element) v)
     v)))

(defn write-setter-value
  "sets the property given keyword and value
 
   (-> ^test.Cat
    (write/write-setter-value {:name {:type String
                                      :fn (fn [^test.Cat cat name] (.setName cat name))}}
                              (Cat. \"lido\")
                              :name
                              \"tim\")
       (.getName))
   => \"tim\""
  {:added "3.0"}
  ([methods obj k v]
   (if-let [{:keys [type ignore exclude] :as setter} (get methods k)]
     (let [v (write-setter-element setter v)
           v (if (or (get ignore (class v))
                     (and exclude (exclude v)))
               v
               (from-data v type))]
       ((:fn setter) obj v))
     (if-not (namespace k)
       (throw (ex-info "Not Found" {:object obj
                                    :key k
                                    :value v
                                    :method (get methods k)}))))
   obj))

(defn from-empty
  "creates the object from an empty object constructor
 
   (write/from-empty {:name \"chris\" :pet \"dog\"}
                     (fn [] (java.util.Hashtable.))
                     {:name {:type String
                             :fn (fn [^java.util.Hashtable obj v]
                                   (.put obj \"hello\" (keyword v))
                                   obj)}
                      :pet  {:type String
                             :fn (fn [^java.util.Hashtable obj v]
                                  (.put obj \"pet\" (keyword v))
                                   obj)}})
   => {\"pet\" :dog, \"hello\" :chris}"
  {:added "3.0"}
  ([m empty methods]
   (let [obj (empty)]
     (reduce-kv (partial write-setter-value methods)
                obj
                m))))

(defn from-constructor
  "creates the object from a constructor
 
   (-> {:name \"spike\"}
       (write/from-constructor {:fn (fn [name] (Cat. name))
                                :params [:name]}
                               {:name {:type String}}
                               test.Cat))
   ;; #test.Cat{:name \"spike\", :species \"cat\"}
   => test.Cat"
  {:added "3.0"}
  ([m {:keys [params default types] :as construct} methods cls]
   (let [setters (map methods params)
         args    (->> (map (fn [k] (or (get m k) (get default k))) params)
                      (map (fn [setter k arg]
                             (let [setter (or setter (get-in (read/meta-read-exact cls)
                                                             [:methods k]))
                                   arg (write-setter-element setter arg)]
                               (from-data arg (:type setter))))
                           setters
                           params))
         obj   (apply (:fn construct) args)]
     (reduce-kv (partial write-setter-value methods)
                obj
                (apply dissoc m params)))))

(defn from-map
  "creates the object from a map
 
   (-> {:name \"chris\" :age 30 :pets [{:name \"slurp\" :species \"dog\"}
                                     {:name \"happy\" :species \"cat\"}]}
       (write/from-map test.Person)
       (read/to-data))
   => (contains-in
       {:name \"chris\",
        :age 30,
        :pets [{:name \"slurp\"}
              {:name \"happy\"}]})"
  {:added "3.0"}
  ([m ^Class cls]
   (let [m (if-let [rels (get *transform* type)]
             (std.lib.collection/transform m rels)
             m)
         {:keys [construct empty methods from-map] :as mobj} (meta-write-exact cls)]
     (cond from-map
           (from-map m)

           empty
           (from-empty m empty methods)

           construct
           (from-constructor m construct methods cls)

           :else
           (throw (ex-info "Cannot convert from map" {:data m
                                                      :type cls
                                                      :code (.hashCode cls)
                                                      :empty empty
                                                      :construct construct}))))))

(defn from-data
  "creates the object from data
 
   (-> (write/from-data [\"hello\"] (Class/forName \"[Ljava.lang.String;\"))
       seq)
   => [\"hello\"]"
  {:added "3.0"}
  ([arg ^Class cls]
   (let [^Class targ (type arg)]
     (cond
       (= targ cls)
       arg

      ;; If there is a direct match
       (or (element.util/param-arg-match cls targ)
           (element.util/param-float-match cls targ))
       arg

      ;; Special case for String/CharArray
       (and (string? arg) (= cls (Class/forName "[C")))
       (.toCharArray ^String arg)

      ;; Special case for Enums
       (enum/enum? cls)
       (cond (string? arg)
             (enum/to-enum arg cls)

             (keyword? arg)
             ((enum/enum-map cls) arg)

             :else
             (throw (ex-info "Only strings and keywords supported for Enums" {:input arg
                                                                              :type cls})))

      ;; Support for vectors
       (and (vector? arg)
            (.isArray cls))
       (let [cls (.getComponentType cls)]
         (->> arg
              (map #(from-data % cls))
              (into-array cls)))

       (and (vector? arg)
            (= java.util.Vector cls))
       (java.util.Vector. ^java.util.List arg)

       :else
       (let [{:keys [from-custom from-string from-vector] :as mobj} (meta-write-exact cls)]
         (cond

           from-custom (from-custom arg)

          ;; If input is a string and there is a from-string method
           (and (string? arg) from-string)
           (from-string arg)

          ;; If input is a vector and there is a from-vector method
           (and (vector? arg) from-vector)
           (from-vector arg)

          ;; If the input is a map
           (map? arg)
           (from-map arg cls)

           :else
           (throw (ex-info "Conversion unsupported" {:input arg
                                                     :type cls}))))))))
