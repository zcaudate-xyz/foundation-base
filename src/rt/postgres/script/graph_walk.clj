(ns rt.postgres.script.graph-walk
  (:require [std.lib.collection]
            [std.lib.foundation]
            [std.lib.schema :as schema]
            [std.lib.transform :as transform]
            [std.lib.transform.base.ref :as ref]
            [std.lib.transform.link :as link])
  (:refer-clojure :exclude [flatten]))

;;
;; adds ids to the data
;;

(defn wrap-seed-id
  "seeds ids for missing primary keys in tree"
  {:added "4.0"}
  ([f]
   (fn [tdata tsch nsv interim fns {:keys [pipeline] :as datasource}]
     (if (vector? (second (first tsch)))
       (let [[pkey pmeta]  (first (keep (fn [[k [{:keys [primary sql type]}]]]
                                          (if primary [k {:sql sql :type type}]))
                                        tsch))
             tdata (if (get tdata pkey)
                     tdata
                     (let [psym (symbol (format "?id-%02d" (std.lib.foundation/inc! (:counter pipeline))))]
                       (assoc tdata pkey (with-meta psym pmeta))))]
         (f tdata tsch nsv interim fns datasource))
       (f tdata tsch nsv interim fns datasource)))))

(defn wrap-sym-id
  "allow strings and symbols in primary key"
  {:added "4.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]
     (if (and (= (:type attr) :ref)
              (or (symbol? subdata)
                  (string? subdata)
                  (std.lib.collection/form? subdata)))
       subdata
       (f subdata [attr] nsv interim fns datasource)))))

(defn wrap-link-attr
  "adds link information to tree"
  {:added "4.0"}
  ([f]
   (fn [subdata [attr] nsv interim fns datasource]
     (let [interim  (if (and (= (:type attr) :ref)
                             (= (:type (:ref attr)) :forward))
                      (assoc-in interim
                                [:link :parent]
                                (if (-> attr :cardinality (= :many))
                                  {(-> attr :ref :rval)
                                   (:id (-> interim :link :current))}))
                      interim)
           subdata  (if (and (= (:type attr) :ref)
                             (= (:type (:ref attr)) :reverse))
                      (let [{:keys [ns rval]} (:ref attr)
                            rtsch  (get-in datasource
                                           [:schema :tree ns])
                            rpkey  (first (keep (fn [[k [{:keys [primary]}]]]
                                                  (if primary k))
                                                rtsch))
                            sub-fn (fn [subdata]
                                     (assoc subdata rval (get (-> interim :link :current) rpkey)))]
                        (if (vector? subdata)
                          (mapv sub-fn subdata)
                          (sub-fn subdata)))
                      subdata)]
       (f subdata [attr] nsv interim fns datasource)))))

(defn link-data
  "adds missing ids to tree"
  {:added "4.0"}
  ([data schema]
   (let [counter    (std.lib.foundation/counter)
         wrap-main  [link/wrap-link-current wrap-seed-id]
         result     (transform/normalise-base data
                                              {:schema schema
                                               :pipeline {:counter counter}}
                                              {:normalise        wrap-main 
                                               :normalise-branch wrap-main
                                               :normalise-attr   [wrap-link-attr]
                                               :normalise-single [wrap-sym-id]})]
     result)))

;;
;; outputs the data
;;

(defn wrap-output
  "adds the flattened data to output"
  {:added "4.0"}
  ([f]
   (fn [tdata tsch nsv interim fns {:keys [pipeline] :as datasource}]
     (let [^clojure.lang.Volatile state (-> interim :flatten)]
       (if (vector? (second (first tsch)))
         (let [tkeys (keep (fn [[k [{:keys [order]}]]]
                             (if order k))
                           tsch)
               rkeys (keep (fn [[k [{:keys [type order]}]]]
                             (if (and (= type :ref)
                                      order)
                               k))
                           tsch)
               odata (-> (merge (-> interim :link :parent)
                                tdata)
                         (select-keys tkeys))
               odata (reduce (fn [odata k]
                               (let [v (get odata k)]
                                 (if (map? v)
                                   (assoc odata k (:id v))
                                   odata)))
                             odata
                             rkeys)]
           (vswap! state update (first nsv)
                   (fnil #(conj % odata)
                         []))))
       (f tdata tsch nsv interim fns datasource)))))

(defn flatten-data
  "converts tree to flattened data by table"
  {:added "4.0"}
  ([data schema]
   (let [output     (volatile! {})
         wrap-main  [wrap-output]
         result     (transform/normalise-base data
                                              {:schema schema
                                               :pipeline {:flatten output}}
                                              {:normalise        wrap-main 
                                               :normalise-branch wrap-main
                                               :normalise-attr   []
                                               :normalise-single [wrap-sym-id]})]
     @output)))
