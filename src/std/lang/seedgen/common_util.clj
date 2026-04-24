(ns std.lang.seedgen.common-util
  (:require [std.fs :as fs]
             [std.lang.base.impl :as lang.impl]
             [std.lang.base.library :as lang.lib]))

;; --------------------------------------------------
;; script form discovery
;; --------------------------------------------------

(defn require-alias
  [require-spec target]
  (when (and (vector? require-spec)
             (= target (first require-spec)))
    (some->> (partition 2 (rest require-spec))
             (some (fn [[k v]]
                     (when (= :as k)
                       v))))))

(defn seedgen-script-heads
  [ns-form]
  (let [require-clause (some #(when (and (seq? %)
                                         (= :require (first %)))
                                %)
                             (nnext ns-form))
        std-lang-alias (some #(require-alias % 'std.lang)
                             (rest require-clause))]
    (cond-> #{'std.lang/script-}
      std-lang-alias
      (conj (symbol (str std-lang-alias) "script-")))))

(defn seedgen-root-langs
  [test-file include-root?]
  (let [forms (fs/read-code test-file)
        heads (seedgen-script-heads (first forms))]
    (->> (rest forms)
         (keep (fn [form]
                 (let [root? (:seedgen/root (meta form))]
                   (when (and (instance? clojure.lang.IObj form)
                              (= include-root? (boolean root?))
                              (seq? form)
                              (contains? heads (first form)))
                     (second form)))))
         vec)))

;; --------------------------------------------------
;; runtime dispatch discovery
;; --------------------------------------------------

(def ^:private +seedgen-runtime-reference-heads+
  '#{l/rt
     notify/wait-on
     notify/wait-on-fn
     notify/captured
     notify/captured:count
     notify/captured:clear})

(def ^:private +seedgen-dispatch-aliases+
  {"py" :python})

(def ^:private +seedgen-dispatch-tags+
  {:python :py})

(defn seedgen-dispatch-map
  []
  (let [library  (lang.impl/default-library)
         snapshot (lang.lib/get-snapshot library)]
    (merge +seedgen-dispatch-aliases+
           (reduce (fn [out {:keys [book]}]
                     (let [lang   (or (:lang book)
                                      (get-in book [:book :lang]))
                           tag    (or (get-in book [:grammar :tag])
                                      (get-in book [:book :grammar :tag]))
                           parent (or (:parent book)
                                      (get-in book [:book :parent]))]
                       (if (and lang
                                tag
                                (not (contains? snapshot parent)))
                         (assoc out (name tag) lang)
                         out)))
                   {}
                   (vals snapshot)))))

(defn seedgen-dispatch-tag-map
  []
  (let [library  (lang.impl/default-library)
        snapshot (lang.lib/get-snapshot library)]
    (merge +seedgen-dispatch-tags+
           (->> snapshot
                vals
                (keep (fn [{:keys [book]}]
                        (let [lang (or (:lang book)
                                       (get-in book [:book :lang]))
                              tag  (or (get-in book [:grammar :tag])
                                       (get-in book [:book :grammar :tag]))]
                          (when (and lang tag)
                            [lang tag]))))
                (into {})))))

(defn seedgen-normalize-runtime-lang
  [lang]
  (let [dispatch-map (seedgen-dispatch-map)
        lang (cond (keyword? lang) lang
                   (symbol? lang) (keyword (name lang))
                   (string? lang) (keyword lang)
                   :else lang)]
    (or (get dispatch-map (name lang))
        lang)))

(defn seedgen-dispatch-tag
  [lang]
  (let [tag-map (seedgen-dispatch-tag-map)
        lang    (seedgen-normalize-runtime-lang lang)]
    (or (get tag-map lang)
        lang)))

(defn seedgen-dispatch-lang
  [form]
  (when (seq? form)
    (let [head (first form)]
      (when (symbol? head)
        (let [n (name head)]
          (when (.startsWith ^String n "!.")
            (or (get (seedgen-dispatch-map) (subs n 2))
                (keyword (subs n 2)))))))))

(defn seedgen-runtime-reference-lang
  [form]
  (when (seq? form)
    (let [[head arg] form]
      (when (contains? +seedgen-runtime-reference-heads+ head)
        (cond
          (keyword? arg)
          (seedgen-normalize-runtime-lang arg)

          (and (vector? arg)
               (seq arg))
          (seedgen-normalize-runtime-lang (first arg))

          :else
          nil)))))

(defn seedgen-runtime-dispatch-langs
  [form]
  (letfn [(collect-langs [form]
            (cond (seq? form)
                  (if-let [lang (or (seedgen-dispatch-lang form)
                                    (seedgen-runtime-reference-lang form))]
                    #{lang}
                    (reduce into #{} (map collect-langs form)))

                  (vector? form)
                  (reduce into #{} (map collect-langs form))

                  (set? form)
                  (reduce into #{} (map collect-langs form))

                  (map? form)
                  (reduce into #{}
                          (concat (map collect-langs (keys form))
                                  (map collect-langs (vals form))))

                  :else
                  #{}))]
    (->> (collect-langs form)
         sort
         vec)))

;; --------------------------------------------------
;; fact coverage inspection
;; --------------------------------------------------

(defn seedgen-fact-forms
  [test-file]
  (->> (fs/read-code test-file)
       (keep (fn [form]
               (when (and (instance? clojure.lang.IObj form)
                          (seq? form)
                          (= 'fact (first form))
                          (:refer (meta form)))
                  [(:refer (meta form)) form])))
       (into {})))

(defn- normalize-seedgen-lang-entry
  [config]
  (cond
    (nil? config)
    nil

    (= true config)
    {}

    (map? config)
    config

    :else
    {:value config}))

(defn- normalize-seedgen-base-config
  [base-config]
  (cond
    (nil? base-config)
    nil

    (= true base-config)
    {:all {}}

    (map? base-config)
    (->> base-config
         (map (fn [[lang config]]
                [(if (= :all lang)
                   :all
                   (seedgen-normalize-runtime-lang lang))
                 (normalize-seedgen-lang-entry config)]))
         (into {}))

    :else
    {:all (normalize-seedgen-lang-entry base-config)}))

(defn- merge-seedgen-configs
  [& configs]
  (let [merged (apply merge-with merge
                      (map #(or % {}) configs))]
    (when (seq merged)
      merged)))

(defn seedgen-base-config
  [form]
  (letfn [(collect-config [form]
            (let [m          (when (instance? clojure.lang.IObj form)
                               (meta form))
                  own-config (merge-seedgen-configs
                              (normalize-seedgen-base-config (:seedgen/base m))
                              (normalize-seedgen-base-config (:seedgen/lang m))
                              (normalize-seedgen-base-config (:seedgen/check m)))]
              (cond
                (seq? form)
                (reduce (fn [out entry]
                          (merge-seedgen-configs out (collect-config entry)))
                        own-config
                        form)

                (vector? form)
                (reduce (fn [out entry]
                          (merge-seedgen-configs out (collect-config entry)))
                        own-config
                        form)

                (set? form)
                (reduce (fn [out entry]
                          (merge-seedgen-configs out (collect-config entry)))
                        own-config
                        form)

                (map? form)
                (reduce (fn [out entry]
                          (merge-seedgen-configs out (collect-config entry)))
                        own-config
                        (concat (keys form) (vals form)))

                :else
                own-config)))]
    (collect-config form)))

(defn seedgen-lang-config
  [form]
  (seedgen-base-config form))

(defn seedgen-lang-entry
  [form lang]
  (let [config (seedgen-base-config form)
        lang   (seedgen-normalize-runtime-lang lang)]
    (merge (get config :all)
           (get config lang))))

(defn seedgen-suppressed-langs
  [form]
  (->> (seedgen-lang-config form)
       (keep (fn [[lang config]]
               (when (:suppress config)
                 lang)))
       set))

(defn seedgen-coverage-langs
  [form]
  (let [m (meta form)]
    (seedgen-runtime-dispatch-langs [form
                                     (:setup m)
                                     (:teardown m)])))
