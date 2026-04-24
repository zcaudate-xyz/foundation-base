(ns std.lang.base.impl-template
  (:require [std.lang.base.preprocess :as preprocess]
            [std.lang.base.emit-rewrite :as rewrite]
            [std.lang.base.grammar-xtalk-system :as xtalk-system]
            [std.lib.collection :as collection]
            [std.lib.walk :as walk]))

(defn infer-static-template
  "determines whether any hydrated form head resolves to a hard-link in the current grammar"
  {:added "4.1"}
  [grammar form]
  (let [template? (volatile! false)]
    (walk/prewalk
     (fn [x]
       (when (and (collection/form? x)
                  (symbol? (first x))
                  (= :hard-link (get-in grammar [:reserved (first x) :emit])))
         (vreset! template? true))
       x)
     form)
    @template?))

(defn create-code-state
  "hydrates and stages a code entry for the current grammar"
  {:added "4.1"}
  [entry reserved grammar modules & [mopts]]
  (let [module (or (get modules (:module entry))
                   (:module mopts))
        module (cond-> module
                 (map? module) (assoc :display :brief))
        context (merge {:module module
                        :entry  (assoc entry :display :brief)}
                       mopts)
        [hmeta form-hydrate] (let [{:keys [hydrate]} reserved]
                               (if hydrate
                                 (hydrate (:form-input entry) grammar context)
                                 [nil (:form-input entry)]))
        [form-staged
         deps
         deps-fragment
         deps-native] (preprocess/to-staging form-hydrate
                                             grammar
                                             modules
                                             context)
        form-rewrite (rewrite/rewrite-stage :staging
                                            form-staged
                                            grammar
                                            context)
        {:keys [ops profiles polyfill-modules]}
        (xtalk-system/scan-xtalk form-rewrite)]
    {:hmeta hmeta
     :form form-rewrite
     :deps deps
     :deps-fragment deps-fragment
     :deps-native deps-native
     :xtalk-ops ops
     :xtalk-profiles profiles
     :polyfill-modules polyfill-modules
     :static/template (or (:static/template entry)
                          (infer-static-template grammar form-hydrate))}))

(defn cached-code-state
  "restages a template entry for the current language, using the per-entry cache when available"
  {:added "4.1"}
  [entry reserved grammar modules & [mopts]]
  (let [lang     (or (:lang mopts) (:lang entry))
        compute  #(create-code-state entry reserved grammar modules mopts)
        cache    (:static/template.cache entry)]
    (if (and (:static/template entry)
             cache
             lang)
      (get (swap! cache
                  (fn [m]
                    (if (contains? m lang)
                      m
                      (assoc m lang (compute)))))
           lang)
      (compute))))

(defn cached-entry-deps
  "restages a template entry and returns its current code dependencies for the book language"
  {:added "4.1"}
  [{:keys [modules grammar lang]} entry]
  (let [reserved (get-in grammar [:reserved (:op entry)])]
    (:deps (cached-code-state entry
                              reserved
                              grammar
                              modules
                              {:lang lang}))))
