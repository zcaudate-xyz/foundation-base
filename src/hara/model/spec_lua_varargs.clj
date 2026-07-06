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

(defn prepare-body
  [args body _ _]
  (let [rest-args (filterv rest-arg-form? args)]
    (cond
      (empty? rest-args)
      body

      (< 1 (count rest-args))
      (throw (ex-info "Only one rest argument is allowed"
                      {:args args}))

      (not= (last args) (first rest-args))
      (throw (ex-info "Rest argument must be final"
                      {:args args}))

      :else
      (let [rest-sym (second (first rest-args))]
        (cons (list 'var rest-sym [(list ':- "...")]) body)))))
