(ns hara.model.spec-python
  (:require [clojure.string :as string]
            [hara.lang.book :as book]
 	    [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.emit-data :as data]
            [hara.common.emit-helper :as helper]
            [hara.common.emit-preprocess :as preprocess] [hara.common.preprocess-base :as preprocess-base]
            [hara.common.emit-top-level :as top]
            [hara.common.grammar :as grammar]
            [hara.common.grammar-spec :as spec]
            [hara.lang.script :as script]
            [hara.typed.xtalk-analysis :as xtalk-analysis]
            [hara.common.util :as ut]
            [hara.model.spec-xtalk]
            [hara.model.spec-python.rewrite :as rewrite]
            [hara.model.spec-xtalk.fn-python :as fn]
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
            optional-count (when (and inferred-count
                                      (pos? inferred-count))
                             inferred-count)]
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

(defn- python-plain-symbol?
  [sym]
  (and (symbol? sym)
       (nil? (namespace sym))))

(defn- python-binding-symbols
  [form]
  (cond (python-plain-symbol? form)
        #{form}

        (vector? form)
        (into #{} (mapcat python-binding-symbols) form)

        (set? form)
        (into #{} (mapcat python-binding-symbols) form)

        (map? form)
        (into #{} (mapcat python-binding-symbols) (vals form))

        :else
        #{}))

(defn- python-scope-form?
  [form]
  (and (collection/form? form)
       (contains? '#{fn fn.inner defn defn- defgen} (first form))))

(defn- python-local-symbols
  [form]
  (cond (python-scope-form? form)
        #{}

        (and (collection/form? form)
             (= 'quote (first form)))
        #{}

        (and (collection/form? form)
             (contains? '#{var var*} (first form)))
        (python-binding-symbols (second form))

        (collection/form? form)
        (into #{} (mapcat python-local-symbols) form)

        (vector? form)
        (into #{} (mapcat python-local-symbols) form)

        (set? form)
        (into #{} (mapcat python-local-symbols) form)

        (map? form)
        (into #{} (mapcat python-local-symbols) (concat (keys form) (vals form)))

        :else
        #{}))

(defn- python-assigned-symbols
  [form]
  (cond (python-scope-form? form)
        #{}

        (and (collection/form? form)
             (= 'quote (first form)))
        #{}

        (and (collection/form? form)
             (= ':= (first form)))
        (python-binding-symbols (second form))

        (collection/form? form)
        (into #{} (mapcat python-assigned-symbols) form)

        (vector? form)
        (into #{} (mapcat python-assigned-symbols) form)

        (set? form)
        (into #{} (mapcat python-assigned-symbols) form)

        (map? form)
        (into #{} (mapcat python-assigned-symbols) (concat (keys form) (vals form)))

        :else
        #{}))

(defn- python-prepend-nonlocals
  [name args body]
  (if-not (:inner (meta name))
    body
    (let [arg-syms    (python-binding-symbols args)
          local-syms  (into arg-syms (mapcat python-local-symbols) body)
          assigned    (into #{} (mapcat python-assigned-symbols) body)
          nonlocals   (->> assigned
                           (remove local-syms)
                           (sort-by clojure.core/str))]
      (if (empty? nonlocals)
        body
        (cons (apply list 'nonlocal nonlocals) body)))))

(defn python-emit-nonlocal
  [[_ & syms] grammar mopts]
  (str "nonlocal "
       (string/join ", " (map #(common/*emit-fn* % grammar mopts) syms))))

(defn python-defn-
  "hidden function without decorators"
  {:added "4.0"}
  [form grammar mopts]
  (let [[tag name args & more] form
        more (python-prepend-nonlocals name args more)]
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
           (let [args (if (empty? args)
                        [(list ':* '__args)]
                        args)]
           (apply list :- :lambda
                  (concat (if (not-empty args)
                              [(list 'quote args) ":"]
                              [":"])
                            [(python-lambda-body body)])))))))

(defn- python-dot-length?
  [prop]
  (and (vector? prop)
       (= 1 (count prop))
       (= "length" (first prop))))

(defn python-dot
  [[_ obj & props]]
  (let [grammar preprocess-base/*macro-grammar*
        mopts   preprocess-base/*macro-opts*]
    (if (and (= 1 (count props))
             (python-dot-length? (first props)))
      (list 'len obj)
      (let [target (let [emitted (common/*emit-fn* obj grammar mopts)]
                     (if (collection/form? obj)
                       (str "(" emitted ")")
                       emitted))]
        (list ':- (str target
                       (apply str (map #(common/emit-index-entry % grammar mopts)
                                       props))))))))

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
  [[_ [[res err] statement] {:keys [success error]}]]
  (template/$ (try (var ~res ~statement)
                   ~success
                   (catch [Exception :as ~err] ~error))))

(defn python-tf-prototype-create
  [[_ m]]
  m)

(defn python-tf-prototype-get
  [[_ obj]]
  (list 'x:get-key obj "_xt_proto" nil))

(defn python-tf-prototype-set
  [[_ obj prototype]]
  (list 'or
        (list '. obj (list '__setitem__ "_xt_proto" prototype))
        obj))

(defn python-tf-prototype-method
  [[_ obj key]]
  (let [direct (list 'x:get-key obj key nil)
        proto  (list 'or (list 'proto:get obj) {})]
    (list ':?
          (list 'not= nil direct)
          direct
          (list 'x:get-key proto key nil))))


(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block])
       (grammar/build:override
        {:pow         {:raw "**"}
         :and         {:raw "and"}
         :or          {:raw "or"}
         :not         {:raw "not" :emit :prefix}
         :throw       {:raw "raise"  :emit :prefix}
         :index       {:macro #'python-dot :emit :macro}
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
         :prototype-get       {:macro #'python-tf-prototype-get     :emit :macro}
         :prototype-set       {:macro #'python-tf-prototype-set     :emit :macro}
         :prototype-create    {:macro #'python-tf-prototype-create  :emit :macro
                               :op-spec {:allow-blocks true}}
         :prototype-method    {:macro #'python-tf-prototype-method  :emit :macro}
         :prototype-tostring  {:emit :unit :default "__str__"}})
       (grammar/build:override fn/+python+)
       (grammar/build:extend
        {:defn-     {:op :defn-   :symbol #{'defn-}  :type :block :emit #'python-defn-}
        :var-let   {:op :var-let :symbol #{'var}  :macro #'python-var :emit :macro}
        :unarr     {:op :unarr   :symbol #{:*}    :raw "*"    :emit :pre}
        :undict    {:op :undict  :symbol #{:**}   :raw "**"   :emit :pre}
        :del       {:op :del     :symbol #{'del}  :raw "del"  :emit :prefix}
        :pass      {:op :pass    :symbol #{'pass} :raw "pass" :emit :return :type :special}
        :nan       {:op :nan     :symbol #{'NaN}  :raw "NaN"  :value true :emit :throw}
         :nonlocal  {:op :nonlocal :symbol #{'nonlocal} :emit #'python-emit-nonlocal}
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
