(ns std.lang.model-annex.spec-r.rewrite
  (:require [std.lang.rewrite.common :as common]
            [std.lang.rewrite.destructure :as destruct]
            [std.lang.rewrite.inline-do :as inline]
            [std.lang.rewrite.unpack :as unpack]
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

(defn- with-form-meta
  [source out]
  (common/with-form-meta source out))

(defn- function-form?
  [form]
  (and (collection/form? form)
       (contains? +r-function-tags+ (first form))))

(defn- iterator-symbol
  [sym]
  (gensym (str (name (or sym 'r_generator))
               "__iter__")))

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
  (reduce (fn [acc segment]
            (list 'append acc segment))
          []
          (unpack/rewrite-args args
                               #(r-rewrite-expression % iterator)
                               #(list 'list %)
                               #(list 'as.list %))))

(defn- rewrite-invoke-expression
  [form iterator]
  (let [head    (first form)
        head*   (r-rewrite-expression head iterator)
        args    (rest form)
        unpack? (unpack/any-unpack? args)]
    (with-form-meta
      form
      (if unpack?
        (list 'do.call head* (rewrite-invoke-args args iterator))
        (apply list head* (map #(r-rewrite-expression % iterator) args))))))

(defn- rewrite-let-bindings
  [bindings iterator]
  (->> (partition 2 bindings)
       (mapcat (fn [[target value]]
                  (let [value (r-rewrite-expression value iterator)]
                    (if (destruct/destructure-target? target)
                      (let [temp (gensym "r_destructure__")]
                        (concat [temp value]
                                (mapcat identity
                                        (destruct/destructure-bindings target temp))))
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

(defn r-rewrite-stage
  [form _opts]
  (-> (cond
        (inline/do-expression? form)
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
      (inline/rewrite-inline-do)))
