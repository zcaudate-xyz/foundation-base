(ns xt.module
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl-lifecycle :as lc]
            [std.lang.base.impl-deps :as deps]))

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
                   (ffirst (h/filter-vals (fn [v] (= v '-))
                                          internal)))]
    (book/get-module (snap/get-book snapshot lang)
                     curr)))

(defn linked-natives
  "gets all linked natives"
  {:added "4.0"}
  ([lang]
   (linked-natives lang (h/ns-sym)))
  ([lang nss]
   (let [book (l/get-book (l/default-library)
                          lang)]
     (->> (h/deps:ordered  book (h/seqify nss))
          (map (comp :native (:modules book)))
          (apply merge)))))

(defn current-natives
  "gets the current natives"
  {:added "4.0"}
  ([lang]
   (current-natives lang (h/ns-sym)))
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
       (h/postwalk (fn [x]
                     (if (or (symbol? x)
                             (keyword? x))
                       (h/strn x)
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
  (h/strn (:id *saved-module*)))
