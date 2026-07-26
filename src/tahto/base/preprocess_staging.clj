(ns tahto.common.preprocess-staging
  (:require [tahto.common.preprocess-base :as preprocess-base]
            [tahto.common.preprocess-assign :as assign]
            [tahto.common.preprocess-resolve :as resolve]
            [tahto.common.preprocess-value :as value]
            [tahto.common.provenance :as provenance]
            [tahto.common.util :as ut]
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
                   {:tahto/form form
                    :tahto/symbol fsym})
         template-assignment (assign/process-template-assignment form grammar modules mopts)]
    (cond (= fsym '!:template)
          (walk-fn (eval (second form)))

          ('#{!:lang !:eval !:deref !:decorate} fsym)
          (volatile! form)

           (= :template (:type reserved))
           (let [mopts (provenance/with-provenance
                         mopts
                        {:tahto/phase :staging/reserved-template
                         :tahto/subsystem :tahto/reserved-template
                         :tahto/lang (:lang mopts)
                         :tahto/module (ut/module-id (:module mopts))})]
             (try
               (binding [preprocess-base/*macro-opts* mopts]
                 (walk-fn ((:macro reserved) form)))
               (catch Throwable t
                 (ut/throw-with-context
                  "tahto.core staging template expansion failed"
                  (:tahto/provenance mopts)
                  t))))
          
          (= :hard-link (:emit reserved))
          (walk-fn (cons (:raw reserved) (rest form)))

          (and (= :def-assign (:emit reserved))
               (or (= :inline (last form))
                   (and (-> form last meta :inline)
                        (not (-> form last meta :assign/inline)))))
          (walk-fn (assign/process-inline-assignment form modules mopts))

          template-assignment
          (walk-fn template-assignment)

          :else
          (if-let [value-form (value/process-value-form form grammar modules mopts)]
            (walk-fn value-form)

            (if reserved
              (assign/protect-reserved-head form)

              (let [fe (resolve/get-fragment (first form)
                                             modules
                                             mopts)]
                (if (:template fe)
                   (let [mopts (provenance/with-provenance
                                 mopts
                                {:tahto/phase :staging/fragment-template
                                 :tahto/subsystem :tahto/fragment-template
                                 :tahto/lang (:lang mopts)
                                 :tahto/module (ut/module-id (:module mopts))
                                 :tahto/entry (ut/entry-summary fe)})]
                     (do (if deps-fragment
                           (vswap! deps-fragment conj (ut/sym-full fe)))
                         (walk-fn (try
                                    (binding [preprocess-base/*macro-form* form
                                              preprocess-base/*macro-opts* mopts]
                                      (apply (:template fe) (rest form)))
                                    (catch Throwable t
                                      (ut/throw-with-context
                                       "tahto.core staging macro expansion failed"
                                       (:tahto/provenance mopts)
                                       t))))))
                   form)))))))

(defn to-staging
  "converts the stage"
  {:added "4.0"}
  [input grammar modules mopts]
  (let [mopts (provenance/with-provenance
                mopts
                {:tahto/phase :staging
                 :tahto/subsystem :tahto/to-staging
                 :tahto/lang (:lang mopts)
                 :tahto/module (ut/module-id (:module mopts))
                 :tahto/entry (some-> (:entry mopts) ut/entry-summary)})]
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
