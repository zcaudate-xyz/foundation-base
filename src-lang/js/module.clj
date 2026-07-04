(ns js.module
  (:require [clojure.string]
            [hara.lang :as l]
            [hara.lang.impl-deps :as impl-deps]
            [std.lib.foundation :as f]
            [xt.lang.common-module :as module]))

(l/script :js js.module)

(defmacro.js import-missing
  "generates all dependent imports missing from current namespace"
  {:added "4.0"}
  []
  (->> (clojure.core/keys (module/current-natives :js))
       (clojure.core/apply dissoc (module/linked-natives :js))
       (clojure.core/map (fn [[k m]]
                           (impl-deps/module-import-form (l/get-book (l/default-library)
                                                                     :js)
                                                         k
                                                         m
                                                         {})))
       (clojure.core/apply list 'do)))

(defmacro.js import-set-global
  "sets all dependent imports to global"
  {:added "4.0"}
  [& [exclude]]
  (let [form-fn (fn [sym]
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
        output (->> (module/linked-natives :js)
                    (clojure.core/mapcat
                     (fn [[pkg {:keys [as]}]]
                       (cond (set? as)
                             (mapv form-fn as)

                             :else
                             (let [sym (if (vector? as)
                                         (clojure.core/last as)
                                         as)]
                               (if (and sym
                                        (not ((or exclude #{})
                                              sym)))
                                 [(form-fn sym)])))))
                    (clojure.core/keep clojure.core/identity)
                    (clojure.core/apply list 'do))]
    output))
