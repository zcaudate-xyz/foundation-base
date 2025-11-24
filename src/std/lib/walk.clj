(ns std.lib.walk)

(defn walk
  "Traverses form, an arbitrary data structure"
  {:added "3.0"}
  [inner outer form]
  (cond    
    (list? form) (outer (with-meta (apply list (map inner form)) (meta form)))

    (instance? clojure.lang.IMapEntry form) (outer (vec (map inner form)))
    (seq? form) (outer (doall (with-meta (map inner form) (meta form))))
    (instance? clojure.lang.IRecord form)
    (outer (reduce (fn [r x] (conj r (inner x))) form form))

    (set? form) (outer (with-meta (into (empty form) (map inner form)) (meta form)))
    (map? form) (outer (with-meta (into (empty form) (map inner form)) (meta form)))
    (vector? form) (outer (with-meta (mapv inner form) (meta form)))
    (coll? form) (outer (into (empty form) (map inner form)))
    :else (outer form)))

(defn postwalk
  "Performs a depth-first, post-order traversal of form"
  {:added "3.0"}
  ([f form]
   (walk (partial postwalk f) f form)))

(defn prewalk
  "Like postwalk, but does pre-order traversal."
  {:added "3.0"}
  ([f form]
   (walk (partial prewalk f) identity (f form))))

(defn keywordize-keys
  "Recursively transforms all map keys from strings to keywords."
  {:added "3.0"}
  ([m]
   (let [f (fn [[k v]] (if (or (string? k)
                               (symbol? k))
                         [(keyword k) v] [k v]))]
    ;; only apply to maps
     (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(defn keyword-spearify-keys
  "recursively transfroms all map keys to spearcase
 
   (keyword-spearify-keys  {\"a_b_c\" [{\"e_f_g\" 1}]})
   => {:a-b-c [{:e-f-g 1}]}"
  {:added "4.0"}
  ([m]
   (let [f (fn [[k v]] (if (string? k) [(keyword (.replaceAll ^String k "_" "-")) v] [k v]))]
    ;; only apply to maps
     (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(defn stringify-keys
  "Recursively transforms all map keys from keywords to strings."
  {:added "3.0"}
  ([m]
   (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
    ;; only apply to maps
     (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(defn string-snakify-keys
  "recursively transforms keyword to string keys
 
   (string-snakify-keys
    {:a-b-c [{:e-f-g 1}]})
   => {\"a_b_c\" [{\"e_f_g\" 1}]}"
  {:added "4.0"}
  ([m]
   (let [f (fn [[k v]] (if (keyword? k) [(.replaceAll ^String (name k) "-" "_") v] [k v]))]
    ;; only apply to maps
     (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(defn prewalk-replace
  "Recursively transforms form by replacing keys in smap with their values."
  {:added "3.0"}
  ([smap form]
   (prewalk (fn [x] (if (contains? smap x) (smap x) x)) form)))

(defn postwalk-replace
  "Recursively transforms form by replacing keys in smap with their values."
  {:added "3.0"}
  ([smap form]
   (postwalk (fn [x] (if (contains? smap x) (smap x) x)) form)))

(defn macroexpand-all
  "Recursively performs all possible macroexpansions in form."
  {:added "3.0"}
  ([form]
   (prewalk (fn [x] (if (seq? x) (macroexpand x) x)) form)))


(defn walk:contains
  "recursively walks form to check for containment"
  {:added "4.0"}
  ([pred form]
   (let [found (volatile! false)]
     (prewalk (fn [x]
                (if (try (pred x) (catch Throwable t))
                  (vreset! found true))
                x)
              form)
     @found)))

(defn walk:find
  "recursively walks to find all matching forms"
  {:added "4.0"}
  ([pred form]
   (let [found (volatile! #{})]
     (postwalk (fn [x]
                 (if (try (pred x) (catch Throwable t))
                   (vswap! found conj x))
                 x)
               form)
     @found)))

(defn walk:keep
  "recursively walks and keeps all processed forms"
  {:added "4.0"}
  ([f form]
   (let [found (volatile! #{})]
     (postwalk (fn [x]
                 (try (let [val (f x)]
                        (if val (vswap! found conj val)))
                      (catch Throwable t))
                 x)
               form)
     @found)))
