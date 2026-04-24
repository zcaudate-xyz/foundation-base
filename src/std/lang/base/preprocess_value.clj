(ns std.lang.base.preprocess-value
  (:require [std.lang.base.preprocess-base :as preprocess-base]
            [std.lang.base.preprocess-resolve :as resolve]
            [std.lib.collection :as collection]))

(declare expand-value-form)

(defn value-block-entry
  "returns the reserved entry for a block-valued macro call"
  {:added "4.1"}
  [form grammar]
  (when (collection/form? form)
    (let [{:keys [emit macro op-spec] :as entry} (get-in grammar [:reserved (first form)])]
      (when (and (= :macro emit)
                 (:allow-blocks op-spec)
                 macro)
        entry))))

(defn expand-value-block
  "expands a block-valued macro call"
  {:added "4.1"}
  [form grammar mopts]
  (when-let [macro (:macro (value-block-entry form grammar))]
    (let [expanded (binding [preprocess-base/*macro-form* form
                             preprocess-base/*macro-grammar* grammar
                             preprocess-base/*macro-opts* mopts]
                     (macro form))]
      (with-meta expanded
        (merge (meta form) (meta expanded))))))

(defn value-fragment-entry
  "returns the standalone fragment entry for a namespaced value form"
  {:added "4.1"}
  [form modules mopts]
  (when (and modules
             (collection/form? form)
             (symbol? (first form))
             (namespace (first form)))
    (when-let [entry (resolve/get-fragment (first form) modules mopts)]
      (when (and (map? entry)
                 (:template entry)
                 (:standalone entry))
        entry))))

(defn expand-value-fragment
  "expands a namespaced standalone fragment used in value position"
  {:added "4.1"}
  [form grammar modules mopts]
  (when-let [entry (value-fragment-entry form modules mopts)]
    (let [expanded (binding [preprocess-base/*macro-form* form
                             preprocess-base/*macro-opts* mopts]
                     (apply (:template entry) (rest form)))
          expanded (with-meta expanded
                     (merge (meta form) (meta expanded)))]
      (when (not= expanded form)
        (expand-value-form expanded grammar modules mopts)))))

(defn expand-value-form
  "expands a form that needs block-aware value lowering"
  {:added "4.1"}
  [form grammar modules mopts]
  (or (expand-value-block form grammar mopts)
      (expand-value-fragment form grammar modules mopts)))

(defn value-template-args
  "derives callable value args from op or template arglists"
  {:added "4.1"}
  ([template]
   (value-template-args nil template))
  ([arglists template]
   (if arglists
     (let [argv (-> arglists first)
           argv (if (vector? (first argv))
                  (first argv)
                  argv)]
       (vec argv))
     (let [arglists (-> template meta :arglists)
           argv     (-> arglists first)
           argv     (if (vector? (first argv))
                      (first argv)
                      argv)]
       (->> argv
            rest
            vec)))))

(defn value-standalone
  "returns the standalone expansion for a value-liftable reserved symbol"
  {:added "4.1"}
  ([sym grammar]
   (value-standalone sym grammar nil nil))
  ([sym grammar modules mopts]
   (let [{:keys [emit macro]
          template :value/template
          standalone :value/standalone
          op-spec :op-spec} (get-in grammar [:reserved sym])
         template (or template
                      (when (= :macro emit)
                        macro))
         block? (and (= :macro emit)
                     (:allow-blocks op-spec))
         self-return? (= :xt/self
                         (get-in op-spec [:type 2]))]
     (cond (or (collection/form? standalone)
               (symbol? standalone))
           standalone

           (and (= true standalone)
                template)
           (let [args (value-template-args (:arglists op-spec)
                                           template)]
             (if self-return?
               (let [self-arg (first args)]
                 (list 'fn args
                       (template (apply list nil args))
                       (list 'return self-arg)))
               (let [body (template (apply list nil args))]
                 (list 'fn args
                       (if block?
                         body
                         (list 'return body))))))

           :else
           (when (and modules
                      (symbol? sym)
                      (namespace sym))
             (when-let [entry (resolve/get-fragment sym modules mopts)]
               (let [{:keys [template standalone form]} entry]
                 (cond (or (collection/form? standalone)
                           (symbol? standalone))
                       standalone

                       (and (= true standalone)
                            template)
                       (let [args (vec (second form))
                             body (apply template args)
                             body (expand-value-form body grammar modules mopts)]
                         (when body
                           (list 'fn args body)))

                       :else
                       nil))))))))

(def ^:private +statement-block-heads+
  '#{do do*})

(defn process-value-form
  "rewrites block-valued macros used in value position"
  {:added "4.1"}
  ([form grammar mopts]
   (process-value-form form grammar nil mopts))
  ([form grammar modules mopts]
   (cond (and (= 'return (first form))
              (= 2 (count form)))
         (when-let [expanded (expand-value-form (second form)
                                                grammar
                                                modules
                                                mopts)]
           expanded)

         :else
         (let [[head & args0] form]
           (when-not (contains? +statement-block-heads+ head)
             (let [args  (vec args0)
                   args' (mapv (fn [arg]
                                 (let [standalone (and (collection/form? arg)
                                                       (symbol? (first arg))
                                                       (value-standalone (first arg)
                                                                         grammar
                                                                         modules
                                                                         mopts))]
                                   (if standalone
                                     (with-meta (cons standalone
                                                      (rest arg))
                                       (meta arg))
                                     arg)))
                               args)]
               (when (not= args' args)
                 (with-meta (apply list head args')
                   (meta form)))))))))
