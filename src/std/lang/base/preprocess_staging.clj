(ns std.lang.base.preprocess-staging
  (:require [std.lang.base.preprocess-base :as preprocess-base]
            [std.lang.base.preprocess-assign :as assign]
            [std.lang.base.preprocess-resolve :as resolve]
            [std.lang.base.preprocess-value :as value]
            [std.lang.base.provenance :as provenance]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.walk :as walk]))

(defn to-staging-form
  "different staging forms"
  {:added "4.0"}
  [form grammar modules mopts deps-fragment walk-fn]
  (let [fsym     (first form)
        reserved (get-in grammar [:reserved (first form)])
        mopts    (provenance/with-provenance
                  mopts
                  {:std.lang/form form
                   :std.lang/symbol fsym})
        template-assignment (assign/process-template-assignment form grammar mopts)]
    (cond (= fsym '!:template)
          (walk-fn (eval (second form)))

          ('#{!:lang !:eval !:deref !:decorate} fsym)
          (volatile! form)

          (= :template (:type reserved))
          (let [mopts (provenance/with-provenance
                        mopts
                        {:std.lang/phase :staging/reserved-template
                         :std.lang/subsystem :std.lang/reserved-template
                         :std.lang/lang (:lang mopts)
                         :std.lang/module (ut/module-id (:module mopts))})]
            (try
              (binding [preprocess-base/*macro-opts* mopts]
                (walk-fn ((:macro reserved) form)))
              (catch Throwable t
                (ut/throw-with-context
                 "std.lang staging template expansion failed"
                 (:std.lang/provenance mopts)
                 t))))
          
          (= :hard-link (:emit reserved))
          (walk-fn (cons (:raw reserved) (rest form)))

          (and (= :def-assign (:emit reserved))
               (= :inline (last form)))
          (walk-fn (assign/process-inline-assignment form modules mopts))

          template-assignment
          (walk-fn template-assignment)

          reserved
          (assign/protect-reserved-head form)

          :else
          (let [fe (resolve/get-fragment (first form)
                                         modules
                                         mopts)]
            (if (:template fe)
              (let [mopts (provenance/with-provenance
                            mopts
                            {:std.lang/phase :staging/fragment-template
                             :std.lang/subsystem :std.lang/fragment-template
                             :std.lang/lang (:lang mopts)
                             :std.lang/module (ut/module-id (:module mopts))
                             :std.lang/entry (ut/entry-summary fe)})]
                (do (if deps-fragment
                      (vswap! deps-fragment conj (ut/sym-full fe)))
                    (walk-fn (try
                               (binding [preprocess-base/*macro-form* form
                                         preprocess-base/*macro-opts* mopts]
                                 (apply (:template fe) (rest form)))
                               (catch Throwable t
                                 (ut/throw-with-context
                                  "std.lang staging macro expansion failed"
                                  (:std.lang/provenance mopts)
                                  t))))))
              form)))))

(defn to-staging
  "converts the stage"
  {:added "4.0"}
  [input grammar modules mopts]
  (let [mopts (provenance/with-provenance
                mopts
                {:std.lang/phase :staging
                 :std.lang/subsystem :std.lang/to-staging
                 :std.lang/lang (:lang mopts)
                 :std.lang/module (ut/module-id (:module mopts))
                 :std.lang/entry (some-> (:entry mopts) ut/entry-summary)})]
    (binding [preprocess-base/*macro-skip-deps* false
              preprocess-base/*macro-grammar* grammar
              preprocess-base/*macro-opts* mopts]
      (let [deps          (volatile! #{})
            deps-fragment (volatile! #{})
            deps-native   (volatile! {})
            _             (if-let [includes (-> mopts :module :includes)]
                            (doseq [inc-id includes]
                              (if-let [module (get modules inc-id)]
                                (doseq [entry (vals (:code module))]
                                  (vswap! deps conj (ut/sym-full entry))))))
            form          (walk/prewalk
                            (fn walk-fn [form]
                              (cond (collection/form? form)
                                    (to-staging-form form grammar modules mopts deps-fragment walk-fn)

                                    (symbol? form)
                                    (or (when-let [standalone (value/value-standalone form grammar)]
                                          (walk-fn standalone))
                                        (if (namespace form)
                                          (resolve/process-namespaced-symbol form modules mopts deps deps-fragment walk-fn)
                                          (resolve/process-standard-symbol form mopts deps-native)))

                                    :else form))
                            input)
            form          (walk/postwalk (fn [form]
                                           (if (volatile? form)
                                             @form
                                             form))
                                         form)]
        [form @deps @deps-fragment @deps-native]))))

(defn to-resolve
  "resolves only the code symbols (no macroexpansion)"
  {:added "4.0"}
  [input grammar modules mopts]
  (binding [preprocess-base/*macro-skip-deps* true
            preprocess-base/*macro-grammar* grammar
            preprocess-base/*macro-opts* mopts]
    (let [form (walk/prewalk
                (fn walk-fn [form]
                  (cond (and (collection/form? form)
                             (= (first form) '!:template))
                        (walk-fn (eval (second form)))

                        (and (collection/form? form)
                             (get-in grammar [:reserved (first form)]))
                        (assign/protect-reserved-head form)

                        (symbol? form)
                        (or (value/value-standalone form grammar)
                            (if (namespace form)
                              (resolve/process-namespaced-symbol form modules mopts nil nil identity)
                              (resolve/process-standard-symbol form mopts nil)))

                        :else
                        form))
                input)]
      form)))
