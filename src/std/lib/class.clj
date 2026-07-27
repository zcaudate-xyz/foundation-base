(ns std.lib.class
  (:require [clojure.set :as set])
  (:refer-clojure :exclude [ancestors]))

(defn class:array?
  "checks if a class is an array class
 
   (class:array? (type (int-array 0)))
   => true"
  {:added "3.0"}
  ([^Class cls]
   (.isArray cls)))

(defn primitive?
  "checks if a class of primitive type"
  {:added "3.0"}
  ([^Class cls]
   (.isPrimitive cls)))

(defn primitive:array?
  "checks if class is a primitive array
 
   (primitive:array? (type (int-array 0)))
   => true
 
   (primitive:array? (type (into-array [1 2 3])))
   => false"
  {:added "3.0"}
  ([^Class cls]
   (and (.isArray cls)
        (.isPrimitive (.getComponentType cls)))))

(defn class:array-component
  "returns the array element within the array
 
   (class:array-component (type (int-array 0)))
   => Integer/TYPE
 
   (class:array-component (type (into-array [1 2 3])))
   => java.lang.Long"
  {:added "3.0"}
  ([^Class cls]
   (if (class:array? cls)
     (.getComponentType cls)
     (throw (ex-info "Not an array" {:class cls})))))

(defn class:interface?
  "returns `true` if `class` is an interface
 
   (class:interface? java.util.Map) => true
 
   (class:interface? Class) => false"
  {:added "3.0"}
  ([^Class class]
   (.isInterface class)))

(defn class:abstract?
  "returns `true` if `class` is an abstract class
 
   (class:abstract? java.util.Map) => true
 
   (class:abstract? Class) => false"
  {:added "3.0"}
  ([^Class class]
   (java.lang.reflect.Modifier/isAbstract (.getModifiers class))))

(def +primitives+
  {:void    ["V" Void      nil     nil           :no-size]
   :boolean ["Z" Boolean   boolean boolean-array :no-size]
   :byte    ["B" Byte      byte    byte-array]
   :char    ["C" Character char    char-array]
   :short   ["S" Short     short   short-array]
   :int     ["I" Integer   int     int-array]
   :long    ["J" Long      long    long-array]
   :float   ["F" Float     float   float-array]
   :double  ["D" Double    double  double-array]})

(def +primitive-records+
  (->> +primitives+
       (mapv (fn [[k [raw ^Class container type-fn array-fn no-size?]]]
               (let [kstr (name k)
                     cls (.get (.getField container "TYPE") nil)
                     base {:type k
                           :raw raw
                           :symbol (symbol kstr)
                           :clssym (symbol (.getName container) "TYPE")
                           :clstag (symbol (str cls))
                           :string kstr
                           :container container
                           :class cls}
                     size (if-not no-size?
                            {:size  (.get (.getField container "SIZE") nil)
                             :bytes (.get (.getField container "BYTES") nil)})
                     types (if (not= k :void)
                             {:type-fn type-fn
                              :array-raw (str "[" raw)
                              :array-fn array-fn
                              :array-class (Class/forName (str "[" raw))})]
                 [k (merge base size types)])))
       (into {})))

(defn create-lookup
  "creates a path lookup given a record
 
   (create-lookup {:byte {:name \"byte\" :size 1}
                   :long {:name \"long\" :size 4}})
   => {\"byte\" [:byte :name]
       1 [:byte :size]
       \"long\" [:long :name]
       4 [:long :size]}"
  {:added "3.0"}
  ([m] (create-lookup m (constantly false)))
  ([m ignore]
   (reduce-kv (fn [out type record]
                (reduce-kv (fn [out k v]
                             (if (ignore k)
                               out
                               (assoc out v [type k])))
                           out
                           record))
              {}
              m)))

(def +primitive-lookup+
  (create-lookup +primitive-records+ #{:size :bytes}))

(defn primitive
  "Converts primitive values across their different representations. The choices are:
    :raw       - The string in the jdk (i.e. `Z` for Boolean, `C` for Character)
    :symbol    - The symbol that std.object.query uses for matching (i.e. boolean, char, int)
    :string    - The string that std.object.query uses for matching
    :class     - The primitive class representation of the primitive
    :container - The containing class representation for the primitive type"
  {:added "3.0"}
  ([v to]
   (if-let [[type rep] (get +primitive-lookup+ v)]
     (get-in +primitive-records+ [type to]))))

(defn ancestor:list
  "Lists the direct ancestors of a class
   (ancestor:list clojure.lang.PersistentHashMap)
   => [clojure.lang.PersistentHashMap
       clojure.lang.APersistentMap
       clojure.lang.AFn
       java.lang.Object]"
  {:added "3.0"}
  ([cls] (ancestor:list cls []))
  ([^java.lang.Class cls output]
   (if (nil? cls)
     output
     (recur (.getSuperclass cls) (conj output cls)))))

(defn class:interfaces
  "Lists all interfaces for a class
 
   (class:interfaces clojure.lang.AFn)
   => #{java.lang.Runnable
        java.util.concurrent.Callable
        clojure.lang.IFn}"
  {:added "3.0"}
  ([^Class cls]
   (let [directs (.getInterfaces cls)
         sub (vec (mapcat class:interfaces directs))]
     (set (concat directs sub)))))

(defn ancestor:tree
  "Lists the hierarchy of bases and interfaces of a class.
   (ancestor:tree Class)
   => [[java.lang.Object
        #{java.lang.constant.Constable
          java.lang.invoke.TypeDescriptor
          java.lang.reflect.GenericDeclaration
          java.io.Serializable
          java.lang.reflect.Type
          java.lang.reflect.AnnotatedElement
          java.lang.invoke.TypeDescriptor$OfField}]]"
  {:added "3.0"}
  ([cls] (ancestor:tree cls []))
  ([^Class cls output]
   (let [base (.getSuperclass cls)]
     (if-not base output
             (recur base
                    (conj output [base (class:interfaces cls)]))))))

(defn ancestor:all
  "returns all ancestors for a given type, itself included
 
   (ancestor:all String)
   => #{java.lang.CharSequence
        java.lang.constant.ConstantDesc
        java.lang.constant.Constable
        java.lang.Comparable
        java.lang.String
        java.io.Serializable
        java.lang.Object}"
  {:added "3.0"}
  ([cls]
   (let [bases (ancestor:list cls)
         interfaces (mapcat class:interfaces bases)]
     (set (concat bases interfaces)))))

(defn class:inherits?
  "checks if one class inherits from another
 
   (class:inherits? clojure.lang.ILookup clojure.lang.APersistentMap)
   => true"
  {:added "3.0"}
  ([ancestor cls]
   (contains? (ancestor:all cls) ancestor)))

(defn class:match
  "finds the best matching interface or class from a list of candidates
 
   (class:match #{Object} Long) => Object
   (class:match #{String} Long) => nil
   (class:match #{Object Number} Long) => Number"
  {:added "3.0"}
  ([candidates ^Class cls]
   (or (get candidates cls)
       (->> (apply concat (ancestor:tree cls))
            (map (fn [v]
                   (if (set? v)
                     (first (set/intersection v candidates))
                     (get candidates v))))
            (filter identity)
            first))))





