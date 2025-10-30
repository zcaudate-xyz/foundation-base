(ns std.lib.enum
  (:require [std.string.case :as case]
            [std.string.coerce :as coerce]
            [std.lib.class :as cls]
            [std.lib.collection :as coll]
            [std.lib.invoke :refer [definvoke]]))

(defn enum?
  "check to see if class is an enum type
 
   (enum? java.lang.annotation.ElementType) => true
 
   (enum? String) => false"
  {:added "3.0"}
  ([type]
   (if (-> (cls/ancestor:list type)
           (set)
           (get java.lang.Enum))
     true false)))

(defn enum-values
  "returns all values of an enum type
 
   (->> (enum-values ElementType)
        (map str))
   => (contains [\"TYPE\" \"FIELD\" \"METHOD\" \"PARAMETER\" \"CONSTRUCTOR\"]
                :in-any-order :gaps-ok)"
  {:added "3.0"}
  ([^Class type]
   (let [method (.getMethod type "values" (make-array Class 0))
         values (.invoke method nil (object-array []))]
     (seq values))))

(defn create-enum
  "creates an enum value from a string
 
   (create-enum \"TYPE\" ElementType)
   => ElementType/TYPE"
  {:added "3.0"}
  ([s type]
   (loop [[e :as values] (enum-values type)]
     (cond (empty? values)
           (throw (ex-info "Cannot create enum" {:type type
                                                 :value s}))
           (= (str e) s) e

           :else
           (recur (rest values))))))

(definvoke enum-map
  "cached map of enum values"
  {:added "3.0"}
  [:memoize]
  ([type]
   (coll/map-juxt [(comp keyword case/spear-case coerce/to-string)
                   identity]
                  (enum-values type))))

(defn enum-map-form
  "creates the form for the enum"
  {:added "3.0"}
  ([^Class type]
   (coll/map-juxt [(comp keyword case/spear-case str)
                   #(symbol (.getName type) (str %))]
                  (enum-values type))))

(defmacro enum-map>
  "a macro for getting elements of the enum"
  {:added "3.0"}
  ([type]
   (enum-map-form (resolve type))))

(defn to-enum
  "gets an enum value given a symbol
   
   (to-enum \"TYPE\" ElementType)
   => ElementType/TYPE
 
   (to-enum :field ElementType)
   => ElementType/FIELD"
  {:added "3.0"}
  ([s type]
   (let [key ((comp keyword case/spear-case coerce/to-string) s)]
     (or (get (enum-map type) key)
         (throw (ex-info "Cannot find the enum value."
                         {:input s
                          :key key
                          :type type
                          :options (sort (keys (enum-map type)))}))))))
