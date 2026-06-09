(ns xt.db.system.memory-graph
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-sort-by :as xtsb]
             [xt.db.text.base-graph :as base-graph]
             [xt.db.text.base-tree :as base-tree]]})

(defn.xt check-in-clause
  "emulates the sql `in` clause"
  {:added "4.0"}
  [x expr]
  (return (xt/x:arr-some (xt/x:first expr)
                         (fn [e] (return (== e x))))))

(defn.xt like-char-at
  "gets a single character from a string"
  {:added "4.1"}
  [s i]
  (return (xt/x:str-substring s
                              (xt/x:offset i)
                              (+ i 1))))

(defn.xt check-like-clause
  "emulates the sql `like` clause"
  {:added "4.0"}
  [x expr]
  (when (or (not (xt/x:is-string? x))
            (not (xt/x:is-string? expr)))
    (return false))
  (var slen (xt/x:str-len x))
  (var plen (xt/x:str-len expr))
  (var sidx 0)
  (var pidx 0)
  (var star-pidx -1)
  (var star-sidx -1)
  (while (< sidx slen)
    (var sch (-/like-char-at x sidx))
    (var pch (:? (< pidx plen)
                 (-/like-char-at expr pidx)
                 nil))
    (cond (and (== "\\" pch)
               (< (+ pidx 1) plen)
               (== sch (-/like-char-at expr (+ pidx 1))))
          (do (:= sidx (+ sidx 1))
              (:= pidx (+ pidx 2)))

          (and (== "\\" pch)
               (== sch "\\")
               (== (+ pidx 1) plen))
          (do (:= sidx (+ sidx 1))
              (:= pidx (+ pidx 1)))

          (== "%" pch)
          (do (:= star-pidx pidx)
              (:= star-sidx sidx)
              (:= pidx (+ pidx 1)))

          (or (== "_" pch)
              (== sch pch))
          (do (:= sidx (+ sidx 1))
              (:= pidx (+ pidx 1)))

          (< star-pidx 0)
          (return false)

          :else
          (do (:= star-sidx (+ star-sidx 1))
              (:= sidx star-sidx)
              (:= pidx (+ star-pidx 1)))))
  (while (< pidx plen)
    (if (== "%" (-/like-char-at expr pidx))
      (:= pidx (+ pidx 1))
      (return false)))
  (return true))

(def.xt LINK_LOOKUP
  {"forward" "ref_links"
   "reverse" "rev_links"})

(defn.xt check-ilike-clause
  "emulates a case-insensitive sql `like` clause"
  {:added "4.1"}
  [x expr]
  (when (or (not (xt/x:is-string? x))
            (not (xt/x:is-string? expr)))
    (return false))
  (return
   (-/check-like-clause
    (xt/x:str-to-lower x)
    (xt/x:str-to-lower expr))))

(def.xt PULL_CHECK
  {"neq"  (fn [x expr]
            (return (xt/x:neq x expr)))
   "eq"   xt/x:eq
   "lt"   xt/x:lt
   "lte"  xt/x:lte
   "gt"   xt/x:gt
   "gte"  xt/x:gte
   "like" -/check-like-clause
   "ilike" -/check-ilike-clause
   "in"   -/check-in-clause
   "is"   (fn [x expr]
            (return (xt/x:eq x expr)))
   "between"  (fn [x start-expr _and end-expr]
                (return
                 (and (>= x start-expr)
                      (:? (== _and "and")
                          (<= x end-expr)
                          (<= x _and)))))
   "not_like" (fn:> [x expr] (not (-/check-like-clause x expr)))
   "not_in" (fn:> [x expr] (not (-/check-in-clause x expr)))
   "is_null" xt/x:nil?
   "is_not_null"  xt/x:not-nil?})

(defn.xt custom-params
  "extracts query controls from tree custom nodes"
  {:added "4.1"}
  [custom]
  (var out {"count" false
            "order_by" nil
            "order_sort" nil
            "limit" nil
            "offset" nil})
  (xt/for:array [entry (or custom [])]
    (cond (== (xt/x:get-key entry "::") "sql/count")
          (xt/x:set-key out "count" true)

          (== (xt/x:get-key entry "::") "sql/keyword")
          (do (var name (xt/x:get-key entry "name"))
              (cond (== name "ORDER BY")
                    (do (var tuple (xt/x:first (or (xt/x:get-key entry "args") [])))
                        (xt/x:set-key out
                                      "order_by"
                                      (xt/x:arr-map (or (xt/x:get-key tuple "args") [])
                                                    (fn [arg]
                                                      (return (xt/x:get-key arg "name"))))))

                    (or (== name "ASC")
                        (== name "DESC"))
                    (xt/x:set-key out "order_sort" (xt/x:str-to-lower name))

                    (== name "LIMIT")
                    (xt/x:set-key out
                                  "limit"
                                  (xt/x:get-key (xt/x:first (or (xt/x:get-key entry "args") []))
                                                "name"))

                    (== name "OFFSET")
                    (xt/x:set-key out
                                  "offset"
                                  (xt/x:get-key (xt/x:first (or (xt/x:get-key entry "args") []))
                                                "name"))))))
  (return out))

(defn.xt check-clause-value
  "checks a scalar clause against one record"
  {:added "4.1"}
  [record key clause]
  (cond (xt/x:str-ends-with key "_id")
        (do (var base-key (xt/x:str-substring key 0 (- (xt/x:str-len key) 3)))
            (return (== clause
                        (xt/x:first (xt/x:obj-keys
                                     (or (xtd/get-in record ["ref_links" base-key])
                                         {}))))))

        :else
        (return (== clause (xtd/get-in record ["data" key])))))

(defn.xt check-clause-function
  "checks a function clause against one record"
  {:added "4.1"}
  [record link-type key pred exprs]
  (cond (xt/x:nil? pred)
        (return false)

        (xt/x:nil? link-type)
        (return (pred (xtd/get-in record ["data" key])
                      (xt/x:unpack exprs)))

        (== link-type "forward")
        (cond (== pred (xt/x:get-key -/PULL_CHECK "is_null"))
              (return (pred (xtd/get-in record ["ref_links" key])))

              :else
              (return (xt/x:arr-some (xt/x:obj-keys (or (xtd/get-in record ["ref_links" key])
                                                        {}))
                                     (fn:> [v] (pred v (xt/x:unpack exprs))))))

        (== link-type "reverse")
        (return (xt/x:arr-some (xt/x:obj-keys (or (xtd/get-in record ["rev_links" key])
                                                  {}))
                               (fn:> [v] (pred v (xt/x:unpack exprs)))))))

(defn.xt where-clause
  "checks one where clause branch"
  {:added "4.1"}
  [rows schema table-name record where-fn key clause]
  (var link-type (xtd/get-in schema [table-name key "ref" "type"]))
  (cond (xt/x:is-array? clause)
        (do (var tag (xt/x:first clause))
            (var exprs [(xt/x:unpack clause)])
            (xt/x:arr-pop-first exprs)
            (return (-/check-clause-function
                     record
                     link-type
                     key
                     (xt/x:get-key -/PULL_CHECK tag)
                     exprs)))

        (xt/x:is-function? clause)
        (return (-/check-clause-function
                 record
                 link-type
                 key
                 clause
                 []))

        (xt/x:is-object? clause)
        (let [ref (xtd/get-in schema [table-name key "ref"])
              link-table (xt/x:get-key ref "ns")
              link-map-key (xt/x:get-key -/LINK_LOOKUP (xt/x:get-key ref "type"))
              ids (xt/x:obj-keys (or (xtd/get-in record [link-map-key key])
                                     {}))
              entries (-> (or (xt/x:get-key rows link-table)
                              {})
                          (xtd/obj-pick ids)
                          (xt/x:obj-vals))
              found (xt/x:arr-filter entries
                                     (fn [entry]
                                       (return (where-fn rows
                                                         schema
                                                         link-table
                                                         clause
                                                         (xt/x:get-key entry "record")))))]
          (return (< 0 (xt/x:len found))))

        :else
        (return (-/check-clause-value record key clause))))

(defn.xt where
  "checks a where predicate over one record"
  {:added "4.1"}
  [rows schema table-name where record]
  (var clause-fn
       (fn [pair]
         (var [k clause] pair)
         (return (-/where-clause rows schema table-name record -/where k clause))))
  (cond (xt/x:is-function? where)
        (return (where record table-name))

        (xtd/is-empty? where)
        (return true)

        (xt/x:is-array? where)
        (return (xt/x:arr-some where
                               (fn [or-clause]
                                 (return (-/where rows schema table-name or-clause record)))))

        :else
        (return (-> (xtd/obj-filter where xt/x:not-nil?)
                    (xt/x:obj-pairs)
                    (xt/x:arr-every clause-fn)))))

(defn.xt data-field
  "projects one scalar field from a record"
  {:added "4.1"}
  [record key]
  (cond (xt/x:str-ends-with key "_id")
        (do (var base-key (xt/x:str-substring key 0 (- (xt/x:str-len key) 3)))
            (return (xt/x:first (xt/x:obj-keys
                                 (or (xtd/get-in record ["ref_links" base-key])
                                     {})))))

        :else
        (return (xtd/get-in record ["data" key]))))

(defn.xt project-record
  "projects one record using tree data and links"
  {:added "4.1"}
  [rows schema tree record opts pull-entries-fn]
  (var params (xtd/second tree))
  (var data (or (xt/x:get-key params "data") []))
  (var links (or (xt/x:get-key params "links") []))
  (var out {})
  (xt/for:array [key data]
    (xt/x:set-key out key (-/data-field record key)))
  (xt/for:array [link links]
    (var link-name (xt/x:first link))
    (var link-type (xtd/second link))
    (var child-tree (xtd/nth link 2))
    (var link-map-key (xt/x:get-key -/LINK_LOOKUP link-type))
    (var ids (xt/x:obj-keys (or (xtd/get-in record [link-map-key link-name])
                                {})))
    (var child-table (xt/x:first child-tree))
    (var child-entries (-> (or (xt/x:get-key rows child-table)
                               {})
                           (xtd/obj-pick ids)
                           (xt/x:obj-vals)))
    (var child-output (pull-entries-fn rows schema child-tree child-entries opts))
    (xt/x:set-key out link-name (:? (and (xt/x:is-array? child-output)
                                         (< 0 (xt/x:len child-output)))
                                    child-output
                                    nil)))
  (return out))

(defn.xt apply-custom
  "applies control nodes to projected output"
  {:added "4.1"}
  [out custom]
  (when (xt/x:not-nil? (xt/x:get-key custom "order_by"))
    (:= out (xtsb/sort-by out
                          (xt/x:get-key custom "order_by"))))
  (when (== (xt/x:get-key custom "order_sort") "desc")
    (:= out (xt/x:arr-reverse out)))
  (when (or (xt/x:not-nil? (xt/x:get-key custom "offset"))
            (xt/x:not-nil? (xt/x:get-key custom "limit")))
    (var sidx (or (xt/x:get-key custom "offset") 0))
    (var total (xt/x:len out))
    (var eidx (+ sidx (or (xt/x:get-key custom "limit")
                          (- total sidx))))
    (:= eidx (xt/x:m-min eidx total))
    (:= out (xt/x:arr-slice out sidx eidx)))
  (return out))

(defn.xt pull-entries
  "pulles results for a given tree and entry list"
  {:added "4.1"}
  [rows schema tree entries opts]
  (var table-name (xt/x:first tree))
  (var params (xtd/second tree))
  (var where-clause (xt/x:get-key params "where"))
  (var custom (-/custom-params (xt/x:get-key params "custom")))
  (var matched (xt/x:arr-filter (or entries [])
                                (fn [entry]
                                  (return (-/where rows
                                                   schema
                                                   table-name
                                                   where-clause
                                                   (xt/x:get-key entry "record"))))))
  (when (xt/x:get-key custom "count")
    (return (xt/x:len matched)))
  (var out (xt/x:arr-map matched
                         (fn [entry]
                           (return (-/project-record rows
                                                     schema
                                                     tree
                                                     (xt/x:get-key entry "record")
                                                     opts
                                                     -/pull-entries)))))
  (return (-/apply-custom out custom)))

;;
;;

(defn.xt pull
  "pulles data from rows using tree ir"
  {:added "4.1"}
  [rows schema tree opts]
  (:= tree (base-graph/select-tree schema tree opts))
  (var table-name (xt/x:first tree))
  (var entries (xt/x:obj-vals (or (xt/x:get-key rows table-name)
                                  {})))
  (return (-/pull-entries rows schema tree entries opts)))


;;
;; memory sql views
;;

(defn.xt view-select
  "plans and pulles a select query"
  {:added "4.1"}
  [rows schema entry args opts]
  (var tree (base-tree/plan-select schema entry args opts))
  (return (-/pull rows schema tree opts)))

(defn.xt view-count
  "plans and pulles a count query"
  {:added "4.1"}
  [rows schema entry args opts]
  (var tree (base-tree/plan-count schema entry args opts))
  (return (-/pull rows schema tree opts)))

(defn.xt view-return
  "plans and pulles a return query"
  {:added "4.1"}
  [rows schema entry id args opts]
  (var tree (base-tree/plan-return schema entry id args opts))
  (return (-/pull rows schema tree opts)))

(defn.xt view-return-bulk
  "plans and pulles a bulk return query"
  {:added "4.1"}
  [rows schema entry ids args opts]
  (var tree (base-tree/plan-return-bulk schema entry ids args opts))
  (return (-/pull rows schema tree opts)))

(defn.xt view-combined
  "plans and pulles a combined query"
  {:added "4.1"}
  [rows schema sel-entry sel-args ret-entry ret-args ret-omit opts]
  (var tree (base-tree/plan-combined schema sel-entry sel-args ret-entry ret-args ret-omit opts))
  (return (-/pull rows schema tree opts)))
