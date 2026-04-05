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
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.com-python :as com]
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

(defn python-defn-
  "hidden function without decorators"
  {:added "4.0"}
  [form grammar mopts]
  (top/emit-top-level :defn form grammar mopts))

(defn python-defn
  "creates a defn function for python"
  {:added "4.0"}
  ([form]
   (let [decorators (get (meta (second form)) :decorators)
         body  (cons 'defn- (rest form))]
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
                          [(if (and (collection/form? body)
                                    (= 'return (first body)))
                             (second body)
                             body)]))))))

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

(def +features+
  (let [base (-> (grammar/build :exclude [:pointer
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
                   :for-return  {:macro #'tf-for-return :emit :macro}}))
        base-keys (set (keys base))
        fn-overrides (select-keys fn/+python+ base-keys)
        fn-extensions (apply dissoc fn/+python+ base-keys)
        with-fn (cond-> base
                  (seq fn-overrides) (grammar/build:override fn-overrides)
                  (seq fn-extensions) (grammar/build:extend fn-extensions))
        with-fn-keys (set (keys with-fn))
        com-overrides (select-keys com/+python-com+ with-fn-keys)
        com-extensions (apply dissoc com/+python-com+ with-fn-keys)]
    (cond-> with-fn
      (seq com-overrides) (grammar/build:override com-overrides)
      (seq com-extensions) (grammar/build:extend com-extensions)
      true (grammar/build:extend
            {:defn-     {:op :defn-   :symbol #{'defn-}  :type :block :emit #'python-defn-}
             :var-let   {:op :var-let :symbol #{'var}  :macro #'python-var :emit :macro}
             :unarr     {:op :unarr   :symbol #{:*}    :raw "*"    :emit :pre}
             :undict    {:op :undict  :symbol #{:**}   :raw "**"   :emit :pre}
             :del       {:op :del     :symbol #{'del}  :raw "del"  :emit :prefix}
             :pass      {:op :pass    :symbol #{'pass} :raw "pass" :emit :return :type :special}
             :nan       {:op :nan     :symbol #{'NaN}  :raw "NaN"  :value true :emit :throw}
             :with      {:op :with    :symbol #{'with} :type :block
                         :block  {:main #{:parameter :body}}}}))))

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
