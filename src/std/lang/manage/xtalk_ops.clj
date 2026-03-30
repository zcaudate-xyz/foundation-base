(ns std.lang.manage.xtalk-ops
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [code.project :as project]
            [std.fs :as fs]
            [std.lang.base.grammar :as grammar]
            [std.lang.typed.xtalk-ops :as ops]))

(def ^:dynamic *xtalk-ops-path*
  "config/xtalk/xtalk_ops.edn")

(defn var->symbol
  [x]
  (cond
    (var? x)
    (symbol (str (ns-name (:ns (meta x))))
            (str (:name (meta x))))

    (symbol? x)
    x

    :else
    x))

(defn xtalk-category-map
  []
  (->> grammar/+op-all+
       (keep (fn [[category entries]]
               (when (str/starts-with? (name category) "xtalk")
                 (keep (fn [[op entry]]
                         (when (and (keyword? op)
                                    (map? entry))
                           [op category]))
                       entries))))
       (mapcat identity)
       (into {})))

(defn grammar-xtalk-entries
  []
  (->> (grammar/ops-list)
       (filter #(str/starts-with? (name %) "xtalk-"))
       (mapcat (fn [category]
                 (for [[op entry] (grammar/ops-detail category)]
                   (when (and (keyword? op)
                              (map? entry))
                     (assoc entry :op op)))))
       (remove nil?)
       vec))

(defn read-xtalk-ops
  ([path]
   (when (fs/exists? path)
     (edn/read-string (slurp path)))))

(defn compact-entry
  [m]
  (reduce-kv (fn [out k v]
               (if (nil? v)
                 out
                 (assoc out k v)))
             {}
             m))

(defn symbol-doc
  [sym]
  (when (symbol? sym)
    (when-let [ns-sym (some-> sym namespace symbol)]
      (require ns-sym))
    (when-let [v (or (resolve sym)
                     (when-let [ns-sym (some-> sym namespace symbol)]
                       (ns-resolve ns-sym (symbol (name sym)))))]
      (:doc (meta v)))))

(defn inventory-entry
  [entry category existing]
  (let [canonical-symbol (ops/canonical-symbol-from-entry entry)
        symbols (->> (:symbol entry)
                     (filter symbol?)
                     (sort-by str)
                     vec)]
    (merge
     (-> {:op (:op entry)
          :category category
          :canonical-symbol canonical-symbol
          :symbols symbols
          :class (:class entry)
          :requires (not-empty (vec (sort-by str (:requires entry))))
          :emit (:emit entry)
          :type (:type entry)
          :macro (var->symbol (:macro entry))
          :raw (var->symbol (:raw entry))
          :doc (or (:doc existing)
                   (symbol-doc (var->symbol (:macro entry))))
          :default (:default entry)
          :cases []}
         compact-entry)
     (select-keys existing [:status
                            :notes
                            :cases
                            :impls
                            :scaffold
                            :skip?]))))

(defn inventory-entries
  ([] (inventory-entries nil))
  ([existing]
   (let [category-map (xtalk-category-map)
         existing-map (into {}
                            (map (fn [entry]
                                   [(:op entry) entry]))
                            (or existing []))]
     (->> (grammar-xtalk-entries)
          (keep (fn [entry]
                  (when-let [category (get category-map (:op entry))]
                    (inventory-entry entry
                                     category
                                     (get existing-map (:op entry))))))
          (sort-by (juxt (comp name :category)
                         (comp str :canonical-symbol)))
          vec))))

(defn ops-path
  ([project]
   (ops-path project nil))
  ([project path]
   (str (fs/path (:root project)
                 (or path *xtalk-ops-path*)))))

(defn render-xtalk-ops
  [entries]
  (binding [pprint/*print-right-margin* 100
            *print-length* nil
            *print-level* nil]
    (with-out-str
      (pprint/pprint entries))))

(defn generate-xtalk-ops
  "Generates an xtalk operator inventory EDN from the grammar tables."
  ([_ {:keys [path write]
       :or {write false}}]
   (let [proj (project/project)
         out-path (ops-path proj path)
         existing (read-xtalk-ops out-path)
         entries (inventory-entries existing)
         content (render-xtalk-ops entries)
         original (when (fs/exists? out-path)
                    (slurp out-path))
         updated (not= original content)]
     (when write
       (fs/create-directory (fs/parent out-path))
       (spit out-path content))
     {:path out-path
      :count (count entries)
      :updated updated
      :entries entries})))
