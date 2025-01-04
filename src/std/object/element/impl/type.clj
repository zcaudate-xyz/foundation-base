(ns std.object.element.impl.type
  (:require [std.protocol.string :as protocol.string]
            [std.object.element.modifier :as modifier]
            [std.object.element.class :as class]
            [std.string :as str]))

(defonce +override+ 
  (try
    (doto (.getDeclaredField java.lang.reflect.AccessibleObject "override")
      (.setAccessible true))
    (catch Throwable t)))

(extend-type Class
  protocol.string/IString
  (-to-string [x] (.getName x)))

(extend-type java.lang.reflect.Member
  protocol.string/IString
  (-to-string [x] (.getName x)))

(defprotocol IElement
  (get-modifiers [obj])
  (get-declaring-class [obj]))

(extend-protocol IElement
  Class
  (get-modifiers [obj] (.getModifiers obj))
  (get-declaring-class [obj] (.getDeclaringClass obj))

  java.lang.reflect.Member
  (get-modifiers [obj] (.getModifiers obj))
  (get-declaring-class [obj] (.getDeclaringClass obj)))

(defn set-accessible
  "sets the accessible flag in the class to be true
 
   (common/set-accessible (.getDeclaredMethod String \"charAt\"
                                              (doto ^\"[Ljava.lang.Class;\"
                                              (make-array Class 1)
                                                (aset 0 Integer/TYPE)))
                          true)"
  {:added "3.0"}
  ([^java.lang.reflect.AccessibleObject obj flag]
   (cond +override+
         (.set ^java.lang.reflect.Field +override+ obj flag)

         (instance? java.lang.reflect.Constructor obj)
         (try
           (.setAccessible obj flag)
           (catch Throwable t))
         
         :else
         (.setAccessible obj flag))))

(defn add-annotations
  "adds additional annotations to the class
 
   (common/add-annotations {} String)
   => {}"
  {:added "3.0"}
  ([seed obj]
   (if-let [anns (seq (.getDeclaredAnnotations
                       ^java.lang.reflect.AnnotatedElement obj))]
     (->> anns
          (map (fn [^java.lang.annotation.Annotation ann] [(.annotationType ann)
                                                           (str ann)]))
          (into {})
          (assoc seed :annotations))
     seed)))

(defn seed
  "returns the preliminary attributes for creating an element
 
   (common/seed :class String)
   => (contains {:name \"java.lang.String\",
                 :tag :class,
                 :modifiers #{:instance :public :final :class},
                 :static false,
                 :delegate java.lang.String})
 
   (common/seed :method (.getDeclaredMethod String \"charAt\"
                                            (doto ^\"[Ljava.lang.Class;\"
                                             (make-array Class 1)
                                              (aset 0 Integer/TYPE))))
   => (contains {:name \"charAt\",
                 :tag :method,
                 :container java.lang.String,
                 :modifiers #{:instance :method :public}
                 :static false,
                 :delegate  java.lang.reflect.Method})"
  {:added "3.0"}
  ([tag obj]
   (let [int-m (get-modifiers obj)
         modifiers (conj (modifier/int-to-modifiers int-m tag) tag)
         modifiers (if (some #(contains? modifiers %) [:public :private :protected])
                     modifiers
                     (conj modifiers :plain))
         modifiers (if (or (contains? modifiers :static)
                           (= tag :constructor))
                     modifiers
                     (conj modifiers :instance))
         _ (if (not= tag :class) (set-accessible obj true))]
     (-> {:name (str/to-string obj)
          :tag  tag
          :hash (.hashCode ^Object obj)
          :container (get-declaring-class obj)
          :modifiers modifiers
          :static  (contains? modifiers :static)
          :delegate obj}
         (add-annotations obj)))))
