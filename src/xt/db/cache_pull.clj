(ns xt.db.cache-pull
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-sort-by :as xtsb]
             [xt.db.base-util :as ut]
             [xt.db.base-scope :as scope]]})

(l/script :js
  {:require [[js.core :as j]
             [xt.db.base-util :as ut]
             [xt.db.base-scope :as scope]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt check-in-clause
  "emulates the sql `in` clause"
  {:added "4.0"}
  [x expr]
  (return (xt/x:arr-some (xt/x:first expr)
                         (fn [e] (return (== e x))))))

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
            (return (xt/x:neq x expr)))
   "eq"   xt/x:eq
   "lt"   xt/x:lt
   "lte"  xt/x:lte
   "gt"   xt/x:gt
   "gte"  xt/x:gte
   "like" -/check-like-clause
   "in"   -/check-in-clause
   "between"  (fn:> [x start-expr _and end-expr]
                (and (>= x start-expr)
                     (:? (== _and "and")
                         (<= x end-expr)
                         (<= x _and))))
   "not_like" (fn:> [x expr] (not (-/check-like-clause x expr)))
   "not_in" (fn:> [x expr] (not (-/check-in-clause x expr)))
   "is_null" xt/x:nil? #_(fn [x]
               (xt/x:LOG! x)
               (return (== nil x)))
   "is_not_null"  xt/x:not-nil? #_(fn:> [x] (not= nil x))})

(defn.xt check-clause-value
  "checks the clause within a record"
  {:added "4.0"}
  [record ktype key clause]
  (cond (== ktype "data")
        (return (== clause (xtd/get-in record ["data" key])))

        (== ktype "forward")
        (return (xtd/get-in record ["ref_links" key clause]))

        (== ktype "reverse")
        (return (xtd/get-in record ["rev_links" key clause]))))

(defn.xt check-clause-function
  "checks the clause for a function within a record"
  {:added "4.0"}
  [record ktype key pred exprs]
  (cond (xt/x:nil? pred)
        (return false)

        (== ktype "data")
        (return (pred (xtd/get-in record ["data" key])
                      (xt/x:unpack exprs)))
        
        (== ktype "forward")
        (cond (== pred (. -/PULL_CHECK ["is_null"]))
              (return (pred (xtd/get-in record ["ref_links" key])))
              
              :else
              (return (xt/x:arr-some (xt/x:obj-keys (or (xtd/get-in record ["ref_links" key])
                                                  {}))
                                  (fn:> [v] (pred v (xt/x:unpack exprs))))))
        
        (== ktype "reverse")
        (return (xt/x:arr-some (xt/x:obj-keys (or (xtd/get-in record ["rev_links" key])
                                            {}))
                            (fn:> [v] (pred v (xt/x:unpack exprs)))))))

(defn.xt pull-where-clause
  "pull where clause"
  {:added "4.0"}
  [rows schema table-key record where-fn key clause]
  (var ktype (or (xtd/get-in schema [table-key key "ref" "type"])
                 "data"))
  (cond (xt/x:is-array? clause)
        (do (var [tag] clause)
            (var exprs [(xt/x:unpack clause)])
            (xt/x:arr-pop-first exprs)
            (return
             (-/check-clause-function
              record ktype key (xt/x:get-key -/PULL_CHECK tag) exprs)))

        (xt/x:is-function? clause)
        (return
         (-/check-clause-function
          record ktype key clause []))
        
        (xt/x:is-object? clause)
        (let [ref  (xtd/get-in schema [table-key key "ref"]) 
              #{ns type} ref 
              table-link (xt/x:get-key -/PULL_LU type)
              ids  (xt/x:obj-keys (or (xtd/get-in record [table-link key])
                                   {}))
              records (-> (or (xt/x:get-key rows ns)
                              {})
                          (xtd/obj-pick ids)
                          (xt/x:obj-vals)
                          (xt/x:arr-map (fn:> [e] (xt/x:get-key e "record"))))
              found   (xt/x:arr-filter records
                                    (fn:> [subrecord]
                                      (where-fn rows schema ns clause subrecord)))]
          (return (< 0 (xt/x:len found))))
        
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
  (cond (xt/x:is-function? where)
        (return (where record table-key))

        (xtd/is-empty? where)
        (return true)

        (xt/x:is-array? where)
        (return
         (xt/x:arr-some where
                        (fn [or-clause]
                          (return
                           (-/pull-where rows schema table-key or-clause record)))))
        
        :else
        (return (-> (xtd/obj-filter where xt/x:not-nil?)
                    (xt/x:obj-pairs)
                    (xt/x:arr-every clause-fn)))))

(defn.xt pull-return-clause
  "pull return clause"
  {:added "4.0"}
  [rows schema record where-fn return-fn attr link-ret]
  (var input (scope/get-link-standard link-ret))
  (var [table-name linked] input)
  
  (var #{ident ref} attr)
  (var #{ns type}   ref)
  (var link-key ns)
  (var table-link (xt/x:get-key -/PULL_LU
                             type))
  
  (var ids (xt/x:obj-keys (or (xtd/get-in record [table-link ident])
                           {})))
  (var entries (-> (or (xt/x:get-key rows link-key)
                       {})
                   (xtd/obj-pick ids)
                   (xt/x:obj-vals)))
  
  (var return-params (xt/x:last linked))
  (var where-params  (xt/x:arr-filter linked (fn [x]
                                            (return (and (xt/x:is-object? x)
                                                         (xtd/not-empty? x))))))
  (var filter-fn
       (fn [e]
         (when (where-fn rows schema link-key
                         where-params
                         (xt/x:get-key e "record"))
           (var out (return-fn rows schema link-key
                               return-params
                               (xt/x:get-key e "record")))
           (when (xtd/not-empty? out)
             (return out)))))
  (var records (xt/x:arr-keep entries filter-fn))
  (if (< 0 (xt/x:len records))
    (return [ident records]))
  (return [ident nil]))

(defn.xt pull-return
  "return construct"
  {:added "4.0"}
  [rows schema table-key returning record]
  (:= returning (or returning ["*/data"]))
  (var data-cols  (scope/get-data-columns schema table-key returning))
  (var link-cols  (scope/get-link-columns schema table-key returning))
  (var output {})
  (xt/for:array [pair link-cols]
    (var [attr link-ret] pair)
    (var ret (-/pull-return-clause rows schema record
                                   -/pull-where
                                   -/pull-return
                                   attr link-ret))
    (xt/x:set-key output
                  (xt/x:first ret)
                  (xt/x:second ret)))
  (xt/for:array [col data-cols]
    (var #{ident ref} col)
    (cond (xt/x:nil? ref)
          (do (var out (xt/x:get-path record ["data" ident]))
              (xt/x:set-key output ident out))
          
          :else
          (xt/x:set-key output
                     (xt/x:cat ident "_id")
                     (xt/x:first (xt/x:obj-keys
                                  (xt/x:get-path record ["ref_links" ident]))))))
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
                   (-> [(xtd/get-in rows [table-key id])]
                       (xt/x:arr-filter (fn [x] (return x))))
                   (-> (or (xtd/get-in rows [table-key]) {})
                       (xt/x:obj-vals))))
  (var out)

  (cond (not (or order-by
                 offset))
        (:= out (ut/keepf-limit entries pred-fn entry-fn limit))
        
        :else
        (:= out (xt/x:arr-map entries entry-fn)))
  
  (when out
    (when order-by
      (:= out (xtsb/sort-by out order-by)))
    (when (== order-sort "desc")
      (:= out (xt/x:arr-reverse out)))
    (when (or order-by
              offset)
      (var sidx (or offset 0))
      (var eidx (+ sidx (or limit (- (xt/x:len entries) sidx))))
      (:= eidx (xt/x:m-min eidx (xt/x:len entries)))
      (:= out (xt/x:arr-slice out sidx eidx)))
    (when single
      (:= out (xt/x:first out)))
    (when as-map
      (:= out (ut/lu-map out))))
  (return out))
