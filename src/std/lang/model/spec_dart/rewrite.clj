(ns std.lang.model.spec-dart.rewrite
  (:require [std.lib.collection :as collection]))

(defn- preserve-meta
  [orig out]
  (if-let [m (meta orig)]
    (with-meta out m)
    out))

(declare rewrite-form)

(defn- rewrite-coll
  [orig ctor xs]
  (preserve-meta orig (ctor (map rewrite-form xs))))

(defn rewrite-form
  [form]
  (cond (and (collection/form? form)
             (seq form)
             (= 'quote (first form)))
        form

        (collection/form? form)
        (let [tag  (first form)
              args (map rewrite-form (rest form))
              out  (case tag
                     or (apply list 'dart:or args)
                     :? (apply list 'dart:ternary args)
                     (apply list tag args))]
          (preserve-meta form out))

        (vector? form)
        (rewrite-coll form vec form)

        (set? form)
        (rewrite-coll form set form)

        (map? form)
        (preserve-meta form
                       (into (empty form)
                             (map (fn [[k v]]
                                    [(rewrite-form k) (rewrite-form v)]))
                             form))

        :else
        form))

(defn dart-rewrite-stage
  [form _]
  (rewrite-form form))
