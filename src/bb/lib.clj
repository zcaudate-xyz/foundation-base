(ns bb.lib)

(defn intern-in
  "adds a function to current"
  {:added "3.0"}
  ([ns? & syms]
   (let [[ns syms] (if (or (vector? ns?)
                           (namespace ns?))
                     [(.getName clojure.core/*ns*) (cons ns? syms)]
                     [ns? syms])]
     (mapv (fn [sym]
             (let [[to from] (if (vector? sym)
                               sym
                               [sym sym])]
               (intern ns (symbol (name to)) (resolve from))))
           syms))))

(defmacro template-entries
  "uses a template function to produce entries"
  {:added "3.0" :style/indent 1}
  ([[tmpl-fn & [tmpl-meta]] & entries]
   (let [tmpl-fn (cond (symbol? tmpl-fn)
                       (resolve tmpl-fn)

                       (list? tmpl-fn)
                       (eval tmpl-fn))
         entries (mapcat (fn [entry]
                           (cond (symbol? entry)
                                 @(resolve entry)

                                 (list? entry)
                                 (eval entry)

                                 :else entry))
                         entries)]
     (mapv (fn [e]
             (try (tmpl-fn e)
                  (catch Throwable t
                    (throw t))))
           entries))))

(defn map-keys [f m]
  (reduce-kv (fn [m k v]
               (assoc m (f k) v))
             {}
             m))

(defn map-vals [f m]
  (reduce-kv (fn [m k v]
               (assoc m k (f v)))
             {}
             m))

(defn filter-keys [f m]
  (reduce-kv (fn [m k v]
               (if (f k)
                 (assoc m k v)
                 m))
             {}
             m))

(defn filter-vals [f m]
  (reduce-kv (fn [m k v]
               (if (f v)
                 (assoc m k v)
                 m))
             {}
             m))

(defn prewalk [f form]
  (clojure.walk/prewalk f form))

(defn postwalk [f form]
  (clojure.walk/postwalk f form))

(defmacro suppress
  "Suppresses any errors thrown in the body."
  {:added "3.0"}
  ([body]
   `(try ~body (catch Throwable ~'t)))
  ([body catch-val]
   `(try ~body (catch Throwable ~'t
                 (cond (fn? ~catch-val)
                       (~catch-val ~'t)
                       :else ~catch-val)))))

(defmacro const [body]
  (eval body))

(defmacro error
  ([message]
   `(throw (ex-info ~message {})))
  ([message data]
   `(throw (ex-info ~message ~data))))
