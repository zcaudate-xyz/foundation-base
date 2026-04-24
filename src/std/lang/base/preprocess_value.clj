(ns std.lang.base.preprocess-value
  (:require [std.lang.base.preprocess-base :as preprocess-base]
            [std.lib.collection :as collection]))

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
  [sym grammar]
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
           nil)))

(defn process-value-form
  "rewrites block-valued macros used in value position"
  {:added "4.1"}
  [form grammar mopts]
  (cond (and (= 'return (first form))
             (= 2 (count form)))
        (when-let [expanded (expand-value-block (second form) grammar mopts)]
          expanded)

        :else
        (let [[head & args0] form
              args  (vec args0)
              args' (mapv (fn [arg]
                            (if-let [_ (value-block-entry arg grammar)]
                              (with-meta (cons (value-standalone (first arg) grammar)
                                               (rest arg))
                                (meta arg))
                              arg))
                          args)]
          (when (not= args' args)
            (with-meta (apply list head args')
              (meta form))))))
