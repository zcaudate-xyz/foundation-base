(ns std.lang.base.emit-preprocess
  (:require [clojure.string]
            [std.lang.base.provenance :as provenance]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))

(def ^:dynamic *macro-form* nil)

(def ^:dynamic *macro-grammar* nil)

(def ^:dynamic *macro-opts* nil)

(def ^:dynamic *macro-splice* nil)

(def ^:dynamic *macro-skip-deps* nil)

(defn macro-form
  "gets the current macro form"
  {:added "4.0"}
  []
  *macro-form*)

(defn macro-opts
  "gets current macro-opts"
  {:added "4.0"}
  []
  *macro-opts*)

(defn macro-grammar
  "gets the current grammar"
  {:added "4.0"}
  []
  *macro-grammar*)

(defmacro ^{:style/indent 1}
  with:macro-opts
  "bind macro opts"
  {:added "4.0"}
  [[mopts] & body]
  `(binding [*macro-opts* ~mopts]
     ~@body))

;;
;; RAW FORMS TO INPUT FORMS
;;

(defn to-input-form
  "processes a form
   
   (def hello 1)
   
   (to-input-form '(@! (+ 1 2 3)))
   => '(!:template (+ 1 2 3))
   
   (to-input-form '(-/Class$$new))
   => (any '(static-invoke -/Class \"new\")
           nil)
   
   (to-input-form '(Class$$new 1 2 3))
   => (any '(static-invoke Class \"new\" 1 2 3)
           nil)
   
   (to-input-form '@#'hello)
   => '(!:deref (var std.lang.base.emit-preprocess-test/hello))
   
   (to-input-form '@(+ 1 2 3))
   => '(!:eval (+ 1 2 3))
 
   (to-input-form '(@.lua (do 1 2 3)))
   => '(!:lang {:lang :lua} (do 1 2 3))"
  {:added "4.0"}
  [[tag input & more :as x]]
  (cond (collection/form? tag)
        (cond (= 'clojure.core/deref (first tag))
              (let [tok (second tag)]
                (cond (= tok '!)
                      (list '!:template input)

                      (and (symbol? tok) 
                           (clojure.string/starts-with? (str tok) "."))
                      (apply list '!:lang {:lang (keyword (subs (str tok) 1))}
                             input more)

                      (and (collection/form? tok)
                           (= 'var (first tok)))
                      (list '!:eval
                            (apply list
                                   (list 'var (or (f/var-sym (resolve (second tok)))
                                                  (f/error "Var not found" {:input (second tok)})))
                                   (if input
                                     (cons input more)
                                     more)))
                      
                      :else
                      (apply list '!:decorate (apply vec (rest tag))
                             input more)))
              
              (= 'clojure.core/unquote (first tag))
              (f/error "Not supported" {:input x}))

        (= 'clojure.core/deref tag)
        (if (and (collection/form? input)
                 (= 'var (first input)))
          (list '!:deref (list 'var (or (f/var-sym (resolve (second input)))
                                        (f/error "Var not found" {:input (second input)}))))
          (list '!:eval input))))

(defn to-input
  "converts a form to input (extracting deref forms)"
  {:added "4.0"}
  [raw]
  (let [check-fn (fn [child]
                   (and (collection/form? child)
                        (= (first child) '(clojure.core/unquote !))))]
    (walk/prewalk (fn [x]
                 (or (cond (ptr/pointer? x)
                           (ut/sym-full x)

                           (and *macro-splice*
                                (or (vector? x)
                                    (collection/form? x))
                                (some check-fn x))
                           (-> (into (empty x) 
                                     (reduce (fn [acc child]
                                               (if (check-fn child)
                                                 (apply conj acc (eval (second child)))
                                                 (conj acc child)))
                                             (empty x)
                                             x))
                               (with-meta (meta x)))
                           
                           (collection/form? x)
                           (to-input-form x))
                     x))
               raw)))

;;
;; INPUT FORMS TO STAGED FORMS
;;

(defn get-fragment
  "gets the fragment given a symbol and modules"
  {:added "4.0"}
  ([sym modules mopts]
   (if (and (symbol? sym)
            (namespace sym))
     (let [[sym-ns sym-id] (ut/sym-pair sym)
           {:keys [id link]} (:module mopts)
           sym-module (or (if (= sym-ns id) id)
                          (get link sym-ns)
                          (first (filter #(= % sym-ns)
                                         (vals link)))
                          sym-ns)]
       (or (get-in modules [sym-module :fragment sym-id])
           (if-let [et (and (get-in modules [sym-module :code sym-id]))]
             (if (= :defrun (:op-key et))
               (apply list 'do (drop 2 (:form et))))))))))

(defn arglists->argv
  "normalizes arglists to a single argv vector"
  {:added "4.1"}
  [arglists]
  (let [argv (-> arglists first)]
    (if (vector? (first argv))
      (first argv)
      argv)))

(defn value-template-args
  "derives callable value args from a template var"
  {:added "4.1"}
  [template]
  (let [arglists (-> template meta :arglists)
        argv     (arglists->argv arglists)]
    (->> argv
         rest
         vec)))

(defn reserved-value-args
  "derives callable value args for a reserved entry"
  {:added "4.1"}
  [reserved]
  (or (some-> reserved :op-spec :arglists arglists->argv vec)
      (some-> (or (:value/template reserved)
                  (when (= :macro (:emit reserved))
                    (:macro reserved)))
              value-template-args)))

(defn reserved-return-mode
  "gets semantic return mode for a reserved entry"
  {:added "4.1"}
  [reserved]
  (or (:return-mode reserved)
      (when (= :xt/self
               (get-in reserved [:op-spec :type 2]))
        :self)))

(defn reserved-expand-context
  "creates context for reserved expander functions"
  {:added "4.1"}
  [mode form grammar modules mopts reserved]
  {:mode mode
   :form form
   :symbol (if (symbol? form) form (first form))
   :grammar grammar
   :modules modules
   :mopts mopts
   :reserved reserved})

(defn expand-reserved-form
  "expands a reserved form using context-aware hooks when available"
  {:added "4.1"}
  ([form grammar mopts]
   (expand-reserved-form form grammar nil mopts))
  ([form grammar modules mopts]
   (when-let [reserved (and (collection/form? form)
                            (get-in grammar [:reserved (first form)]))]
     (let [ctx (reserved-expand-context :form form grammar modules mopts reserved)]
       (or (when-let [expand (:expand/form reserved)]
             (expand ctx))
           (when-let [macro (:macro reserved)]
             (macro form))
           (when (and (:raw reserved)
                      (#{:alias :hard-link} (:emit reserved)))
             (cons (:raw reserved) (rest form))))))))

(defn expand-reserved-assign
  "expands a reserved form for assignment-aware lowering"
  {:added "4.1"}
  ([form grammar mopts]
   (expand-reserved-assign form grammar nil mopts))
  ([form grammar modules mopts]
   (when-let [reserved (and (collection/form? form)
                            (get-in grammar [:reserved (first form)]))]
     (let [ctx (reserved-expand-context :assign form grammar modules mopts reserved)]
       (or (when-let [expand (:expand/assign reserved)]
             (expand ctx))
           (expand-reserved-form form grammar modules mopts))))))

(defn value-standalone
  "returns the standalone expansion for a value-liftable reserved symbol"
  {:added "4.1"}
  ([sym grammar]
   (value-standalone sym grammar nil nil))
  ([sym grammar modules mopts]
   (let [{:keys [emit macro]
          template :value/template
          standalone :value/standalone
          expand-value :expand/value
          :as reserved} (get-in grammar [:reserved sym])
         args        (reserved-value-args reserved)
         return-mode (reserved-return-mode reserved)]
     (cond (fn? expand-value)
           (expand-value (reserved-expand-context :value sym grammar modules mopts reserved))

           (or (collection/form? expand-value)
               (symbol? expand-value))
           expand-value

           (and (= true expand-value)
                (seq args))
           (let [expanded (expand-reserved-form (apply list sym args)
                                                grammar
                                                modules
                                                mopts)]
             (case return-mode
               :self (let [self-arg (first args)]
                       (list 'fn args
                             expanded
                             (list 'return self-arg)))
               :statement (list 'fn args expanded)
               (list 'fn args
                     (list 'return expanded))))

           (or (collection/form? standalone)
               (symbol? standalone))
           standalone

           (and (= true standalone)
                (or template
                    (and (= :macro emit) macro)))
           (let [template (or template
                              (when (= :macro emit)
                                macro))
                 args     (value-template-args template)]
             (if (= :self return-mode)
               (let [self-arg (first args)]
                 (list 'fn args
                       (template (apply list nil args))
                       (list 'return self-arg)))
               (list 'fn args
                     (list 'return
                           (template (apply list nil args))))))

           :else
           nil))))

(defn process-namespaced-resolve
  "resolves symbol in current namespace"
  {:added "4.0"}
  [sym modules {:keys [module
                       entry] :as mopts}]
  (let [[sym-ns sym-id] (ut/sym-pair sym)
        sym-module (or (if (= '- sym-ns) (:id module))
                       (get (:link module) sym-ns)
                       (if (get modules sym-ns) sym-ns))]
    (cond (not sym-module)
          (f/error "Cannot resolve Module." {:input sym
                                             :current module
                                             :modules (keys modules)})
          
          :else
          [sym-module sym-id
           (ut/sym-full sym-module sym-id)])))

(defn process-namespaced-symbol
  "process namespaced symbols"
  {:added "4.0"}
  [sym modules {:keys [module
                       entry] :as mopts} deps deps-fragment walk-fn]
  (let [walk-fn (or walk-fn identity)
        [sym-module sym-id sym-full] (process-namespaced-resolve sym modules mopts)
        module-id (:id module)]
    (cond (and (= sym-module module-id)
               (= sym-id (:id entry)))
          sym-full

          :else
          (let [[type entry] (or (if-let [e (get-in modules [sym-module :code sym-id])]
                                   [:code  e])
                                 (if-let [e (get-in modules [sym-module :fragment sym-id])]
                                   [:fragment e])
                                 (if-let [e (get-in modules [sym-module :header sym-id])]
                                   [:header e])
                                 (f/error (str "Upstream not found: "
                                               (ut/sym-full {:module sym-module
                                                             :id sym-id}))
                                          {:entry (ut/sym-full {:module sym-module
                                                                :id sym-id})
                                           :opts    (select-keys mopts [:lang
                                                                        :module])}))]
            (or (if *macro-skip-deps* sym-full)
                (case type
                  (:header :code)   (let [{:keys [op]} entry
                                          _  (if (and (get (:suppress module) sym-module)
                                                      (not= 'defglobal op))
                                               (f/error "Suppressed module - macros only"
                                                        {:sym [sym-module sym-id]
                                                         :module (dissoc module :code :fragment)}))
                                          
                                          _ (if (not (or *macro-skip-deps*
                                                         (not deps)
                                                         (= 'defglobal op)
                                                         (= 'defrun op)))
                                              (vswap! deps conj sym-full))]
                                      sym-full)
                  :fragment  (let [{:keys [template standalone form]} entry
                                   _ (if (not (or *macro-skip-deps*
                                                  (not deps-fragment)))
                                       (vswap! deps-fragment conj sym-full))]
                               (cond (not template) form
                                     
                                     (not standalone)
                                     (f/error "Pure templates are not allowed in body"
                                              {:module sym-module
                                               :id sym-id
                                               :form sym})
                                     
                                     (or (collection/form? standalone)
                                         (symbol? standalone))
                                     (walk-fn (:standalone entry))
                                     
                                     :else
                                     (let [args (second form)]
                                       (list 'fn args (list 'return
                                                            (apply template args))))))))))))

(defn process-inline-assignment
  "prepares the form for inline assignment"
  {:added "4.0"}
  [form modules mopts & [unwrapped]]
  (let [[_ bind-form & rdecl] (reverse form)
        [f & args] bind-form
        [f-module f-id] (process-namespaced-resolve f modules mopts)
        _  (or (get-in modules [f-module :code f-id])
               (f/error "Code entry not found:" {:input f
                                                 :form form}))]
    (concat (reverse rdecl)
             [(with-meta (cons (cond-> (ut/sym-full f-module f-id)
                                 (not unwrapped) (volatile!))
                              args)
                {:assign/inline true})])))

(defn protect-reserved-head
  [form]
  (with-meta (cons (volatile! (first form))
                   (rest form))
    (meta form)))

(defn to-staging-form
  "different staging forms"
  {:added "4.0"}
  [form grammar modules mopts deps-fragment walk-fn]
  (let [fsym      (first form)
        reserved  (get-in grammar [:reserved (first form)])
        mopts     (provenance/with-provenance
                    mopts
                    {:std.lang/form form
                     :std.lang/symbol fsym})]
    (cond (= fsym '!:template)
          (walk-fn (eval (second form)))
          
          ('#{!:lang !:eval !:deref !:decorate} fsym)
          (volatile! form)
          
          (= :template (:type reserved))
          (let [mopts (provenance/with-provenance
                        mopts
                        {:std.lang/phase :staging/reserved-template
                         :std.lang/subsystem :std.lang.base.emit-preprocess/reserved-template
                         :std.lang/lang (:lang mopts)
                         :std.lang/module (ut/module-id (:module mopts))})]
            (try
              (binding [*macro-opts* mopts]
                (walk-fn (expand-reserved-form form grammar modules mopts)))
              (catch Throwable t
                (ut/throw-with-context
                 "std.lang staging template expansion failed"
                 (:std.lang/provenance mopts)
                 t))))
          
          (= :hard-link (:emit reserved))
          (walk-fn (cons (:raw reserved) (rest form)))
          
          (and (= :def-assign (:emit reserved))
               (= :inline (last form)))
          (walk-fn (process-inline-assignment form modules mopts))

          reserved
          (protect-reserved-head form)
          
          :else
          (let [fe (get-fragment (first form)
                                 modules
                                 mopts)]
            (if (:template fe)
              (let [mopts (provenance/with-provenance
                            mopts
                            {:std.lang/phase :staging/fragment-template
                             :std.lang/subsystem :std.lang.base.emit-preprocess/fragment-template
                             :std.lang/lang (:lang mopts)
                             :std.lang/module (ut/module-id (:module mopts))
                             :std.lang/entry (ut/entry-summary fe)})]
                (do (if deps-fragment
                      (vswap! deps-fragment conj (ut/sym-full fe)))
                    (walk-fn (try
                               (binding [*macro-form* form
                                         *macro-opts* mopts]
                                 (apply (:template fe) (rest form)))
                               (catch Throwable t
                                 (ut/throw-with-context
                                  "std.lang staging macro expansion failed"
                                  (:std.lang/provenance mopts)
                                  t))))))
              form)))))

(defn process-standard-symbol
  [sym mopts deps-native]
  (let [symstr (name sym)
        idx  (.indexOf (name sym) ".")
        _  (if (<= 0 idx)
             (let [symlead (symbol (subs symstr 0 idx))
                   import  (get-in mopts
                                   [:module
                                    :native-lu
                                    symlead])]
               (if (and import deps-native)
                 (vswap! deps-native
                         update
                         import
                         (fnil #(conj % symlead) #{}))))
             (let [import  (get-in mopts
                                   [:module
                                    :native-lu
                                    sym])]
               (if (and import deps-native)
                 (vswap! deps-native
                         update
                         import
                         (fnil #(conj % sym) #{})))))]
    sym))

(defn to-staging
  "converts the stage"
  {:added "4.0"}
  [input grammar modules mopts]
  (let [mopts (provenance/with-provenance
                mopts
                {:std.lang/phase :staging
                 :std.lang/subsystem :std.lang.base.emit-preprocess/to-staging
                 :std.lang/lang (:lang mopts)
                 :std.lang/module (ut/module-id (:module mopts))
                 :std.lang/entry (some-> (:entry mopts) ut/entry-summary)})]
    (binding [*macro-skip-deps* false
              *macro-grammar* grammar
              *macro-opts* mopts]
      (let [deps  (volatile! #{})
            deps-fragment   (volatile! #{})
            deps-native  (volatile! {})
          
            _   (if-let [includes (-> mopts :module :includes)]
                  (doseq [inc-id includes]
                    (if-let [module (get modules inc-id)]
                      (doseq [entry (vals (:code module))]
                        (vswap! deps conj (ut/sym-full entry))))))

            form  (walk/prewalk
                   (fn walk-fn [form]
                     (cond (collection/form? form)
                           (to-staging-form form grammar modules mopts deps-fragment walk-fn)
                           
                           (and (symbol? form))
                           (or (when-let [standalone (value-standalone form grammar modules mopts)]
                                 (walk-fn standalone))
                               (if (namespace form)
                                 (process-namespaced-symbol form modules mopts deps deps-fragment walk-fn)
                                 (process-standard-symbol form mopts deps-native)))
                           
                           :else form))
                   input)
            form  (walk/postwalk (fn [form] (if (volatile? form)
                                            @form
                                            form))
                               form)]
      
        [form @deps @deps-fragment @deps-native]))))

(defn to-resolve
  "resolves only the code symbols (no macroexpansion)"
  {:added "4.0"}
  [input grammar modules mopts]
  (binding [*macro-skip-deps* true
            *macro-grammar* grammar
            *macro-opts* mopts]
    (let [form  (walk/prewalk
                 (fn walk-fn [form]
                    
                    (cond (and (collection/form? form)
                               (= (first form) '!:template))
                          (walk-fn (eval (second form)))

                          (and (collection/form? form)
                               (get-in grammar [:reserved (first form)]))
                          (protect-reserved-head form)

                          (symbol? form)
                          (or (value-standalone form grammar modules mopts)
                              (if (namespace form)
                                (process-namespaced-symbol form modules mopts nil nil identity)
                                (process-standard-symbol form mopts nil)))
                          
                          :else
                          form))
                 input)]
      form)))

(defn find-natives
  [entry mopts]
  (let [deps-quoted  (volatile! [])
        deps-native  (volatile! {})
        _    (walk/postwalk
              (fn [form]
                (if (and (list? form)
                         (= (first form) 'quote))
                  (vswap! deps-quoted conj (second form)))
                form)
              (:form entry))
        _    (walk/postwalk
              (fn [form]
                (cond (symbol? form)
                      (process-standard-symbol form mopts deps-native)
                      
                      :else form))
              @deps-quoted)]
    @deps-native))

(comment
  
  (comment
    (get-in (std.lang/grammar :lua) [:reserved 'var*])))
