(ns rt.postgres.script.graph-insert
  (:require [clojure.string]
            [rt.postgres.base.application :as app]
            [rt.postgres.script.graph-walk :as walk]
            [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-insert :as insert]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.sort :as sort]
            [std.lib.walk]))

(defn insert-walk-ids
  "inserts walk ids to the entries"
  {:added "4.0"}
  ([data]
   (let [cnt   (f/counter)
         data  (std.lib.walk/prewalk (fn [x]
                            (cond (instance? java.util.Map$Entry x) x
                                  
                                  (vector? x)
                                  (with-meta x {:walk/id (f/inc! cnt)})
                                  
                                  (map? x)
                                  (let [i (f/inc! cnt)]
                                    (if (:id x)
                                      (with-meta x {:walk/id i})
                                      (with-meta x {:walk/id i
                                                    :walk/data true})))
                                  
                                  :else
                                  x))
                          data)]
     data)))

(defn insert-generate-graph-tree
  "generates a graph tree from nodes"
  {:added "4.0"}
  ([data]
   (let [lu-gid   (volatile! {})
         gid-fn   (comp :walk/id meta)
         gdata-fn (comp :walk/data meta)
         data     (std.lib.walk/postwalk (fn [x]
                                (cond (vector? x)
                                      (let [gid (gid-fn x)]
                                        (vswap! lu-gid assoc gid (mapv gid-fn x)))
                                      
                                      (map? x)
                                      (let [gid   (gid-fn x)
                                            gdata (gdata-fn x)]
                                        (if (and gid (not gdata))
                                          (let [x  (collection/filter-vals (fn [v]
                                                                    (and (or (vector? v)
                                                                             (map? v))
                                                                         (not (gdata-fn v))))
                                                                  x)]
                                            (vswap! lu-gid assoc gid
                                                    (collection/map-vals gid-fn x))))))
                                x)
                              data)
         _  (vswap! lu-gid dissoc nil)]
     [data @lu-gid])))

(defn insert-associate-graph-data
  "associate nodes wit h graph data"
  {:added "4.0"}
  [data lu-gid]
  (let [lu-data  (volatile! {})
        gid-fn (comp :walk/id meta)

        ;; associate data with graph
        data     (std.lib.walk/postwalk (fn [x]
                               (if (and (map? x)
                                        (:id x))
                                 (vswap! lu-data assoc (:id x) (gid-fn x)))
                               x)
                             data)
        gid-syms  (collection/map-juxt [identity
                               (fn [gid]
                                 (symbol (str "gid_" gid)))]
                              (keys lu-gid))
        
        ;; get deps and order for graph ids
         gid-ordered (-> (collection/map-vals  (fn [x]
                                      (if (vector? x)
                                        (set x)
                                        (set (vals x))))
                                      lu-gid)
                         (sort/topological-sort))]
    [gid-ordered gid-syms @lu-data]))

(defn insert-gen-sql
  "generates sql given graph"
  {:added "4.0"}
  ([data params {:keys [schema
                        book
                        application]
                 :as mopts}]
   (let [data          (insert-walk-ids data)
         [data lu-gid] (insert-generate-graph-tree data)
         [gid-ordered gid-syms lu-data] (insert-associate-graph-data data lu-gid)
         
         ;; generate sql forms
         pids   (filter (fn [form]
                          (and (symbol? form)
                               (clojure.string/starts-with? (str form) "?")))
                        (keys lu-data))
         pdecl  (mapcat (fn [sym]
                          (let [{:keys [type sql]} (meta sym)]
                            [(list type sym) (:default sql)]))
                        pids)
         data   (->> (walk/flatten-data data schema)
                     (sort-by (comp (:lu application) first)))
         ddecl  (mapcat (fn [[k arr]]
                          (let [tsch   (get-in schema [:tree k])
                                ptr    (get-in application [:pointers (symbol (name k))])
                                entry (book/get-base-entry book
                                                           (:module ptr)
                                                           (:id ptr)
                                                           (:section ptr))
                                {:static/keys [tracker]} entry]
                            (mapcat (fn [m]
                                      (let [gid   (get lu-data (get m :id))
                                            gsym  (gid-syms gid)]
                                        [gsym (insert/t-insert-raw
                                               [entry tsch mopts]
                                               m
                                               (cond-> (merge {:returning '*
                                                               :single true}
                                                              params)
                                                 tracker (assoc :static/tracker tracker)))]))
                                    arr)))
                        data)
         
         ;; reconstructing graph
         jsdecl (mapcat (fn [gid]
                          (let [gsym  (gid-syms gid)
                                gdeps (get lu-gid gid)]
                            (cond (empty? gdeps) []

                                  (map? gdeps)
                                  ['_  (list := gsym (list '|| gsym (collection/map-vals gid-syms gdeps)))]

                                  (vector? gdeps)
                                  [gsym  (list 'js (mapv gid-syms gdeps))])))
                        gid-ordered)
         assign-fn (fn [into]
                     `(~'let [~@pdecl ~@ddecl ~@jsdecl]
                       [:select ~(gid-syms (last gid-ordered))
                        :into ~into]))]
     (with-meta `(~'let [~@pdecl ~@ddecl])
       {:assign/fn assign-fn}))))

(defn insert-fn-raw
  "constructs insert form with prep"
  {:added "4.0"}
  ([[entry tsch mopts] data params]
   (let [{:static/keys [application]} entry
         mopts (cond-> mopts
                 application (assoc :application (app/app (first application))))
         {:keys [schema]} mopts
         kspec  (keyword (name (:id entry)))
         
         data  (-> {kspec data}
                   (walk/link-data schema))]
     (insert-gen-sql data params mopts))))

(defn insert-fn
  "constructs insert form"
  {:added "4.0"}
  ([spec-sym data params]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         {:keys [book]} mopts]
     (insert-fn-raw [entry tsch mopts]
                    data
                    params))))

(comment
  (./create-tests))
