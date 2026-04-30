(ns std.lang.base.impl-template
  (:require [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-rewrite :as rewrite]
            [std.lang.base.grammar-xtalk-system :as xtalk-system]))

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
     :polyfill-modules polyfill-modules}))

(defn cached-code-state
  "restages a code entry for the current language, using the per-entry cache when available"
  {:added "4.1"}
  [entry reserved grammar modules & [mopts]]
  (let [lang     (or (:lang mopts) (:lang entry))
         compute  #(create-code-state entry reserved grammar modules mopts)
        cache    (:static/code.cache entry)]
    (if (and cache
             lang)
      (get (swap! cache
                  (fn [m]
                    (if (contains? m lang)
                      m
                      (assoc m lang (compute)))))
           lang)
      (compute))))

(defn cached-entry-deps
  "restages a code entry and returns its current code dependencies for the book language"
  {:added "4.1"}
  [{:keys [modules grammar lang]} entry]
  (let [reserved (get-in grammar [:reserved (:op entry)])]
    (:deps (cached-code-state entry
                              reserved
                              grammar
                              modules
                              {:lang lang}))))
