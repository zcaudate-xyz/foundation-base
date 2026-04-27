(ns std.lang.model-annex.spec-r.rewrite
  (:require [std.lang.rewrite.lift-named-lambda :as lift]
            [std.lib.collection :as collection]))

(def ^:private +r-function-tags+
  #{'fn
    'fn.inner
    'defn
    'defn-
    'defgen})

(declare r-rewrite-expression)
(declare r-rewrite-statement)
(declare r-rewrite-statements)
(declare r-rewrite-inline-do)

(defn- with-form-meta
  [source out]
  (lift/with-form-meta source out))

(defn- function-form?
  [form]
  (and (collection/form? form)
       (contains? +r-function-tags+ (first form))))

(defn- iterator-symbol
  [sym]
  (gensym (str (name (or sym 'r_generator))
               "__iter__")))

(defn- do-expression?
  [form]
  (and (collection/form? form)
       (#{'do 'do*} (first form))))

(defn- unpack-form?
  [form]
  (and (collection/form? form)
       (= 'x:unpack (first form))
       (= 2 (count form))))

(defn- rewrite-yield
  [form iterator]
  (if-not iterator
    form
    (let [[_ value] form]
      (with-form-meta
        form
        (list 'x:arr-push
              iterator
              (r-rewrite-expression value iterator))))))

(defn- rewrite-map-entry
  [[k v] iterator]
  [(r-rewrite-expression k iterator)
   (r-rewrite-expression v iterator)])

(defn- rewrite-invoke-args
  [args iterator]
  (reduce (fn [acc arg]
            (let [segment (if (unpack-form? arg)
                            (list 'as.list
                                  (r-rewrite-expression (second arg) iterator))
                            (list 'list
                                  (r-rewrite-expression arg iterator)))]
              (list 'append acc segment)))
          []
          args))

(defn- rewrite-invoke-expression
  [form iterator]
  (let [head    (first form)
        head*   (r-rewrite-expression head iterator)
        args    (rest form)
        unpack? (some unpack-form? args)]
    (with-form-meta
      form
      (if unpack?
        (list 'do.call head* (rewrite-invoke-args args iterator))
        (apply list head* (map #(r-rewrite-expression % iterator) args))))))

(defn- destructure-target?
  [form]
  (and (set? form)
       (seq form)
       (every? symbol? form)))

(defn- destructure-symbols
  [target]
  (sort-by name target))

(defn- destructure-value
  [temp sym]
  (list 'x:get-key temp (name sym) nil))

(defn- rewrite-let-bindings
  [bindings iterator]
  (->> (partition 2 bindings)
       (mapcat (fn [[target value]]
                 (let [value (r-rewrite-expression value iterator)]
                   (if (destructure-target? target)
                     (let [temp (gensym "r_destructure__")]
                       (concat [temp value]
                               (mapcat (fn [sym]
                                         [sym (destructure-value temp sym)])
                                       (destructure-symbols target))))
                     [target value]))))
       vec))

(defn r-rewrite-expression
  [form iterator]
  (cond
    (and (collection/form? form)
         (= 'var (first form)))
    (with-form-meta
      form
      (apply list (map #(r-rewrite-expression % iterator) form)))

    (and (collection/form? form)
         (#{'let 'let*} (first form)))
    (let [[head bindings & body] form]
      (with-form-meta
        form
        (apply list head
               (concat [(rewrite-let-bindings bindings iterator)]
                       (map #(r-rewrite-statement % iterator) body)))))

    (and iterator
         (collection/form? form)
         (= 'yield (first form)))
    (rewrite-yield form iterator)

    (and (collection/form? form)
         (= 'quote (first form)))
    form

    (and (collection/form? form)
         (function-form? form))
    form

    (collection/form? form)
    (rewrite-invoke-expression form iterator)

    (vector? form)
    (with-form-meta
      form
      (mapv #(r-rewrite-expression % iterator) form))

    (set? form)
    (with-form-meta
      form
      (set (map #(r-rewrite-expression % iterator) form)))

    (map? form)
    (with-form-meta
      form
      (into (empty form)
            (map #(rewrite-map-entry % iterator))
            form))

    :else
    form))

(defn- rewrite-defgen
  [form]
  (let [[_ sym args & body] form
        iterator (iterator-symbol sym)]
    (with-form-meta
      form
      (apply list 'defn sym args
             (concat [(list 'var iterator (list 'list))]
                     (r-rewrite-statements body iterator)
                     [(list 'return
                            (list 'x:iter-from-arr iterator))])))))

(defn r-rewrite-statement
  [form iterator]
  (if (and (collection/form? form)
           (= 'defgen (first form)))
    (rewrite-defgen form)
    (r-rewrite-expression form iterator)))

(defn r-rewrite-statements
  [forms iterator]
  (map #(r-rewrite-statement % iterator) forms))

(defn- r-rewrite-inline-do-list
  [form]
  (let [rewritten (with-form-meta form
                    (apply list (map r-rewrite-inline-do form)))]
    (if (and (= 'return (first rewritten))
             (= 2 (count rewritten))
             (do-expression? (second rewritten)))
      (let [expr (second rewritten)
            body (rest expr)]
        (with-form-meta
          form
          (apply list 'do*
                 (concat (butlast body)
                         [(with-form-meta
                            rewritten
                            (list 'return (last body)))]))))
      rewritten)))

(defn- r-rewrite-inline-do
  [form]
  (cond
    (and (collection/form? form)
         (= 'quote (first form)))
    form

    (collection/form? form)
    (r-rewrite-inline-do-list form)

    (vector? form)
    (with-form-meta form (vec (map r-rewrite-inline-do form)))

    (set? form)
    (with-form-meta form (set (map r-rewrite-inline-do form)))

    (map? form)
    (with-form-meta
      form
      (into (empty form)
            (map (fn [[k v]]
                   [(r-rewrite-inline-do k)
                    (r-rewrite-inline-do v)]))
            form))

    :else
    form))

(defn r-rewrite-stage
  [form _opts]
  (-> (cond
        (do-expression? form)
        (with-form-meta
          form
          (apply list 'do*
                 (map #(r-rewrite-statement % nil) (rest form))))

        (collection/form? form)
        (r-rewrite-statement form nil)

        (vector? form)
        (with-form-meta
          form
          (mapv #(r-rewrite-statement % nil) form))

        :else
        form)
      (r-rewrite-inline-do)))
