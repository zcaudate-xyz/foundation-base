(ns xt.lang.common-module
  (:require [lang.core :as l]
            [lang.base.book :as book]
            [lang.core.impl-deps :as deps]
            [lang.core.impl-deps-imports :as imports]
            [lang.core.impl-lifecycle :as lc]
            [lang.core.library :as lib]
            [lang.core.library-snapshot :as snap]
            [std.lib.collection :as collection]
            [std.lib.deps]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))

(defonce ^:dynamic *saved-module* nil)

(l/script :xtalk
  {})

(defn current-module
  "gets the current module"
  {:added "4.0"}
  [module-id]
  (let [{:keys [lang snapshot emit]} (l/macro-opts)
        internal (-> emit :runtime :module/internal)
        curr   (or (if module-id (symbol (str module-id)))
                   (ffirst (collection/filter-vals (fn [v] (= v '-))
                                                  internal)))]
    (book/get-module (snap/get-book snapshot lang)
                     curr)))

(defn- module-natives
  "gets the native imports used by one module"
  [book ns]
  (when-let [module (get-in book [:modules ns])]
    (let [mopts        (l/macro-opts)
          entry-id     (some-> mopts :entry :id)
          entry-module (some-> mopts :module :id)
          entries      (->> (:code module)
                            vals
                            ;; import-missing is expanded from this entry. Do
                            ;; not materialize it while collecting its imports.
                            (remove (fn [entry]
                                      (and (= ns entry-module)
                                           (= (:id entry) entry-id)))))]
      (merge (:native module)
             (imports/script-imports book entries)))))

(defn linked-natives
  "gets native imports used by the linked code"
  {:added "4.0"}
  ([lang]
   (linked-natives lang (env/ns-sym)))
  ([lang nss]
   (let [book (l/get-book (l/default-library) lang)]
     (->> (collection/seqify nss)
          (map #(or (module-natives book %) {}))
          (apply merge)))))

(defn current-natives
  "gets native imports used by the current namespace"
  {:added "4.0"}
  ([lang]
   (current-natives lang (env/ns-sym)))
  ([lang ns]
   (linked-natives lang ns)))

(defn expose-module
  "helper function for additional libs"
  {:added "4.0"}
  [key module-id]
  (->> (get (current-module module-id) key)
       (walk/postwalk (fn [x]
                        (if (or (symbol? x)
                                (keyword? x))
                          (f/strn x)
                          x)))))

(defmacro.xt module-native
  "returns the native map"
  {:added "4.0"}
  [& [module-id]]
  (expose-module :native module-id))

(defmacro.xt module-link
  "returns the module link map"
  {:added "4.0"}
  [& [module-id]]
  (expose-module :link module-id))

(defmacro.xt module-internal
  "returns the module link map"
  {:added "4.0"}
  [& [module-id]]
  (expose-module :internal module-id))

(defmacro.xt module-save
  "saves module to `module/*saved-module*` var"
  {:added "4.0"}
  [& [module-id]]
  (alter-var-root #'*saved-module* (fn [_] (current-module module-id)))
  (f/strn (:id *saved-module*)))
