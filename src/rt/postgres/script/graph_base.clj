(ns rt.postgres.script.graph-base
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.library-snapshot :as snap]
            [std.string :as str]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.script.impl-base :as base]
            [rt.postgres.script.impl-main :as main]
            [rt.postgres.script.impl-update :as update]))

(defn where-pair-ref
  "constructs the where ref pair"
  {:added "4.0"}
  [[k v] where-fn attrs {:keys [schema
                                book]
                         :as mopts}]
  (let [{:keys [ref]} attrs
        {:keys [type link rval]} ref
        v  (cond (or (map? v)
                     (ut/hashvec? v))
                 v
                 
                 :else
                 {:id v})]
    (cond (and (= type :forward)
               (map? v)
               (= [:id] (keys v))
               (or (not (coll? (:id v)))
                   (:fn/value (meta (:id v)))))
          [k [:eq (:id v)]]

          (= v :*)
          [k :*]
          
          (and (= type :forward)
               (map? v)
               (= [:id] (keys v))
               (vector? (:id v)))
          [k (:id v)]
          
          :else
          (let [{:keys [ref]} attrs
                {:keys [type link rval]} (or ref (h/error "Not found." {:attrs attrs}))
                nspec-sym (ut/sym-full link)
                nentry (book/get-base-entry book
                                            (:module link)
                                            (:id link)
                                            (:section link))
                ntsch   (get-in schema [:tree (keyword (name (:id link)))])]
            (cond (= :reverse type)
                  [:id [:in (main/t-select-raw
                             [nentry ntsch mopts]
                             {:returning #{rval} 
                              :where (where-fn ntsch v mopts)
                              :as :raw})]]

                  :else
                  (let [clause (where-fn ntsch v mopts)
                        multi  (or (map? v)
                                   (set? v))]
                    
                    [k [(if multi :in :eq)
                        (main/t-id-raw 
                         [nentry ntsch mopts]
                         {:where clause
                          :as :raw
                          :single (not multi)})]]))))))

(defn where-pair-fn
  "constructs the where pair"
  {:added "4.0"}
  [[k v] where-fn tsch mopts]
  (let [k (cond (string? k)
                ((str/wrap str/spear-case)
                 (if (str/ends-with? k "_id")
                   (keyword (subs k 0 (- (count k) 3)))
                   (keyword k)))
                
                :else k)]
    
    (if-let [[{:keys [type] :as attrs}] (get tsch k)]
      (case type
        :ref (where-pair-ref [k v] where-fn attrs mopts)
        (if (vector? v)
          [k v]
          [k [:eq v]]))
      (h/error "Key not found" {:key k
                                :allowed (keys tsch)}))))

(defn where-fn
  "constructs where clause"
  {:added "4.0"}
  [tsch where {:keys [schema
                      snapshot]
               :as mopts}]
  (let [pair-fn (fn [[k v]]
                  (where-pair-fn [k v] where-fn tsch mopts))
        tf-fn     (fn [arr]
                    (let [results (map pair-fn arr)
                          collision (not= (count results)
                                          (count (set (map first results))))]
                      [collision results]))]
    (cond (map? where)
          (let [[collision results] (tf-fn where)]
            (if (not collision)
              (into {} results)
              (hash-set (vec (mapcat identity results)))))
          
          (ut/hashvec? where)
          (base/t-where-hashvec where (comp second tf-fn))

          (nil? where) nil
          
          :else
          (h/error "Entry not valid." {:tsch (keys tsch)
                                       :input where}))))

(defn id-where-fn
  "constructs id-fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         t-key   (-> entry :static/schema-seed :vec first)
         t-spec  (-> entry :static/schema-seed :tree t-key)
         output  (where-fn tsch where mopts)]
     [(h/map-keys (fn [k]
                    (cond (-> t-spec k first :type (= :ref))
                          (keyword (str (name k) "_id"))

                          :else k))
                  output)])))

(defn id-fn
  "constructs id-fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (main/t-id-raw [entry tsch mopts] (assoc params :where where)))))

(defn count-fn
  "constructs count-fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (main/t-count-raw [entry tsch mopts] (assoc params :where where)))))

(defn exists-fn
  "constructs exists-fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (main/t-exists-raw [entry tsch mopts] (assoc params :where where)))))

(defn select-fn-raw
  "constructs a select fn with prep"
  {:added "4.0"}
  ([[entry tsch mopts]  {:keys [where] :as params}]
   (let [where   (where-fn tsch where mopts)]
     (main/t-select-raw [entry tsch mopts] (assoc params :where where)))))

(defn select-fn
  "constructs a select fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))]
     (select-fn-raw [entry tsch mopts] params))))

(defn delete-fn
  "constructs a delete fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (main/t-delete-raw [entry tsch mopts] (assoc params :where where)))))

(defn update-fn
  "constructs an update fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (update/t-update-raw [entry tsch mopts] (assoc params :where where)))))

(defn modify-fn
  "constructs an modify fn"
  {:added "4.0"}
  ([spec-sym {:keys [where] :as params}]
   (let [[entry tsch mopts] (base/prep-table spec-sym true (l/macro-opts))
         where   (where-fn tsch where mopts)]
     (update/t-modify-raw [entry tsch mopts] (assoc params :where where)))))
