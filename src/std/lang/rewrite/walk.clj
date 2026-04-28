(ns std.lang.rewrite.walk
  (:require [std.lang.rewrite.common :as common]
            [std.lib.collection :as collection]))

(def with-form-meta
  common/with-form-meta)

(defn rewrite-coll
  [items rewrite-item]
  (map rewrite-item items))

(defn rewrite-map-entry
  [[k v] rewrite-item]
  [(rewrite-item k)
   (rewrite-item v)])

(defn rewrite-map
  [form rewrite-item]
  (into (empty form)
        (map #(rewrite-map-entry % rewrite-item))
        form))

(defn rewrite-vector
  [form rewrite-item]
  (common/with-form-meta form
    (vec (rewrite-coll form rewrite-item))))

(defn rewrite-set
  [form rewrite-item]
  (common/with-form-meta form
    (set (rewrite-coll form rewrite-item))))

(defn rewrite-map-form
  [form rewrite-item]
  (common/with-form-meta form
    (rewrite-map form rewrite-item)))

(defn rewrite-form
  [form rewrite-list rewrite-item]
  (cond
    (collection/form? form)
    (rewrite-list form)

    (vector? form)
    (rewrite-vector form rewrite-item)

    (set? form)
    (rewrite-set form rewrite-item)

    (map? form)
    (rewrite-map-form form rewrite-item)

    :else
    form))

(defn rewrite-binding-vector
  [binding rewrite-item]
  (if (and (vector? binding)
           (<= 2 (count binding)))
      (let [[lhs rhs & more] binding]
      (common/with-form-meta
        binding
        (vec (concat [lhs (rewrite-item rhs)]
                     more))))
    binding))
