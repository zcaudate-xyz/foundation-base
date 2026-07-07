(ns hara.model.annex.spec-php.rewrite
  (:require [hara.lang.rewrite.common :as common]
            [std.lib.collection :as collection]))

(defn php-local-symbol?
  "Returns true when `sym` should be emitted as a PHP local variable."
  {:added "4.1"}
  [sym]
  (and (symbol? sym)
       (nil? (namespace sym))
       (let [n (name sym)]
         (and (not (.startsWith ^String n "$"))
              (not (.startsWith ^String n ":"))
              (not (.startsWith ^String n "-"))
              (not (.startsWith ^String n "!"))
              (not (.startsWith ^String n "__"))))))

(defn php-prefix-local
  "Prefixes a local symbol for PHP emission."
  {:added "4.1"}
  [sym]
  (if (php-local-symbol? sym)
    (symbol (str "$" (name sym)))
    sym))

(defn- scoped-symbol
  [sym scope]
  (if (contains? scope sym)
    (php-prefix-local sym)
    sym))

(defn- local-binding-symbols
  [binding]
  (cond
    (php-local-symbol? binding)
    #{binding}

    (vector? binding)
    (->> binding
         (filter php-local-symbol?)
         set)

    :else
    #{}))

(defn- rest-arg-form?
  [form]
  (and (collection/form? form)
       (= 2 (count form))
       (= :.. (first form))
       (symbol? (second form))))

(defn- param-local-symbols
  [param]
  (cond
    (php-local-symbol? param)
    #{param}

    (rest-arg-form? param)
    #{(second param)}

    :else
    #{}))

(defn- rewrite-param
  [param]
  (if (rest-arg-form? param)
    (common/with-form-meta
      param
      (list :.. (php-prefix-local (second param))))
    (php-prefix-local param)))

(declare php-rewrite-form*)
(declare php-rewrite-statements*)

(defn- rewrite-coll*
  [form scope]
  (cond
    (vector? form)
    (common/with-form-meta form
      (mapv #(php-rewrite-form* % scope) form))

    (set? form)
    (common/with-form-meta form
      (set (map #(php-rewrite-form* % scope) form)))

    (map? form)
    (common/with-form-meta form
      (into (empty form)
            (map (fn [[k v]]
                   [(php-rewrite-form* k scope)
                    (php-rewrite-form* v scope)]))
            form))

    :else
    form))

(defn- rewrite-fn*
  [form scope]
  (let [[tag head & tail] form
        [name params body] (if (symbol? head)
                             [head (first tail) (rest tail)]
                             [nil head tail])
        param-locals      (into #{} (mapcat param-local-symbols) params)
        scope*            (into scope param-locals)
        params*           (mapv rewrite-param params)
        body*             (php-rewrite-statements* body scope*)]
    (common/with-form-meta
      form
      (if name
        (apply list tag name params* body*)
        (apply list tag params* body*)))))

(defn- rewrite-defn*
  [form scope]
  (let [[tag name params & body] form
        param-locals (into #{} (mapcat param-local-symbols) params)
        scope*       (into scope param-locals)]
    (common/with-form-meta
      form
      (apply list tag name
             (mapv rewrite-param params)
             (php-rewrite-statements* body scope*)))))

(defn- rewrite-let*
  [form scope]
  (let [[tag bindings & body] form
        [bindings* scope*]
        (loop [[[binding value] & more] (partition 2 bindings)
               out   []
               scope scope]
          (if binding
            (let [locals   (local-binding-symbols binding)
                  binding* (if (php-local-symbol? binding)
                             (php-prefix-local binding)
                             binding)]
              (recur more
                     (conj out binding* (php-rewrite-form* value scope))
                     (into scope locals)))
            [(vec out) scope]))]
    (common/with-form-meta
      form
      (apply list tag bindings* (php-rewrite-statements* body scope*)))))

(defn- rewrite-var*
  [form scope]
  (let [[tag target & more] form
        locals  (local-binding-symbols target)
        target* (if (php-local-symbol? target)
                  (php-prefix-local target)
                  (php-rewrite-form* target scope))
        more*   (map #(php-rewrite-form* % scope) more)]
    [(common/with-form-meta form (apply list tag target* more*))
     (into scope locals)]))

(defn- rewrite-for*
  [form scope]
  (let [[tag binding & body] form
        [lhs rhs & more] binding
        locals           (local-binding-symbols lhs)
        lhs*             (cond
                           (php-local-symbol? lhs) (php-prefix-local lhs)
                           (vector? lhs)           (mapv php-prefix-local lhs)
                           :else                   lhs)
        binding*         (common/with-form-meta
                           binding
                           (vec (concat [lhs*
                                         (php-rewrite-form* rhs scope)]
                                        more)))
        scope*           (into scope locals)]
    (common/with-form-meta
      form
      (apply list tag binding* (php-rewrite-statements* body scope*)))))

(defn- rewrite-foreach*
  "Handles (foreach [coll var] body...) generated by for:array/for:object macros.
   The bound variable is the second element of the binding vector."
  [form scope]
  (let [[_ [coll var] & body] form
        locals    (local-binding-symbols var)
        binding*  [(php-rewrite-form* coll scope) (php-prefix-local var)]
        scope*    (into scope locals)]
    (common/with-form-meta
      form
      (apply list 'foreach binding* (php-rewrite-statements* body scope*)))))

(defn- rewrite-catch*
  [form scope]
  (let [[tag binding & body] form
        locals           (local-binding-symbols binding)
        binding*         (if (php-local-symbol? binding)
                           (php-prefix-local binding)
                           binding)
        scope*           (into scope locals)]
    (common/with-form-meta
      form
      (apply list tag binding* (php-rewrite-statements* body scope*)))))

(defn- rewrite-list*
  [form scope]
  (let [head (first form)]
    (case head
      quote
      form

      (fn fn.inner)
      (rewrite-fn* form scope)

      (defn defn- defgen)
      (rewrite-defn* form scope)

      (let let*)
      (rewrite-let* form scope)

      (for for:index for:object for:array for:iter for:async)
      (rewrite-for* form scope)

      foreach
      (rewrite-foreach* form scope)

      catch
      (rewrite-catch* form scope)

      (common/with-form-meta
        form
        (apply list
               (if (collection/form? head)
                 (php-rewrite-form* head scope)
                 head)
               (map #(php-rewrite-form* % scope) (rest form)))))))

(defn php-rewrite-form*
  [form scope]
  (cond
    (symbol? form)
    (scoped-symbol form scope)

    (collection/form? form)
    (rewrite-list* form scope)

    (or (vector? form)
        (set? form)
        (map? form))
    (rewrite-coll* form scope)

    :else
    form))

(defn php-rewrite-statements*
  [forms scope]
  (first
   (reduce (fn [[out scope] form]
             (if (and (collection/form? form)
                      (#{'var 'var*} (first form)))
               (let [[form* scope*] (rewrite-var* form scope)]
                 [(conj out form*) scope*])
               [(conj out (php-rewrite-form* form scope)) scope]))
           [[] scope]
           forms)))

(defn php-rewrite-form
  "Rewrites bare lexical locals to PHP `$` locals."
  {:added "4.1"}
  [form]
  (php-rewrite-form* form #{}))

(defn php-rewrite-stage
  "PHP staging rewrite used by the emitter."
  {:added "4.1"}
  [form _]
  (cond
    (and (collection/form? form)
         (= 'do (first form)))
    (common/with-form-meta
      form
      (apply list 'do (php-rewrite-statements* (rest form) #{})))

    (vector? form)
    (common/with-form-meta form
      (vec (php-rewrite-statements* form #{})))

    :else
    (php-rewrite-form form)))
