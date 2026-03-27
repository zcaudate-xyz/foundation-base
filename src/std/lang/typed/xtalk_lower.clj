(ns std.lang.typed.xtalk-lower
  (:require [std.lang.typed.xtalk-intrinsic :as intrinsic]
            [std.lang.typed.xtalk-ops :as ops]))

(defn intrinsic-sym
  [name]
  (intrinsic/intrinsic-sym name))

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

(def +wrapper-targets+
  {'xt.lang.base-lib/nil? 'x:nil?
   'xt.lang.base-lib/cat 'x:cat
   'xt.lang.base-lib/json-encode 'x:json-encode
   'xt.lang.base-lib/split 'x:str-split
   'xt.lang.base-lib/fn? 'x:is-function?})

(def +intrinsic-targets+
  {'xt.lang.base-lib/arrayify "arrayify"
   'xt.lang.base-lib/not-empty? "not-empty?"
   'xt.lang.base-lib/is-empty? "is-empty?"
   'xt.lang.event-common/make-container "make-container"
   'xt.lang.event-common/blank-container "blank-container"})

(defn lower-defaulted-target
  [target args]
  (list target (first args) (second args) (nth args 2 nil)))

(defn lower-offset-index
  [args offset]
  (if (zero? offset)
    (list 'x:get-idx (first args) '(x:offset))
    (list 'x:get-idx (first args) (list 'x:offset offset))))

(defn lower-list
  [form ctx]
  (let [[op & args] form
         op' (resolve-op op ctx)
         canonical-entry (when (symbol? op')
                           (ops/canonical-entry op'))
         canonical-op (or (:canonical-symbol canonical-entry)
                          op')
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
      (lower-defaulted-target 'x:get-key args')

      (= op' 'xt.lang.base-lib/get-in)
      (lower-defaulted-target 'x:get-path args')

      (= op' 'xt.lang.base-lib/arr-join)
      (list 'x:str-join (second args') (first args'))

      (= op' 'xt.lang.base-lib/first)
      (lower-offset-index args' 0)

      (= op' 'xt.lang.base-lib/second)
      (lower-offset-index args' 1)

      (contains? +wrapper-targets+ op')
      (cons (get +wrapper-targets+ op') args')

      (contains? +intrinsic-targets+ op')
      (cons (intrinsic-sym (get +intrinsic-targets+ op')) args')

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
