(ns code.manage.xtalk-audit
  (:require [clojure.string :as str]
            [std.lang.base.library-loader :as lib-loader]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.registry :as reg]))

(def +status-mark+
  {:implemented "Y"
   :abstract "A"
   :missing "."})

(defn xtalk-categories
  "returns all xtalk categories in declaration order"
  {:added "4.1"}
  []
  (->> (grammar/ops-list)
       (filter #(str/starts-with? (name %) "xtalk-"))
       vec))

(defn xtalk-op-map
  "returns xtalk op definitions keyed by op"
  {:added "4.1"}
  []
  (->> (xtalk-categories)
       (mapcat (fn [category]
                 (for [[op entry] (grammar/ops-detail category)]
                   [op (assoc entry :category category)])))
       (into (sorted-map))))

(defn xtalk-symbols
  "returns all x:* symbols in xtalk grammar"
  {:added "4.1"}
  []
  (->> (vals (xtalk-op-map))
       (mapcat :symbol)
       (filter #(str/starts-with? (name %) "x:"))
       distinct
       sort
       vec))

(defn installed-languages
  "loads all default books from the registry via lib-loader and returns installed languages"
  {:added "4.1"}
  []
  (let [library (impl/default-library)]
    (doseq [[lang key] (reg/registry-book-list)
            :when (= :default key)]
      (lib-loader/ensure-book! library lang key))
    (->> (keys (lib/get-snapshot library))
         sort
         vec)))

(defn xtalk-parent-languages
  "returns installed languages whose loaded book has `:parent :xtalk`"
  {:added "4.1"}
  []
  (let [library (impl/default-library)]
    (installed-languages)
    (->> (lib/get-snapshot library)
         (keep (fn [[lang {:keys [book]}]]
                 (when (= :xtalk (:parent book))
                   lang)))
         sort
         vec)))

(defn audit-languages
  "returns languages to include in xtalk support audits"
  {:added "4.1"}
  ([] (audit-languages nil))
  ([langs]
   (let [installed (set (installed-languages))
         selected  (or langs (xtalk-parent-languages))]
     (->> selected
          (filter installed)
          vec))))

(defn feature-status
  "returns xtalk feature status for a language"
  {:added "4.1"}
  [lang feature]
  (if-let [entry (get-in (impl/grammar lang) [:reserved feature])]
    (if (= :abstract (:emit entry))
      :abstract
      :implemented)
    :missing))

(defn support-matrix
  "returns xtalk support data per language and feature"
  {:added "4.1"}
  ([] (support-matrix nil nil))
  ([langs] (support-matrix langs nil))
  ([langs features]
   (let [langs    (audit-languages langs)
         features (vec (or features (xtalk-symbols)))
         status   (into {}
                        (for [lang langs]
                          [lang (into (sorted-map)
                                      (for [feature features]
                                        [feature (feature-status lang feature)]))]))
         summary  (into {}
                        (for [[lang entries] status]
                          [lang (merge {:implemented 0
                                        :abstract 0
                                        :missing 0}
                                       (frequencies (vals entries)))]))]
     {:languages langs
      :features features
      :status status
      :summary summary})))

(defn missing-by-language
  "returns non-implemented xtalk features grouped by language"
  {:added "4.1"}
  ([] (missing-by-language nil nil))
  ([langs] (missing-by-language langs nil))
  ([langs features]
   (let [{:keys [languages status]} (support-matrix langs features)]
     (into {}
           (for [lang languages]
             [lang (into (sorted-map)
                         (remove (comp #{:implemented} val))
                         (get status lang))])))))

(defn missing-by-feature
  "returns languages missing or leaving features abstract"
  {:added "4.1"}
  ([] (missing-by-feature nil nil))
  ([langs] (missing-by-feature langs nil))
  ([langs features]
   (let [{:keys [languages features status]} (support-matrix langs features)]
     (into (sorted-map)
           (for [feature features]
             [feature (into (sorted-map)
                            (for [lang languages
                                  :let [state (get-in status [lang feature])]
                                  :when (not= :implemented state)]
                              [lang state]))])))))

(defn- pad-right
  [s width]
  (let [s (str s)]
    (if (< (count s) width)
      (str s (apply str (repeat (- width (count s)) " ")))
      s)))

(defn- status-mark
  [status]
  (get +status-mark+ status "?"))

(defn- visualize-summary*
  [{:keys [languages summary]}]
  (let [rows  (for [lang languages]
                [(name lang)
                 (str (get-in summary [lang :implemented]))
                 (str (get-in summary [lang :abstract]))
                 (str (get-in summary [lang :missing]))])
        table (cons ["language" "implemented" "abstract" "missing"] rows)
        widths (apply map max (map #(map count %) table))]
    (str/join
     "\n"
     (map (fn [row]
            (str/join "  "
                      (map pad-right row widths)))
          table))))

(defn- visualize-matrix*
  [{:keys [languages features status]}]
  (let [rows   (for [feature features]
                 (cons (name feature)
                       (for [lang languages]
                         (status-mark (get-in status [lang feature])))))
        table  (cons (cons "feature" (map name languages)) rows)
        widths (apply map max (map #(map count %) table))]
    (str/join
     "\n"
     (concat
      ["Y=implemented  A=abstract-only  .=missing"]
      [(str/join "  "
                 (map pad-right (first table) widths))]
      (map (fn [row]
             (str/join "  "
                       (map pad-right row widths)))
           (rest table))))))

(defn visualize-support
  "renders a summary or matrix view of xtalk support"
  {:added "4.1"}
  ([] (visualize-support {}))
  ([{:keys [langs features view]
     :or {view :summary}}]
   (let [matrix (support-matrix langs features)]
     (case view
       :matrix  (visualize-matrix* matrix)
       :summary (visualize-summary* matrix)
       (throw (ex-info "Unsupported xtalk support view"
                       {:view view
                        :supported #{:summary :matrix}}))))))
