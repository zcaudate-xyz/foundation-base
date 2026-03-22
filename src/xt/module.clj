(ns xt.module
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-lifecycle :as lc]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lib.collection]
            [std.lib.deps]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.walk]))

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
                   (ffirst (std.lib.collection/filter-vals (fn [v] (= v '-))
                                          internal)))]
    (book/get-module (snap/get-book snapshot lang)
                     curr)))

(defn linked-natives
  "gets all linked natives"
  {:added "4.0"}
  ([lang]
   (linked-natives lang (std.lib.env/ns-sym)))
  ([lang nss]
   (let [book (l/get-book (l/default-library)
                          lang)]
     (->> (std.lib.deps/deps-ordered  book (std.lib.collection/seqify nss))
          (map (comp :native (:modules book)))
          (apply merge)))))

(defn current-natives
  "gets the current natives"
  {:added "4.0"}
  ([lang]
   (current-natives lang (std.lib.env/ns-sym)))
  ([lang ns]
   (get-in (l/get-book (l/default-library)
                       lang)
           [:modules
            ns
            :native])))

(defn expose-module
  "helper function for additional libs"
  {:added "4.0"}
  [key module-id]
  (->> (get (current-module module-id) key)
       (std.lib.walk/postwalk (fn [x]
                     (if (or (symbol? x)
                             (keyword? x))
                       (std.lib.foundation/strn x)
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
  (std.lib.foundation/strn (:id *saved-module*)))
