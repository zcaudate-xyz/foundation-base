(ns xt.db.cache-pull
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-util :as ut]
             [xt.db.base-scope :as scope]]})

(l/script :js
  {:require [[js.core :as j]
             [xt.db.base-util :as ut]
             [xt.db.base-scope :as scope]
             [xt.lang.base-lib :as k]]})

(defn.xt check-in-clause
  "emulates the sql `in` clause"
  {:added "4.0"}
  [x expr]
  (return (k/arr-some (k/first expr)
                      (fn:> [e] (== e x)))))

(defn.js check-like-clause
  "emulates the sql `like` clause"
  {:added "4.0"}
  [x expr]
  (return 
   (. (new RegExp
           (+ "^"
              (-> expr
                  (j/replaceAll "_" ".")
                  (j/replaceAll "%" ".*"))
              "$"))
      (test x))))

(defn.xt check-like-clause
  "emulates the sql `like` clause"
  {:added "4.0"}
  [x expr]
  (return true))

(def.xt PULL_LU
  {:reverse "rev_links"
   :forward "ref_links"})

(def.xt PULL_CHECK
  {"neq"  (fn [x expr]
            (return (k/neq x expr)))
   "eq"   k/eq
   "lt"   k/lt
   "lte"  k/lte
   "gt"   k/gt
   "gte"  k/gte
   "like" -/check-like-clause
   "in"   -/check-in-clause
   "between"  (fn:> [x start-expr _and end-expr]
                (and (>= x start-expr)
                     (:? (== _and "and")
                         (<= x end-expr)
                         (<= x _and))))
   "not_like" (fn:> [x expr] (not (-/check-like-clause x expr)))
   "not_in" (fn:> [x expr] (not (-/check-in-clause x expr)))
   "is_null" k/nil? #_(fn [x]
               (k/LOG! x)
               (return (== nil x)))
   "is_not_null"  k/not-nil? #_(fn:> [x] (not= nil x))})

(defn.xt check-clause-value
  "checks the clause within a record"
  {:added "4.0"}
  [record ktype key clause]
  (cond (== ktype "data")
        (return (== clause (k/get-in record ["data" key])))

        (== ktype "forward")
        (return (k/get-in record ["ref_links" key clause]))

        (== ktype "reverse")
        (return (k/get-in record ["rev_links" key clause]))))

(defn.xt check-clause-function
  "checks the clause for a function within a record"
  {:added "4.0"}
  [record ktype key pred exprs]
  (cond (k/nil? pred)
        (return false)

        (== ktype "data")
        (return (pred (k/get-in record ["data" key])
                      (k/unpack exprs)))
        
        (== ktype "forward")
        (cond (== pred (. -/PULL_CHECK ["is_null"]))
              (return (pred (k/get-in record ["ref_links" key])))
              
              :else
              (return (k/arr-some (k/obj-keys (or (k/get-in record ["ref_links" key])
                                                  {}))
                                  (fn:> [v] (pred v (k/unpack exprs))))))
        
        (== ktype "reverse")
        (return (k/arr-some (k/obj-keys (or (k/get-in record ["rev_links" key])
                                            {}))
                            (fn:> [v] (pred v (k/unpack exprs)))))))

(defn.xt pull-where-clause
  "pull where clause"
  {:added "4.0"}
  [rows schema table-key record where-fn key clause]
  (var ktype (or (k/get-in schema [table-key key "ref" "type"])
                 "data"))
  (cond (k/arr? clause)
        (do (var [tag] clause)
            (var exprs [(k/unpack clause)])
            (x:arr-pop-first exprs)
            (return
             (-/check-clause-function
              record ktype key (k/get-key -/PULL_CHECK tag) exprs)))

        (k/fn? clause)
        (return
         (-/check-clause-function
          record ktype key clause []))
        
        (k/obj? clause)
        (let [ref  (k/get-in schema [table-key key "ref"]) 
              #{ns type} ref 
              table-link (k/get-key -/PULL_LU type)
              ids  (k/obj-keys (or (k/get-in record [table-link key])
                                   {}))
              records (-> (or (k/get-key rows ns)
                              {})
                          (k/obj-pick ids)
                          (k/obj-vals)
                          (k/arr-map (fn:> [e] (k/get-key e "record"))))
              found   (k/arr-filter records
                                    (fn:> [subrecord]
                                      (where-fn rows schema ns clause subrecord)))]
          (return (< 0 (k/len found))))
        
        :else
        (return (-/check-clause-value record ktype key clause))))

(defn.xt pull-where
  "clause for where construct"
  {:added "4.0"}
  [rows schema table-key where record]
  (var clause-fn
       (fn [pair]
         (var [k clause] pair)
         (return (-/pull-where-clause rows schema table-key record -/pull-where k clause))))
  (cond (k/fn? where)
        (return (where record table-key))

        (k/is-empty? where)
        (return true)

        (k/arr? where)
        (return
         (k/arr-some where
                     (fn:> [or-clause]
                       (-/pull-where rows schema table-key or-clause record))))
        
        :else
        (return (-> (k/obj-filter where k/not-nil?)
                    (k/obj-pairs where)
                    (k/arr-every clause-fn)))))

(defn.xt pull-return-clause
  "pull return clause"
  {:added "4.0"}
  [rows schema record where-fn return-fn attr link-ret]
  (var input (scope/get-link-standard link-ret))
  (var [table-name linked] input)
  
  (var #{ident ref} attr)
  (var #{ns type}   ref)
  (var link-key ns)
  (var table-link (k/get-key -/PULL_LU
                             type))
  
  (var ids (k/obj-keys (or (k/get-in record [table-link ident])
                           {})))
  (var entries (-> (or (k/get-key rows link-key)
                       {})
                   (k/obj-pick ids)
                   (k/obj-vals)))
  
  (var return-params (k/last linked))
  (var where-params  (k/arr-filter linked (fn [x]
                                            (return (and (k/obj? x)
                                                         (k/not-empty? x))))))
  (var filter-fn
       (fn [e]
         (when (where-fn rows schema link-key
                         where-params
                         (k/get-key e "record"))
           (var out (return-fn rows schema link-key
                               return-params
                               (k/get-key e "record")))
           (when (k/not-empty? out)
             (return out)))))
  (var records (k/arr-keep entries filter-fn))
  (if (< 0 (k/len records))
    (return [ident records])))

(defn.xt pull-return
  "return construct"
  {:added "4.0"}
  [rows schema table-key returning record]
  (:= returning (or returning ["*/data"]))
  (var data-cols  (scope/get-data-columns schema table-key returning))
  (var link-cols  (scope/get-link-columns schema table-key returning))
  (var output {})
  (k/for:array [pair link-cols]
    (var [attr link-ret] pair)
    (var ret (-/pull-return-clause rows schema record
                                   -/pull-where
                                   -/pull-return
                                   attr link-ret))
    (when ret
      (k/step-set-pair output ret)))
  (k/for:array [col data-cols]
    (var #{ident ref} col)
    (cond (k/nil? ref)
          (do (var out (-> record
                           (k/get-key "data")
                           (k/get-key ident)))
              (when (k/not-nil? out)
                (k/set-key output ident out)))
          
          :else
          (k/set-key output
                     (k/cat ident "_id")
                     (k/first (k/obj-keys (or (-> record
                                                  (k/get-key "ref_links")
                                                  (k/get-key ident))
                                              {}))))))
  (return output))

(defn.xt pull
  "pull data from database"
  {:added "4.0"}
  [rows schema table-key opts]
  (:= opts (or opts {}))
  (var #{id where returning limit order-by order-sort offset single as-map} opts)
  (var pred-fn  (fn [e]
                  (var #{record} e)
                  (return (-/pull-where rows schema table-key where record))))
  (var entry-fn (fn [e]
                  (var #{record} e)
                  (return (-/pull-return rows schema
                                         table-key returning record))))
  (var entries (:? id
                   (-> [(k/get-in rows [table-key id])]
                       (k/arr-filter k/identity))
                   (-> (or (k/get-in rows [table-key]) {})
                       (k/obj-vals))))
  (var out)

  (cond (not (or order-by
                 offset))
        (:= out (ut/keepf-limit entries pred-fn entry-fn limit))
        
        :else
        (:= out (k/arr-map entries entry-fn)))
  
  (when out
    (when order-by
      (:= out (k/sort-by out order-by)))
    (when (== order-sort "desc")
      (:= out (k/arr-reverse out)))
    (when (or order-by
              offset)
      (var sidx (or offset 0))
      (var eidx (+ sidx (or limit (- (k/len entries) sidx))))
      (:= eidx (k/min eidx (k/len entries)))
      (:= out (k/arr-slice out sidx eidx)))
    (when single
      (:= out (k/first out)))
    (when as-map
      (:= out (ut/lu-map out))))
  (return out))

