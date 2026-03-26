(ns rt.postgres.base.typed.typed-resolve
  (:require [clojure.string :as str]
            [rt.postgres.base.typed.typed-common :as types]))

(defn app-name-from-static
  [app]
  (cond
    (sequential? app) (first app)
    app app
    :else nil))

(defn fn-ref->fn-sym
  [fn-ref]
  (letfn [(nsish->str [x]
            (cond
              (instance? clojure.lang.Namespace x) (str (ns-name x))
              (symbol? x) (str x)
              (string? x) x
              :else nil))
          (nameish->str [x]
            (cond
              (symbol? x) (name x)
              (keyword? x) (name x)
              (string? x) x
              :else nil))]
    (cond
      (symbol? fn-ref)
      (if (namespace fn-ref)
        fn-ref
        (symbol (str (ns-name *ns*)) (name fn-ref)))

      (var? fn-ref)
      (let [{:keys [ns name]} (meta fn-ref)]
        (when (and ns name)
          (symbol (str (ns-name ns)) (str name))))

      (instance? clojure.lang.IDeref fn-ref)
      (let [d (try (deref fn-ref)
                   (catch Throwable _ nil))
            module-str (or (some-> (get d :module) nsish->str)
                           (some-> (get d :namespace) nsish->str)
                           (some-> (get d :ns) nsish->str))
            id-val (or (get d :id)
                       (get d :name))
            id-str (nameish->str id-val)]
        (cond
          (and (symbol? id-val) (namespace id-val))
          id-val

          (and module-str id-str)
          (symbol module-str id-str)

          :else nil))

      (instance? clojure.lang.IFn fn-ref)
      (let [class-name (.getName (class fn-ref))
            parts (str/split class-name #"\$")
            ns-part (first parts)
            fn-part (second parts)]
        (when (and ns-part fn-part (not (str/blank? ns-part)) (not (str/blank? fn-part)))
          (symbol (-> ns-part (str/replace "_" "-"))
                  (-> fn-part (str/replace "_" "-")))))

      :else nil)))

(defn fn-ref->app-name
  [fn-ref fn-def]
  (or (some-> (get-in fn-def [:body-meta :static/application])
              app-name-from-static)
      (when (instance? clojure.lang.IDeref fn-ref)
        (let [d (try (deref fn-ref)
                     (catch Throwable _ nil))]
          (some-> (get d :static/application)
                  app-name-from-static)))))

(defn- namespaced-symbol?
  [sym]
  (boolean (and (symbol? sym)
                (namespace sym))))

(defn- resolve-by-name
  [op-name]
  (or (types/get-type (symbol op-name))
      (first
       (filter (fn [f]
                 (and (types/fn-def? f)
                      (= op-name (:name f))))
               (vals @types/*type-registry*)))))

(defn resolve-called-fn
  [op aliases]
  (let [op-name (name op)
        op-str (str op)
        resolved-op (if (str/includes? op-str "/")
                      (let [[alias-part fn-part] (str/split op-str #"/")
                             alias-sym (symbol alias-part)]
                         (if-let [full-ns (get aliases alias-sym)]
                           (symbol (str full-ns "/" fn-part))
                           op))
                       op)
        qualified? (or (namespaced-symbol? op)
                       (namespaced-symbol? resolved-op))
        fn-def (or (types/get-type resolved-op)
                   (types/get-type op)
                   (when-not qualified?
                     (resolve-by-name op-name)))]
    [resolved-op fn-def]))

(defn resolve-function-def
  [fn-ref]
  (cond
    (types/fn-def? fn-ref)
    fn-ref

    (types/fn-def? (types/get-type fn-ref))
    (types/get-type fn-ref)

    :else
    (when-let [fn-sym (fn-ref->fn-sym fn-ref)]
      (or (some-> (types/get-type fn-sym))
          (types/get-type (symbol (name fn-sym)))
          (types/get-type (keyword (name fn-sym)))))))
