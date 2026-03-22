(ns std.object.element.class
  (:require [std.lib.class :as class]
            [std.string.coerce]))

(defn type->raw
  "converts to the raw representation
 
   (type->raw Class) => \"java.lang.Class\"
   (type->raw 'byte) => \"B\""
  {:added "3.0"}
  ([v]
   (let [raw (std.string.coerce/to-string v)]
     (or (std.lib.class/primitive raw :raw)
         raw))))

(declare raw->string)

(defn raw-array->string
  "converts the raw representation to a more readable form
 
   (raw-array->string \"[[B\") => \"byte[][]\"
   (raw-array->string \"[Ljava.lang.Class;\") => \"java.lang.Class[]\""
  {:added "3.0"}
  ([v]
   (if-let [obj-name (second (re-find #"^L(.*);" v))]
     (raw->string obj-name)
     (raw->string v))))

(defn raw->string
  "converts the raw array representation to a human readable form
 
   (raw->string \"[[V\") => \"void[][]\"
   (raw->string \"[Ljava.lang.String;\") => \"java.lang.String[]\""
  {:added "3.0"}
  ([^String v]
   (if (.startsWith v "[")
     (str (raw-array->string (subs v 1)) "[]")
     (or (std.lib.class/primitive v :string)
         v))))

(defn string-array->raw
  "converts the human readable form to a raw string
 
   (string-array->raw \"java.lang.String[]\") \"[Ljava.lang.String;\""
  {:added "3.0"}
  ([s] (string-array->raw s false))
  ([^String s arr]
   (if (.endsWith s "[]")
     (str "[" (string-array->raw
               (subs s 0 (- (.length s) 2)) true))
     (if arr
       (or (std.lib.class/primitive s :raw)
           (str "L" s ";"))
       s))))

(defn string->raw
  "converts any string to it's raw representation
 
   (string->raw \"java.lang.String[]\") => \"[Ljava.lang.String;\"
 
   (string->raw \"int[][][]\") => \"[[[I\""
  {:added "3.0"}
  ([v]
   (or (std.lib.class/primitive v :raw)
       (string-array->raw v))))

(declare class-convert)

(defmulti -class-convert
  "converts a string to its representation. Implementation function
 
   (-class-convert Class  :string) => \"java.lang.Class\"
 
   (-class-convert \"byte\" :class) => Byte/TYPE
 
   (-class-convert \"byte\" :container) => Byte"
  {:added "3.0"}
  (fn [v to] (type v)))

(defmethod -class-convert :default
  [v to]
  nil)

(defmethod -class-convert Class
  ([^Class v to]
   (condp = to
     :container (if (class/+primitive-lookup+ v)
                  (std.lib.class/primitive v :container)
                  v)
     :class    (if (class/+primitive-lookup+ v)
                 (std.lib.class/primitive v :class)
                 v)
     :symbol (class-convert (.getName v) to)
     :raw (type->raw v)
     :string (raw->string (type->raw v)))))

(defmethod -class-convert clojure.lang.Symbol
  ([v to]
   (condp = to
     :container (or (std.lib.class/primitive v :container)
                    (eval v))
     :class (or (std.lib.class/primitive v :class)
                (eval v))
     :symbol v
     :raw (string->raw (name v))
     :string (raw->string (name v)))))

(defmethod -class-convert String
  ([v to]
   (condp = to
     :container (or (std.lib.class/primitive v :container)
                    (Class/forName (string->raw v)))
     :class (or (std.lib.class/primitive v :class)
                (Class/forName (string->raw v)))
     :symbol (or (std.lib.class/primitive v :symbol)
                 (symbol (string->raw v)))
     :raw (string->raw v)
     :string (raw->string v))))

(defn class-convert
  "Converts a class to its representation.
 
   (class-convert \"byte\") => Byte/TYPE
 
   (class-convert 'byte :string) => \"byte\"
 
   (class-convert (Class/forName \"[[B\") :string) => \"byte[][]\""
  {:added "3.0"}
  ([v] (class-convert v :class))
  ([v to] (-class-convert v to)))
