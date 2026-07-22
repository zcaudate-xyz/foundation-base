(ns hara.typed.xtalk-lower
  (:require [hara.typed.xtalk-intrinsic :as intrinsic]
            [hara.typed.xtalk-compat :as compat]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-ops :as ops]))

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

(defn access-kind
  "returns the canonical access kind for a receiver type"
  {:added "4.1"}
  [type ctx]
  (let [type (if (and (map? type)
                      (contains? type :kind))
               type
               (types/normalize-type type ctx))
        type (compat/resolve-type type ctx)]
    (case (:kind type)
      (:array :tuple) :idx
      (:record :dict) :key
      :maybe (access-kind (:item type) ctx)
      :union (let [kinds (->> (:types type)
                              (map #(access-kind % ctx))
                              distinct)]
               (when (= 1 (count kinds))
                 (first kinds)))
      nil)))

(defn- receiver-access-kind
  [obj ctx]
  (or (when-let [infer (:infer ctx)]
        (access-kind (:type (infer obj ctx)) ctx))
      (when (symbol? obj)
        (when-let [declared (or (get (meta obj) :-)
                                (get (meta obj) :hara/type))]
          (access-kind declared ctx)))))

(defn lower-dot
  "lowers dot access using the receiver's XTalk type when available

   Single-segment access becomes x:get-key or x:get-idx. Multi-segment access
   becomes x:get-path, which is direct chained path access; it is not the
   guarded traversal provided by xt.lang.common-data/get-in."
  {:added "4.1"}
  ([form]
   (lower-dot form nil))
  ([[_ obj & path-parts] ctx]
   (let [path (mapv (fn [part]
                      (if (and (vector? part) (= 1 (count part)))
                        (first part)
                        part))
                    path-parts)]
     (if (= 1 (count path))
       (case (receiver-access-kind obj ctx)
         :idx (list 'x:get-idx obj (first path))
         :key (list 'x:get-key obj (first path))
         (if (:preserve-unknown ctx)
           (list* '. obj path-parts)
           (list 'x:get-key obj (first path))))
       (if (:preserve-unknown ctx)
         (list* '. obj path-parts)
         (list 'x:get-path obj path nil))))))

(defn lower-fn-shorthand
  [[_ & args]]
  (cond
    (empty? args)
    (list (intrinsic-sym "const-fn") nil)

    (vector? (first args))
    (list* 'fn (first args) (rest args))

    :else
    (list (intrinsic-sym "const-fn")
          (first args))))

(declare lower-form)

(def +wrapper-targets+
  {'xt.lang.common-lib/nil? 'x:nil?
    'xt.lang.common-lib/cat 'x:cat
    'xt.lang.common-lib/json-decode 'x:json-decode
    'xt.lang.common-lib/json-encode 'x:json-encode
    'xt.lang.common-lib/split 'x:str-split
    'xt.lang.common-lib/fn? 'x:is-function?
    'xt.lang.common-lib/arr? 'x:is-array?
    'xt.lang.common-lib/obj? 'x:is-object?})

(def +intrinsic-targets+
  {'xt.lang.common-lib/arrayify "arrayify"
   'xt.lang.common-lib/not-empty? "not-empty?"
   'xt.lang.common-lib/is-empty? "is-empty?"
   'xt.event.base-listener/make-container "make-container"
   'xt.event.base-listener/blank-container "blank-container"})

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
         canonical-entry (when (and (symbol? op')
                                    (namespace op'))
                           (ops/canonical-entry op'))
         canonical-op (or (:canonical-symbol canonical-entry)
                          op')
         args' (map #(lower-form % ctx) args)
         lowered (cons canonical-op args')]
    (cond
      (= op' '.)
      (lower-dot (cons op' args') ctx)

      (= op' 'fn:>)
      (lower-fn-shorthand (cons op' args'))

      (= canonical-op 'x:get-key)
      (lower-defaulted-target 'x:get-key args')

      (or (= op' 'xt.lang.common-lib/get-in)
          (= canonical-op 'x:get-path))
      (lower-defaulted-target 'x:get-path args')

      (= op' 'xt.lang.common-lib/arr-join)
      (list 'x:str-join (second args') (first args'))

      (or (= op' 'xt.lang.common-lib/first)
          (= canonical-op 'x:first))
      (lower-offset-index args' 0)

      (or (= op' 'xt.lang.common-lib/second)
          (= canonical-op 'x:second))
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
