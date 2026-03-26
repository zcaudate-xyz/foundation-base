(ns std.lang.typed.xtalk-lower
  (:require [std.lang.typed.xtalk-ops :as ops]))

(def +intrinsic-ns+ "std.lang.typed.xtalk-intrinsic")

(defn intrinsic-sym
  [name]
  (symbol +intrinsic-ns+ name))

(defn resolve-op
  [op {:keys [ns aliases]}]
  (cond
    (not (symbol? op))
    op

    (= "-" (namespace op))
    (symbol (str ns) (name op))

    (namespace op)
    (if-let [alias-ns (get aliases (symbol (namespace op)))]
      (symbol (str alias-ns) (name op))
      op)

    :else
    op))

(defn lower-dot
  [[_ obj & path-parts]]
  (let [path (mapv (fn [part]
                     (if (and (vector? part) (= 1 (count part)))
                       (first part)
                       part))
                   path-parts)]
    (if (= 1 (count path))
      (list 'x:get-key obj (first path))
      (list 'x:get-path obj path nil))))

(defn lower-fn-shorthand
  [[_ & args]]
  (cond
    (empty? args)
    (list (intrinsic-sym "const-fn") nil)

    (vector? (first args))
    (list* 'fn (first args) (rest args))

    :else
    (list (intrinsic-sym "const-fn") (first args))))

(declare lower-form)

(defn lower-list
  [form ctx]
  (let [[op & args] form
        op' (resolve-op op ctx)
        canonical-op (ops/canonical-symbol op')
        args' (map #(lower-form % ctx) args)
        lowered (cons canonical-op args')]
    (cond
      (= op' '.)
      (lower-dot (cons op' args'))

      (= op' :?)
      (list 'if (first args') (second args') (nth args' 2 nil))

      (= op' 'fn:>)
      (lower-fn-shorthand (cons op' args'))

      (= op' 'xt.lang.base-lib/get-key)
      (list 'x:get-key (first args') (second args') (nth args' 2 nil))

      (= op' 'xt.lang.base-lib/get-in)
      (list 'x:get-path (first args') (second args') (nth args' 2 nil))

      (= op' 'xt.lang.base-lib/nil?)
      (list 'x:nil? (first args'))

      (= op' 'xt.lang.base-lib/cat)
      (list* 'x:cat args')

      (= op' 'xt.lang.base-lib/json-encode)
      (cons 'x:json-encode args')

      (= op' 'xt.lang.base-lib/split)
      (cons 'x:str-split args')

      (= op' 'xt.lang.base-lib/arr-join)
      (list 'x:str-join (second args') (first args'))

      (= op' 'xt.lang.base-lib/fn?)
      (cons 'x:is-function? args')

      (= op' 'xt.lang.base-lib/first)
      (list 'x:get-idx (first args') '(x:offset))

      (= op' 'xt.lang.base-lib/second)
      (list 'x:get-idx (first args') '(x:offset 1))

      (= op' 'xt.lang.base-lib/arrayify)
      (cons (intrinsic-sym "arrayify") args')

      (= op' 'xt.lang.base-lib/not-empty?)
      (cons (intrinsic-sym "not-empty?") args')

      (= op' 'xt.lang.base-lib/is-empty?)
      (cons (intrinsic-sym "is-empty?") args')

      (= op' 'xt.lang.event-common/make-container)
      (cons (intrinsic-sym "make-container") args')

      (= op' 'xt.lang.event-common/blank-container)
      (cons (intrinsic-sym "blank-container") args')

      :else
      lowered)))

(defn lower-form
  [form ctx]
  (cond
    (seq? form)
    (lower-list form ctx)

    (vector? form)
    (mapv #(lower-form % ctx) form)

    (map? form)
    (into (empty form)
          (map (fn [[k v]]
                 [(lower-form k ctx) (lower-form v ctx)]))
          form)

    :else
    form))
