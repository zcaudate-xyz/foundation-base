(ns std.lang.base.emit-preprocess
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.util :as ut]))

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
  (cond (h/form? tag)
        (cond (= 'clojure.core/deref (first tag))
              (let [tok (second tag)]
                (cond (= tok '!)
                      (list '!:template input)

                      (and (symbol? tok) 
                           (str/starts-with? (str tok) "."))
                      (apply list '!:lang {:lang (keyword (subs (str tok) 1))}
                             input more)

                      (and (h/form? tok)
                           (= 'var (first tok)))
                      (list '!:eval
                            (apply list
                                   (list 'var (or (h/var-sym (resolve (second tok)))
                                                  (h/error "Var not found" {:input (second tok)})))
                                   (if input
                                     (cons input more)
                                     more)))
                      
                      :else
                      (apply list '!:decorate (apply vec (rest tag))
                             input more)))
              
              (= 'clojure.core/unquote (first tag))
              (h/error "Not supported" {:input x}))

        (= 'clojure.core/deref tag)
        (if (and (h/form? input)
                 (= 'var (first input)))
          (list '!:deref (list 'var (or (h/var-sym (resolve (second input)))
                                        (h/error "Var not found" {:input (second input)}))))
          (list '!:eval input))))

(defn to-input
  "converts a form to input (extracting deref forms)"
  {:added "4.0"}
  [raw]
  (let [check-fn (fn [child]
                   (and (h/form? child)
                        (= (first child) '(clojure.core/unquote !))))]
    (h/prewalk (fn [x]
                 (or (cond (h/pointer? x)
                           (ut/sym-full x)

                           (and *macro-splice*
                                (or (vector? x)
                                    (h/form? x))
                                (some check-fn x))
                           (-> (into (empty x) 
                                     (reduce (fn [acc child]
                                               (if (check-fn child)
                                                 (apply conj acc (eval (second child)))
                                                 (conj acc child)))
                                             (empty x)
                                             x))
                               (with-meta (meta x)))
                           
                           (h/form? x)
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
          (h/error "Cannot resolve Module." {:input sym
                                             :current module
                                             :modules (keys modules)})
          
          :else
          [sym-module sym-id
           (ut/sym-full sym-module sym-id)])))

(defn process-namespaced-symbol
  "process namespaced symbols"
  {:added "4.0"}
  [sym modules {:keys [module
                       entry] :as mopts} deps]
  (let [[sym-module sym-id sym-full] (process-namespaced-resolve sym modules mopts)
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
                                 (h/error (str "Upstream not found: "
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
                                               (h/error "Suppressed module - macros only"
                                                        {:sym [sym-module sym-id]
                                                         :module (dissoc module :code :fragment)}))
                                          
                                          _ (if (not (or *macro-skip-deps*
                                                         (= 'defglobal op)
                                                         (= 'defrun op)))
                                              (vswap! deps conj sym-full))]
                                      sym-full)
                  :fragment  (let [{:keys [template standalone form]} entry]
                               (cond (not template) form
                                     
                                     (not standalone)
                                     (h/error "Pure templates are not allowed in body"
                                              {:module sym-module
                                               :id sym-id
                                               :form sym})
                                     (or (h/form? standalone)
                                         (symbol? standalone))
                                     (:standalone entry)
                                     
                                     :else
                                     (let [args (second form)]
                                       (list 'fn args (list 'return (apply template args))))))))))))

(defn process-inline-assignment
  "prepares the form for inline assignment"
  {:added "4.0"}
  [form modules mopts & [unwrapped]]
  (let [[_ bind-form & rdecl] (reverse form)
        [f & args] bind-form
        [f-module f-id] (process-namespaced-resolve f modules mopts)
        _  (or (get-in modules [f-module :code f-id])
               (h/error "Code entry not found:" {:input f
                                                 :form form}))]
    (concat (reverse rdecl)
            [(with-meta (cons (cond-> (ut/sym-full f-module f-id)
                                (not unwrapped) (volatile!))
                              args)
               {:assign/inline true})])))

(defn to-staging-form
  "different staging forms"
  {:added "4.0"}
  [form grammar modules mopts walk-fn]
  (let [fsym      (first form)
        reserved  (get-in grammar [:reserved (first form)])]
    (cond (= fsym '!:template)
          (walk-fn (eval (second form)))
          
          ('#{!:lang !:eval !:deref !:decorate} fsym)
          (volatile! form)
          
          (= :template (:type reserved))
          (walk-fn ((:macro reserved) form))
          
          (= :hard-link (:type reserved))
          (walk-fn (cons (:raw reserved) (rest form)))
          
          (and (= :def-assign (:emit reserved))
               (= :inline (last form)))
          (walk-fn (process-inline-assignment form modules mopts))
          
          :else
          (let [fe (get-fragment (first form)
                                 modules
                                 mopts)]
            (if (:template fe)
              (walk-fn (try (binding [*macro-form* form]
                              (apply (:template fe) (rest form)))
                            (catch Throwable t
                              (h/error "Cannot process form:" {:form form
                                                               :cause t}))))
              form)))))

(defn to-staging
  "converts the stage"
  {:added "4.0"}
  [input grammar modules mopts]
  (binding [*macro-skip-deps* false
            *macro-grammar* grammar
            *macro-opts* mopts]
    (let [deps  (volatile! #{})
          form  (h/prewalk
                 (fn walk-fn [form]
                   (cond (h/form? form)
                         (to-staging-form form grammar modules mopts walk-fn)
                         
                         
                         (and (symbol? form)
                              (namespace form))
                         (process-namespaced-symbol form modules mopts deps)
                         
                         :else form))
                 input)
          form  (h/postwalk (fn [form] (if (volatile? form)
                                         @form
                                         form))
                            form)]
      [form @deps])))

(defn to-resolve
  "resolves only the code symbols (no macroexpansion)"
  {:added "4.0"}
  [input grammar modules mopts]
  (binding [*macro-skip-deps* true
            *macro-grammar* grammar
            *macro-opts* mopts]
    (let [form  (h/prewalk
                 (fn walk-fn [form]
                   
                   (cond  (and (h/form? form)
                               (= (first form) '!:template))
                          (walk-fn (eval (second form)))
                          
                          (and (symbol? form)
                               (namespace form))
                          (process-namespaced-symbol form modules mopts nil)

                          :else
                          form))
                 input)]
      form)))

(comment
  
  (comment
  (get-in (std.lang/grammar :lua) [:reserved 'var*])))
