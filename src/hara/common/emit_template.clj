(ns hara.common.emit-template
  (:require [hara.common.emit-preprocess :as preprocess]
              [hara.common.emit-rewrite :as rewrite]
              [hara.common.grammar-xtalk-system :as xtalk-system]))

(def +code-state-keys+
  [:form
   :deps
   :deps-fragment
   :deps-native
   :xtalk-ops
   :xtalk-profiles
   :polyfill-modules])

(defn entry-reserved
  "gets the reserved grammar entry for a code entry"
  {:added "4.1"}
  [grammar entry]
  (get-in grammar [:reserved (:op entry)]))

(defn create-code-state
  "hydrates and stages a code entry for the current grammar"
  {:added "4.1"}
  [entry reserved grammar modules & [mopts]]
  (let [module (or (:module mopts)
                   (get modules (:module entry)))
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
        (xtalk-system/scan-xtalk form-rewrite)
        deps (into (or deps #{})
                   (xtalk-system/xtalk-ops-polyfill-symbols ops))]
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

(defn materialize-code-entry
  "materializes a code entry for the target language using the cached code state"
  {:added "4.1"}
  ([{:keys [grammar modules lang] :as book} entry]
   (materialize-code-entry book entry {}))
  ([{:keys [grammar modules lang] :as _book} entry mopts]
   (let [reserved (entry-reserved grammar entry)
         mopts    (merge {:lang   (or (:lang mopts) lang)
                          :module (or (:module mopts)
                                      (get modules (:module entry)))}
                         mopts)]
     (if (and reserved modules)
       (let [{:keys [hydrate-hook]} reserved
             {:keys [hmeta]
              :as state} (cached-code-state entry
                                            reserved
                                            grammar
                                            modules
                                            mopts)
             entry (merge entry
                          (dissoc state :hmeta)
                          hmeta)]
         (if hydrate-hook
           (or (hydrate-hook entry) entry)
           entry))
       entry))))

(defn cached-entry-deps
  "restages a code entry and returns its current code dependencies for the book language"
  {:added "4.1"}
  [{:keys [modules grammar lang]} entry]
  (let [reserved (entry-reserved grammar entry)]
    (:deps (cached-code-state entry
                              reserved
                              grammar
                              modules
                              {:lang lang}))))
