(ns std.pretty.edn
  (:require [std.lib.foundation]
            [std.pretty.protocol :as protocol.pretty]))

(defn override?
  "implements `std.pretty.protocol/IOverride`"
  {:added "3.0"}
  ([x]
   (satisfies? protocol.pretty/IOverride x)))

(defn edn
  "converts an object to a tagged literal
 
   (edn 1)
   => 1
 
   (edn nil)
   => nil
 
   (edn (java.lang.ClassLoader/getPlatformClassLoader))
   => clojure.lang.TaggedLiteral"
  {:added "3.0"}
  ([x]
   (protocol.pretty/-edn x)))

(defn class->edn
  "converts a type to edn
 
   (class->edn (type (byte-array [])))
   => \"[B\"
 
   (class->edn (type (into-array String [])))
   => \"[Ljava.lang.String;\"
 
   (class->edn (type :keyword))
   => 'clojure.lang.Keyword"
  {:added "3.0"}
  ([^Class c]
   (if (.isArray c)
     (.getName c)
     (symbol (.getName c)))))

(defn tagged-object
  "converts a type to a tagged literal
 
   (tagged-object (java.lang.ClassLoader/getPlatformClassLoader) :classloader)
   ;;=> #object [jdk.internal.loader.ClassLoaders$PlatformClassLoader \"0x73698a00\" :classloader]
   => clojure.lang.TaggedLiteral"
  {:added "3.0"}
  ([o rep]
   (let [cls (class->edn (class o))
         id (format "0x%x" (System/identityHashCode o))]
     (tagged-literal 'object [cls id rep]))))

(defn format-date
  "helper function for formatting date"
  {:added "3.0"}
  ([v x]
   (let [local ^java.lang.ThreadLocal @v
         fmt ^java.text.SimpleDateFormat (.get local)]
     (.format fmt x))))

(extend-protocol protocol.pretty/IEdn

  nil
  (-edn [x]
    nil)

  java.lang.Object
  (-edn [x]
    (if (std.lib.foundation/edn? x)
      x
      (tagged-object x (str x))))

  clojure.lang.IDeref
  (-edn [x]
    (let [pending? (and (instance? clojure.lang.IPending x)
                        (not (.isRealized ^clojure.lang.IPending x)))
          [ex val] (when-not pending?
                     (try [false @x]
                          (catch Throwable e
                            [true e])))
          failed? (or ex (and (instance? clojure.lang.Agent x)
                              (agent-error x)))
          status (cond
                   failed? :failed
                   pending? :pending
                   :else :ready)]
      (tagged-object x {:status status :val val})))

  java.lang.Class
  (-edn [x]
    (class->edn x))

  java.util.List
  (-edn [x]
    (vec x))

  java.util.LinkedHashMap
  (-edn [x]
    (into {} x))

  java.util.AbstractMap
  (-edn [x]
    (into {} x))

  java.util.Date
  (-edn [x]
    (let [s (format-date #'clojure.instant/thread-local-utc-date-format x)]
      (tagged-literal 'inst s)))

  java.sql.Timestamp
  (-edn [x]
    (let [s (format-date #'clojure.instant/thread-local-utc-timestamp-format x)]
      (tagged-literal 'inst s)))

  java.util.UUID
  (-edn [x]
    (tagged-literal 'uuid (str x)))

  clojure.lang.PersistentQueue
  (-edn [x]
    (vec x)))

(defn visit-seq
  "creates a form for a seq
 
   (visit-seq (printer/canonical-printer)
              [1 2 3 4])
   => [:group \"(\" [:align [\"1\" \" \" \"2\" \" \" \"3\" \" \" \"4\"]] \")\"]"
  {:added "3.0"}
  ([visitor x]
   (protocol.pretty/-visit-seq visitor x)))

(defn visit-tagged
  "creates a form for a tagged literal
 
   (visit-tagged (printer/canonical-printer)
                 (tagged-literal 'hello [1 2 3]))
   => [:span \"#hello\" \" \" [:group \"[\" [:align [\"1\" \" \" \"2\" \" \" \"3\"]] \"]\"]]"
  {:added "3.0"}
  ([visitor x]
   (protocol.pretty/-visit-tagged visitor x)))

(defn visit-unknown
  "creatse a form for an unknown element
 
   (visit-unknown (printer/canonical-printer)
                  (Thread/currentThread))
   => (throws)"
  {:added "3.0"}
  ([visitor x]
   (protocol.pretty/-visit-unknown visitor x)))

(defn visit-meta
  "creates a form for a meta
   (visit-meta (printer/canonical-printer)
               {:a 1} {})
   => [:group \"{\" [:align ()] \"}\"]"
  {:added "3.0"}
  ([visitor meta x]
   (protocol.pretty/-visit-meta visitor meta x)))

(defn visit-edn
  "creates a form for a non-edn element
 
   (visit-edn (printer/canonical-printer)
              (doto (java.util.ArrayList.)
                (.add 1)
                (.add 2)
                (.add 3)))
   => [:group \"[\" [:align [\"1\" \" \" \"2\" \" \" \"3\"]] \"]\"]"
  {:added "3.0"}
  ([visitor x]
   (cond (nil? x) (protocol.pretty/-visit-nil visitor)
         (override? x) (protocol.pretty/-visit-unknown visitor x)
         (boolean? x) (protocol.pretty/-visit-boolean visitor x)
         (string? x) (protocol.pretty/-visit-string visitor x)
         (char? x) (protocol.pretty/-visit-character visitor x)
         (symbol? x) (protocol.pretty/-visit-symbol visitor x)
         (keyword? x) (protocol.pretty/-visit-keyword visitor x)
         (number? x) (protocol.pretty/-visit-number visitor x)
         (seq? x) (protocol.pretty/-visit-seq visitor x)
         (vector? x) (protocol.pretty/-visit-vector visitor x)
         (record? x) (protocol.pretty/-visit-record visitor x)
         (map? x) (protocol.pretty/-visit-map visitor x)
         (set? x) (protocol.pretty/-visit-set visitor x)
         (tagged-literal? x) (protocol.pretty/-visit-tagged visitor x)
         (var? x) (protocol.pretty/-visit-var visitor x)
         (std.lib.foundation/regexp? x) (protocol.pretty/-visit-pattern visitor x)
         (satisfies? protocol.pretty/IEdn x) (visit-edn visitor (edn x))
         :else (protocol.pretty/-visit-unknown visitor x))))

(defn visit
  "a extensible walker for printing `edn`` data
 
   (visit (printer/canonical-printer)
          (Thread/currentThread))
   => (contains-in [:span \"#object\" \" \" [:group \"[\" [:align coll?] \"]\"]])"
  {:added "3.0"}
  ([visitor x]
   (if-let [mta (and (std.lib.foundation/iobj? x) (meta x))]
     (protocol.pretty/-visit-meta visitor mta x)
     (visit-edn visitor x))))
