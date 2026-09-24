(ns js.module
  (:require [clojure.string]
            [lang.core :as l]
            [lang.core.impl-deps :as impl-deps]
            [std.lib.foundation :as f]
            [xt.lang.common-module :as module]))

(l/script :js js.module)

(defmacro.js import-missing
  "generates all dependent imports missing from current namespace"
  {:added "4.0"}
  []
  (let [module-args (clojure.core/filter
                     (fn [module-id]
                       (and module-id
                            (not= module-id (clojure.core/ns-name *ns*))))
                     [(some-> (l/macro-opts) :module :id)])]
    (->> (clojure.core/keys (clojure.core/apply module/current-natives
                                               :js
                                               module-args))
         (clojure.core/apply dissoc
                             (clojure.core/apply module/linked-natives
                                                 :js
                                                 module-args))
         (clojure.core/map (fn [[k m]]
                             (impl-deps/module-import-form (l/get-book (l/default-library)
                                                                       :js)
                                                           k
                                                           m
                                                           {})))
         (clojure.core/apply list 'do))))

(defmacro.js import-set-global
  "sets all dependent imports to global"
  {:added "4.0"}
  [& [exclude]]
  (let [module-args (clojure.core/filter
                     (fn [module-id]
                       (and module-id
                            (not= module-id (clojure.core/ns-name *ns*))))
                     [(some-> (l/macro-opts) :module :id)])
        form-fn (fn [sym]
                  (let [sym-str (.replaceAll (clojure.core/name sym)
                                             "-"
                                             "_")]
                    (list 'when
                          (list 'not
                                (list '. 'globalThis
                                      (symbol sym-str)))
                          (list 'Object.defineProperty 'globalThis
                                sym-str
                                {:value sym
                                 :writeable true}))))
        output (->> (clojure.core/apply module/linked-natives :js module-args)
                    (clojure.core/mapcat
                     (fn [[pkg {:keys [as]}]]
                       (cond (set? as)
                             (mapv form-fn as)

                             :else
                             (let [sym (clojure.core/last
                                        (clojure.core/flatten [as]))]
                               (->> [sym]
                                    (filter (fn [sym]
                                              (and sym
                                                   (not ((or exclude #{}) sym)))))
                                    (map form-fn))))))
                    (clojure.core/keep clojure.core/identity)
                    (clojure.core/apply list 'do))]
    output))
