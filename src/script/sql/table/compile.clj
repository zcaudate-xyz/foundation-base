(ns script.sql.table.compile
  (:require [std.json :as json]
            [std.lib.collection]
            [std.lib.invoke :refer [definvoke]]))

(def ^:dynamic *skip* nil)

(def ^:dynamic *enum->str* nil)

(definvoke in:fn-map
  "constructs an function map for sql input"
  {:added "3.0"}
  [:memoize]
  ([schema table]
   (reduce-kv (fn [out k [{:keys [type sql] :as attrs}]]
                (let [f (case type
                          :alias  nil
                          :ref    (fn [v]
                                    (if (keyword? v)
                                      [(keyword (str (name k) "-id"))
                                       (name v)]
                                      [k v]))
                          :enum   (fn [v]
                                    [k ((or *enum->str* name) v)])
                          :string (case (:format sql)
                                    :edn  (fn [v] [k (pr-str v)])
                                    :json (fn [v] [k (json/write v)])
                                    nil)
                          nil
                          ;;(fn [v] [k v])
                          )]
                  (cond-> out
                    f (assoc k f))))
              {}
              (get (:tree schema) table))))

(definvoke out:fn-map
  "constructs a function map for sql output"
  {:added "3.0"}
  [:memoize]
  ([schema table]
   (reduce-kv (fn [out k [{:keys [type sql] :as attrs}]]
                (let [[k f] (case type
                              :alias  nil
                              :ref    (let [rns  (-> attrs :ref :ns name)]
                                        [(keyword (str (name k) "-id"))
                                         (fn [v]
                                           [k (keyword (str rns ".id") v)])])
                              :enum   [k (fn [v] [k (keyword v)])]
                              :string [k (case (:format sql)
                                           :edn  (fn [v] [k (read-string v)])
                                           :json (fn [v] [k (json/read v)])
                                           nil)]
                              nil;;[k (fn [v] [k v])]
                              )]
                  (cond-> out
                    f (assoc k f))))
              {}
              (get (:tree schema) table))))

(defn transform:fn
  "constructs a data transform function"
  {:added "3.0"}
  ([fn-map schema table]
   (let [m (fn-map schema table)]
     (fn [data]
       (if *skip*
         data
         (std.lib.collection/map-entries (fn [[k v]]
                          (if-let [f (get m k)]
                            (f v)
                            [k v]))
                        data))))))

(defn transform
  "constructs a transform function for data pipeline"
  {:added "3.0"}
  ([schema table tm-fns array]
   (let [f (->> tm-fns
                (map #(transform:fn % schema table))
                reverse
                (apply comp))]
     (map f array))))

(defn transform:in
  "transforms data in"
  {:added "3.0"}
  ([data table {:keys [schema]}]
   ((transform:fn in:fn-map schema table) data)))

(defn transform:out
  "transforms data out"
  {:added "3.0"}
  ([data table {:keys [schema]}]
   ((transform:fn out:fn-map schema table) data)))
