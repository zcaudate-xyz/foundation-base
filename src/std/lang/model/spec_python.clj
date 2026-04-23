(ns std.lang.model.spec-python
  (:require [std.lang.base.book :as book]
	    [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.script :as script]
            [std.lang.typed.xtalk-analysis :as xtalk-analysis]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-python.rewrite :as rewrite]
            [std.lang.model.spec-xtalk.fn-python :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template])
  (:refer-clojure :exclude [await]))

;;
;; LANG
;;

(defn- python-symbol-global
  [fsym grammar mopts]
  (list '. '(globals) [(helper/emit-symbol-full fsym
                                                (namespace fsym)
                                                grammar)]))

(defn- python-token-boolean
  [v]
  (if v "True" "False"))

(def ^:private +python-optional-default-counts+
  '{xt.lang.event-box/add-listener 1
    xt.lang.event-box/get-data 1
    xt.lang.event-route/path-to-tree 1
    xt.lang.event-route/interim-to-tree 1
    xt.lang.event-route/changed-params 1
    xt.lang.event-route/get-param 1
    xt.lang.event-route/get-all-params 1
    xt.lang.event-route/add-url-listener 1
    xt.lang.event-route/add-path-listener 1
    xt.lang.event-route/add-param-listener 1
    xt.lang.event-route/add-full-listener 1
    xt.lang.event-route/set-url 1
    xt.lang.event-route/set-path 2
    xt.lang.event-route/set-param 1
    xt.lang.event-route/reset-route 1
    xt.lang.event-animate/make-linear-indicator 1
    xt.lang.event-animate/make-circular-indicator 1
    xt.lang.event-form/validate-all 2
    xt.lang.event-form/validate-field 1
    xt.lang.event-view/create-view 3
    xt.lang.event-view/get-output 1
    xt.lang.event-view/get-current 1
    xt.lang.event-view/is-errored 1
    xt.lang.event-view/is-pending 1
    xt.lang.event-view/get-time-elapsed 1
    xt.lang.event-view/get-time-updated 1
    xt.lang.event-view/get-success 1
    xt.lang.event-view/set-output 3
    xt.lang.event-view/set-output-disabled 1
    xt.lang.event-view/set-pending 1
    xt.lang.event-view/set-elapsed 1
    xt.lang.event-view/pipeline-prep 1
    xt.lang.event-view/pipeline-set 1
    xt.lang.event-view/pipeline-call 1
    xt.lang.event-view/pipeline-run-impl 1
    xt.lang.event-view/pipeline-run 1
    xt.lang.event-view/get-with-lookup 1
    xt.lang.event-view/sorted-lookup 1
    xt.lang.util-loader/load-tasks-single 3
    xt.lang.util-throttle/throttle-run-async 1
    xt.lang.util-throttle/throttle-run 1
    xt.lang.util-color/rgb->hsl 1})

(defn- python-qualified-symbol
  [sym]
  (let [{:keys [module]} (preprocess/macro-opts)
        module-id (:id module)]
    (cond
      (or (nil? sym)
          (namespace sym)
          (:inner (meta sym))
          (nil? module-id))
      sym

      :else
      (symbol (name module-id) (name sym)))))

(defn- python-optional-input?
  [input]
  (= :maybe (get-in input [:type :kind])))

(defn- python-apply-optional-defaults
  [sym args]
  (if-not (and sym (vector? args))
    args
    (try
      (let [qualified (python-qualified-symbol sym)
            fn-def (xtalk-analysis/resolve-function-def qualified)
            inferred-count (when fn-def
                             (count (take-while python-optional-input?
                                                (reverse (:inputs fn-def)))))
            optional-count (or (when (and inferred-count
                                          (pos? inferred-count))
                                 inferred-count)
                               (get +python-optional-default-counts+ qualified))]
        (if (and optional-count (pos? optional-count))
          (let [optional-args (take-last optional-count args)]
            (if (not (neg? (collection/index-at #{:=} args)))
              args
              (vec
               (concat (drop-last optional-count args)
                       (mapcat (fn [arg]
                                 [arg := nil])
                               optional-args)))))
          args))
      (catch Throwable _
        args))))

(defn- python-lambda-body
  [body]
  (let [body (if (and (collection/form? body)
                      (= 'return (first body)))
               (second body)
               body)]
    (if (or (string? body)
            (keyword? body)
            (nil? body))
      (list 'quote body)
      body)))

(defn python-defn-
  "hidden function without decorators"
  {:added "4.0"}
  [form grammar mopts]
  (let [[tag name args & more] form]
    (top/emit-top-level
     :defn
     (list* tag name (python-apply-optional-defaults name args) more)
     grammar
     mopts)))

(defn python-defn
  "creates a defn function for python"
  {:added "4.0"}
  ([form]
   (let [[_ name args & more] form
         decorators (get (meta name) :decorators)
         body  (list* 'defn- name (python-apply-optional-defaults name args) more)]
     (if (empty? decorators)
       body
       `(\\ ~(apply list
                    \\ (mapcat (fn [d]
                                 [\\ (list :%
                                           (list :- "@")
                                           (if (keyword? d)
                                             (list :- (f/strn d))
                                             d))])
                               decorators))
         \\ ~body)))))

(defn python-fn
  "basic transform for python lambdas"
  {:added "4.0"}
  ([[_ & args]]
   (cond (symbol? (first args))
         (apply list 'fn.inner (with-meta (first args)
                                 {:inner true})
                (rest args))
         
         :else
         (let [[args body] args]
           (apply list :- :lambda
                  (concat (if (not-empty args)
                            [(list 'quote args) ":"]
                            [":"])
                          [(python-lambda-body body)]))))))

(defn python-defclass
  "emits a defclass template for python"
  {:added "4.0"}
  ([[_ sym inherit & body]]
   (let [{:keys [module] :as mopts}  (preprocess/macro-opts)
         body   (top/transform-defclass-inner body)
         name   (symbol (:id module) (name sym))
         supers (list 'quote (remove keyword? inherit))]
     `(:- :class (:% ~name ~supers) \:
          (\\
           \\ (\| (do ~@body))
           \\)))))

(defn python-var
  "var -> fn.inner shorthand"
  {:added "4.0"}
  ([[_ sym & args]]
   (let [bound (last args)]
     (cond (and (collection/form? bound)
                (= 'fn  (first bound)))
           (apply list 'fn.inner (with-meta sym {:inner true})
                  (rest bound))

           (vector? sym)
           (list 'var* (list 'quote sym) := bound)

           (set? sym)
           (cons 'do
                 (map (fn [e]
                        (list 'var* e := (list '. bound (list 'get (ut/sym-default-str e)))))
                      sym))
           
           :else
           (list 'var* sym := bound)))))

(defn tf-for-object
  "for object loop"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (let [[binding method] (cond (= k '_) [v '(values)]
                               (= v '_) [k '(keys)]
                               :else [[k v] '(items)])]
    (apply list 'for [binding :in (list '. m method)]
           (or (not-empty body)
               ['(pass)]))))

(defn tf-for-array
  "for array loop"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (apply list 'for [i :in (list 'range (list 'len arr))]
             (list 'var v (list '. arr [i]))
             (or (not-empty body)
                 ['(pass)])))
    (apply list 'for [e :in arr]
           (or (not-empty body)
               ['(pass)]))))

(defn tf-for-iter
  "for iter loop"
  {:added "4.0"}
  [[_ [e it] & body]]
  (apply list 'for [e :in it]
         (or (not-empty body)
             ['(pass)])))

(defn tf-for-index
  "for index transform"
  {:added "4.0"}
  [[_ [i range] & body]]
  (apply list 'for [i :in (apply list 'range (filter identity range))]
         (or (not-empty body)
             ['(pass)])))

(defn tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (if (and (seq? statement)
           (= 'x:return-run (first statement)))
    (let [[_ runner] statement
          on-ok (gensym "on_ok")
          on-err (gensym "on_err")
          ex (gensym "ex")
          state (gensym "state")]
      (template/$ (do (var ~state {"res" nil
                                   "err" nil})
                      (var ~res nil)
                      (var ~err nil)
                      (fn ~on-ok [value]
                        (:= (. ~state ["res"]) value)
                        (:= (. ~state ["err"]) nil))
                      (fn ~on-err [value]
                        (:= (. ~state ["res"]) nil)
                        (:= (. ~state ["err"]) value))
                      (try
                        (~runner ~on-ok ~on-err)
                        (:= ~res (. ~state ["res"]))
                        (:= ~err (. ~state ["err"]))
                        (if (not= nil ~err)
                          ~(if final (list 'return error) error)
                          ~(if final (list 'return success) success))
                        (catch [Exception :as ~ex]
                            (:= ~err ~ex)
                          ~(if final (list 'return error) error))))))
    (template/$ (try (var ~res ~statement)
                     ~(if final (list 'return success) success)
                     (catch [Exception :as ~err]
                         ~(if final (list 'return error) error))))))

(defn tf-for-try
  "for try transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (let [success-form (or success '(return nil))
        error-form   (or error '(return nil))
        expanded-do  (when (and (seq? statement)
                                (= 1 (count statement))
                                (seq? (first statement))
                                (= 'quote (ffirst statement)))
                       (let [quoted (second (first statement))
                             thunk  (first quoted)]
                         (when (and (seq? thunk)
                                    (= 'fn (first thunk))
                                    (vector? (second thunk)))
                           (drop 2 thunk))))]
    (if (or (and (seq? statement)
                 (= 'do:> (first statement)))
            expanded-do)
      (let [body   (or expanded-do (rest statement))
            runner 'fnthunk]
        (template/$
         (try
           ~(apply list 'fn.inner (with-meta runner {:inner true}) '[] body)
           (var ~res (~runner))
           ~success-form
           (catch [Exception :as ~err]
             ~error-form))))
      (template/$
       (try
         (var ~res ~statement)
         ~success-form
         (catch [Exception :as ~err]
             ~error-form))))))

(defn tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (let [success-form (or success '(return nil))
        error-form   (or error '(return nil))
        runner       (gensym "runner")]
    (if (and (seq? statement)
             (= 'x:return-run (first statement)))
      (let [[_ runner] statement
            on-ok (gensym "on_ok")
            on-err (gensym "on_err")
            ex (gensym "ex")
            state (gensym "state")
            thunk (gensym "runner")]
        (template/$
         (do (fn ~thunk []
               (var ~state {"res" nil
                            "err" nil})
               (fn ~on-ok [value]
                 (:= (. ~state ["res"]) value)
                 (:= (. ~state ["err"]) nil))
               (fn ~on-err [value]
                 (:= (. ~state ["res"]) nil)
                 (:= (. ~state ["err"]) value))
               (try
                 (~runner ~on-ok ~on-err)
                 (:= ~res (. ~state ["res"]))
                 (:= ~err (. ~state ["err"]))
                 (if (not= nil ~err)
                   ~error-form
                   ~success-form)
                 (catch [Exception :as ~ex]
                     (:= ~err ~ex)
                   ~error-form)
                 ~@(if finally
                     [(list 'finally finally)])))
             (x:thread-spawn ~thunk))))
      (template/$
       (do (fn ~runner []
             (try
               (var ~res ~statement)
               ~success-form
               (catch [Exception :as ~err]
                   ~error-form)
               ~@(if finally
                   [(list 'finally finally)])))
            (x:thread-spawn ~runner))))))

(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block])
      (grammar/build:override
       {:pow         {:raw "**"}
        :and         {:raw "and"}
        :or          {:raw "or"}
        :not         {:raw "not" :emit :prefix}
        :throw       {:raw "raise"  :emit :prefix}
        :fn          {:macro  #'python-fn   :emit :macro}
        :var         {:symbol #{'var*}}
        :defn        {:symbol #{'defn}   :macro #'python-defn :emit :macro}
        :defgen      {:symbol #{'defgen} :macro #'python-defn :emit :macro}
        :fn.inner    {:macro #'python-defn :emit :macro}
        :with-global {:value true :raw "globals()"}
        :defclass    {:macro  #'python-defclass :emit :macro}
        :for-object  {:macro #'tf-for-object :emit :macro}
        :for-array   {:macro #'tf-for-array  :emit :macro}
        :for-iter    {:macro #'tf-for-iter   :emit :macro}
        :for-index   {:macro #'tf-for-index  :emit :macro}
        :for-try     {:macro #'tf-for-try    :emit :macro}
        :for-async   {:macro #'tf-for-async  :emit :macro}
        :for-return  {:macro #'tf-for-return :emit :macro}})
      (grammar/build:override fn/+python+)
      (grammar/build:extend
       {:defn-     {:op :defn-   :symbol #{'defn-}  :type :block :emit #'python-defn-}
        :var-let   {:op :var-let :symbol #{'var}  :macro #'python-var :emit :macro}
        :unarr     {:op :unarr   :symbol #{:*}    :raw "*"    :emit :pre}
        :undict    {:op :undict  :symbol #{:**}   :raw "**"   :emit :pre}
        :del       {:op :del     :symbol #{'del}  :raw "del"  :emit :prefix}
        :pass      {:op :pass    :symbol #{'pass} :raw "pass" :emit :return :type :special}
        :nan       {:op :nan     :symbol #{'NaN}  :raw "NaN"  :value true :emit :throw}
        :with      {:op :with    :symbol #{'with} :type :block
                    :block  {:main #{:parameter :body}}}})))

(def +template+
  (->> {:banned #{:keyword}
        :allow   {:assign  #{:symbol :quote}}
        :highlight '#{return break tup with await yield pass raise}
        :default {:comment   {:prefix "#"}
                  :common    {:statement ""}
                  :block     {:parameter {:start " " :end ""}
                              :body      {:start ":" :end "" :append true}}
                  :invoke    {:reversed true
                              :hint ":"}
                  :function  {:raw "lambda"
                              :args      {:start " " :end ":" :space ""}
                              :body      {:start "" :end "" :append true}}
                  :infix     {:if  {:check "and" :then "or"}}
                  :global    {:reference '(globals)}}
        :token   {:nil       {:as "None"}
                  :boolean   {:as #'python-token-boolean}
                  :string    {}
                  :symbol    {:global #'python-symbol-global
                              :namespace {:alias true :link false}}}
        :block    {:try      {:control {:catch  {:raw  "except"
                                                 :args {:start "" :end ":" :space ""}}}}
                   :branch   {:control {:elseif {:raw "elif"}}}}
        :data     {:map-entry {:key-fn data/default-map-key}
                   :set       {:start "{" :end "}" :space ""}
                   :vector    {:start "[" :end "]" :space ""}
                   :tuple     {:start "(" :end ")" :space ""}
                   :free      {:start ""  :end "" :space ""}}
        :rewrite  {:staging [#'rewrite/python-rewrite-stage]}
        :function {:defn        {:raw "def"
                                 :args  {:start "(" :end "):" :space ""}}
                   :fn.inner    {:raw "def"
                                 :symbol {:layout :flat}
                                 :args   {:start "(" :end "):" :space ""}}}
        :define   {:defglobal  {:raw ""}
                   :def        {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :py
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current (fn []
                      (template/$ (list (b:& (set [(str x) :for x :in (locals)])
                                             (set [(str m) :for m :in sys.modules])))))
    :module-export  (fn [{:keys [as refer]} opts])
    :module-import  (fn [name {:keys [as refer]} opts]  
                      (if as
                        (template/$ (:- :import ~name :as ~as))
                        (template/$ (:- :import ~name))))
    :module-unload  (fn [name as]
                      (template/$ (do (del (. sys.modules [~name]))
                                      (del ~(symbol name)))))}))

(def +book+
  (book/book {:lang :python
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
