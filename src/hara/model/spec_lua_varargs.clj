(ns hara.model.spec-lua-varargs
  (:require [std.lib.collection :as collection]))

(defn emit-input-rest
  [_ _ _]
  "...")

(defn rest-arg-form?
  [form]
  (and (collection/form? form)
       (= 2 (count form))
       (= :.. (first form))
       (symbol? (second form))))

(defn- prepare-function
  [form]
  (let [[tag head & tail] form
        named?           (symbol? head)
        args             (if named? (first tail) head)
        body             (if named? (rest tail) tail)
        rest-args        (filterv rest-arg-form? args)]
    (cond
      (empty? rest-args)
      form

      (< 1 (count rest-args))
      (throw (ex-info "Only one rest argument is allowed"
                      {:form form :args args}))

      (not= (last args) (first rest-args))
      (throw (ex-info "Rest argument must be final"
                      {:form form :args args}))

      :else
      (let [rest-sym (second (first rest-args))
            body     (cons (list 'var rest-sym [(list ':- "...")]) body)]
        (with-meta
          (if named?
            (apply list tag head args body)
            (apply list tag args body))
          (meta form))))))

(declare prepare-form)

(defn- prepare-coll
  [form]
  (cond
    (vector? form)
    (with-meta (mapv prepare-form form) (meta form))

    (map? form)
    (with-meta (into (empty form)
                     (map (fn [[k v]]
                            [(prepare-form k) (prepare-form v)]))
                     form)
      (meta form))

    (set? form)
    (with-meta (set (map prepare-form form)) (meta form))

    :else
    form))

(defn prepare-form
  [form]
  (cond
    (and (collection/form? form)
         (= 'quote (first form)))
    form

    (collection/form? form)
    (let [form* (with-meta (apply list (map prepare-form form)) (meta form))]
      (if (contains? '#{fn fn.inner defn defn- defgen} (first form*))
        (prepare-function form*)
        form*))

    (or (vector? form) (map? form) (set? form))
    (prepare-coll form)

    :else
    form))
