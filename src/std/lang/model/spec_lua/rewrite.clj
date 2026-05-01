(ns std.lang.model.spec-lua.rewrite
  (:require [std.lang.rewrite.hoist :as hoist]
            [std.lang.rewrite.fn :as fnrw]
            [std.lang.rewrite.walk :as walk]
            [std.lib.collection :as collection]))

(defn- lua-lambda-compatible?
  [form _grammar]
  (let [[name] (fnrw/fn-parts form)]
    (nil? name)))

(def ^:private +lua-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "lua_callback__"
    :lambda-compatible? lua-lambda-compatible?}))

(def lua-rewrite-expression
  (:rewrite-expression +lua-rewriter+))

(def ^:private with-form-meta
  walk/with-form-meta)

(def ^:dynamic *lua-grammar* nil)

(defn- runtime-eval?
  [{:keys [mopts]}]
  (boolean (get-in mopts [:emit :body :transform])))

(declare lua-rewrite-statement)
(declare lua-rewrite-statements)
(declare lua-rewrite-form)

(defn- lua-handler-form?
  [tag form]
  (and (collection/form? form)
       (= tag (first form))))

(defn- lua-find-handler
  [tag handlers]
  (first (filter #(lua-handler-form? tag %) handlers)))

(defn- lua-catch-binding
  [binding]
  (cond
    (symbol? binding)
    binding

    (and (vector? binding)
         (symbol? (last binding)))
    (last binding)

    :else
    (gensym "lua_try_err__")))

(defn- lua-try-thunk
  [body]
  (apply list 'fn [] body))

(defn- lua-mark-inline-def
  [form]
  (if (and (collection/form? form)
           (= 'defn (first form))
           (symbol? (second form)))
    (apply list 'defn
           (with-meta (second form)
             (assoc (meta (second form)) :inner true))
           (drop 2 form))
    form))

(defn- lua-mark-runtime-defs
  [form]
  (cond
    (vector? form)
    (with-form-meta form (mapv lua-mark-inline-def form))

    (and (collection/form? form)
         (#{'do 'do*} (first form)))
    (with-form-meta form
      (apply list (first form)
             (map lua-mark-inline-def (rest form))))

    :else
    (lua-mark-inline-def form)))

(defn- lua-pcall-bind
  [bindings thunk]
  (list 'var (list 'quote bindings) (list 'pcall thunk)))

(defn- lua-return-if-value
  [value]
  (list 'when (list 'not= nil value)
        (list 'return value)))

(defn- lua-return-or-error
  [ok value]
  (list 'if ok
        (lua-return-if-value value)
        (list 'error value)))

(defn- lua-catch-guard
  [body-ok body-value]
  (list 'not body-ok))

(defn- lua-catch-branch
  [{:keys [body-ok body-value ok value catch-ok catch-value catch-sym catch-body]}]
  (let [catch-thunk (lua-try-thunk
                     (concat [(list 'var catch-sym ':= body-value)]
                             catch-body))]
    (list 'when
          (lua-catch-guard body-ok body-value)
          (lua-pcall-bind [catch-ok catch-value] catch-thunk)
          (list ':= ok catch-ok)
          (list ':= value catch-value))))

(defn- lua-finally-branch
  [{:keys [final-ok final-value finally-body]}]
  (let [final-thunk (lua-try-thunk finally-body)]
    [(lua-pcall-bind [final-ok final-value] final-thunk)
     (list 'if (list 'not final-ok)
           (list 'error final-value)
           (lua-return-if-value final-value))]))

(defn- lua-lower-try
  [form]
  (let [[_ & items] form
        [body handlers] (split-with (fn [x]
                                      (not (or (lua-handler-form? 'catch x)
                                               (lua-handler-form? 'finally x))))
                                    items)
        catch-form    (lua-find-handler 'catch handlers)
        finally-form  (lua-find-handler 'finally handlers)
        body         (lua-rewrite-statements body)
        catch-sym    (when catch-form
                       (lua-catch-binding (second catch-form)))
        catch-body   (when catch-form
                       (lua-rewrite-statements (drop 2 catch-form)))
        finally-body (when finally-form
                       (lua-rewrite-statements (rest finally-form)))
        body-ok      (gensym "lua_try_body_ok__")
        body-value   (gensym "lua_try_body_value__")
        ok           (gensym "lua_try_ok__")
        value        (gensym "lua_try_value__")
        catch-ok     (gensym "lua_try_catch_ok__")
        catch-value  (gensym "lua_try_catch_value__")
        final-ok     (gensym "lua_try_final_ok__")
        final-value  (gensym "lua_try_final_value__")
        body-thunk    (lua-try-thunk body)
        catch-branch  (when catch-form
                        (lua-catch-branch {:body-ok body-ok
                                           :body-value body-value
                                           :ok ok
                                           :value value
                                           :catch-ok catch-ok
                                           :catch-value catch-value
                                           :catch-sym catch-sym
                                           :catch-body catch-body}))
        finally-forms (when finally-form
                        (lua-finally-branch {:final-ok final-ok
                                             :final-value final-value
                                             :finally-body finally-body}))]
    (with-form-meta
      form
      (apply list 'do
             (concat
              [(lua-pcall-bind [body-ok body-value] body-thunk)
               (list 'var ok ':= body-ok)
               (list 'var value ':= body-value)]
              (if catch-branch [catch-branch] [])
              (or finally-forms [])
              [(lua-return-or-error ok value)])))))

(defn- lua-rewrite-list
  [form]
  (cond
    (not (collection/form? form))
    form

    (= 'quote (first form))
    form

    (= 'try (first form))
    (lua-lower-try form)

    :else
    (with-form-meta form
      (apply list (map lua-rewrite-form form)))))

(defn lua-rewrite-form
  [form]
  (walk/rewrite-form form lua-rewrite-list lua-rewrite-form))

(defn lua-rewrite-statement
  [form]
  (lua-rewrite-form ((:rewrite-statement +lua-rewriter+) form *lua-grammar*)))

(defn lua-rewrite-statements
  [forms]
  (map lua-rewrite-statement forms))

(defn lua-rewrite-stage
  [form {:keys [grammar] :as opts}]
  (binding [*lua-grammar* grammar]
    (let [form (lua-rewrite-form ((:rewrite-stage +lua-rewriter+) form opts))]
      (if (runtime-eval? opts)
        (lua-mark-runtime-defs form)
        form))))
