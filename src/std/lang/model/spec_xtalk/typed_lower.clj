(ns std.lang.model.spec-xtalk.typed-lower)

(def +intrinsic-ns+ "std.lang.model.spec-xtalk.typed-intrinsic")

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
        args' (map lower-form args)
        lowered (cons op' args')]
    (case op'
      . (lower-dot lowered)
      :? (list 'if (first args') (second args') (nth args' 2 nil))
      fn:> (lower-fn-shorthand lowered)
      xt.lang.base-lib/get-key (list 'x:get-key (first args') (second args') (nth args' 2 nil))
      xt.lang.base-lib/get-in (list 'x:get-path (first args') (second args') (nth args' 2 nil))
      xt.lang.base-lib/nil? (list 'x:nil? (first args'))
      xt.lang.base-lib/cat (list* 'x:cat args')
      xt.lang.base-lib/obj-assign (cons (intrinsic-sym "obj-assign") args')
      xt.lang.base-lib/arrayify (cons (intrinsic-sym "arrayify") args')
      xt.lang.base-lib/obj-keys (cons (intrinsic-sym "obj-keys") args')
      xt.lang.base-lib/json-encode (cons (intrinsic-sym "json-encode") args')
      xt.lang.base-lib/split (cons (intrinsic-sym "split") args')
      xt.lang.base-lib/arr-join (cons (intrinsic-sym "arr-join") args')
      xt.lang.base-lib/not-empty? (cons (intrinsic-sym "not-empty?") args')
      xt.lang.base-lib/is-empty? (cons (intrinsic-sym "is-empty?") args')
      xt.lang.base-lib/fn? (cons (intrinsic-sym "fn?") args')
      xt.lang.base-lib/first (cons (intrinsic-sym "first") args')
      xt.lang.base-lib/second (cons (intrinsic-sym "second") args')
      xt.lang.event-common/make-container (cons (intrinsic-sym "make-container") args')
      xt.lang.event-common/blank-container (cons (intrinsic-sym "blank-container") args')
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
