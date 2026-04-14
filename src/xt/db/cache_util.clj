(ns xt.db.cache-util
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt has-entry
  "checks if entry exists"
  {:added "4.0"}
  [rows table-key id]
  (return (not= nil (xtd/get-in rows [table-key id]))))

(defn.xt get-entry
  "gets entry by id"
  {:added "4.0"}
  [rows table-key id]
  (return (xtd/get-in rows [table-key id])))

(defn.xt swap-if-entry
  "modifies entry if exists"
  {:added "4.0"}
  [rows table-key id f]
  (let [entry (xtd/get-in rows [table-key id])]
    (if entry
      (let [#{record} entry
            _ (f record)
            new-entry {:t (xt/x:now-ms)
                       :record record}]
        (xtd/set-in rows [table-key id] new-entry)
        (return new-entry)))
    (return entry)))

(defn.xt merge-single
  "merges a single entry"
  {:added "4.0"}
  [rows table-key id new-record new-fn]
  (let [entry    (or (-/get-entry rows table-key id)
                     {:record {:id id
                               :data {}
                               :ref-links {}
                               :rev-links {}}})
        #{data rev-links ref-links} new-record
        #{record} entry
        _ (xt/x:obj-assign (xt/x:get-key record "data") data)
        _ (xtd/swap-key record "ref_links" xtd/obj-assign-with [ref-links xt/x:obj-assign])
        _ (xtd/swap-key record "rev_links" xtd/obj-assign-with [rev-links xt/x:obj-assign])
        new-entry  (new-fn {:t (xt/x:now-ms)
                            :record record})]
    (xtd/set-in rows [table-key id] new-entry)
    (return new-entry)))

(defn.xt merge-bulk
  "merges flattened data into the database"
  {:added "4.0"}
  [rows fdata new-fn]
  (var out {})
  (xt/for:object [[table-key m] fdata]
    (xt/for:object [[id new-record] m]
      (xtd/set-in out [table-key id]
                (-/merge-single rows table-key id new-record
                                (or new-fn (fn [x] (return x)))))))
  (return out))

(defn.xt get-ids
  "get ids for table-key"
  {:added "4.0"}
  [rows table-key]
  (return (xt/x:obj-keys (or (xt/x:get-key rows table-key)
                          {}))))

(defn.xt all-records
  "returns all records"
  {:added "4.0"}
  [rows table-key]
  (if (xt/x:nil? table-key)
    (return (-> (xtd/arr-juxt (xt/x:obj-keys rows)
                            (fn [x] (return x))
                            (fn [k] (return (-/all-records rows k))))
                (xtd/obj-filter (fn [e]
                                (return (or (xt/x:nil? e)
                                            (< 0 (xt/x:len (xt/x:obj-keys e)))))))))
    (return (xtd/obj-map (xt/x:get-key rows table-key)
                       (fn [e]
                         (return (xt/x:get-key e "record")))))))

(defn.xt get-changed-single
  "gets changed record"
  {:added "4.0"}
  [rows table-key id record]
  (var curr (-/get-entry rows table-key id))
  (cond (xt/x:nil? curr)
        (return record)

        :else
        (return (xtd/tree-diff-nested (xt/x:get-key curr "record")
                                      record))))

(defn.xt has-changed-single
  "checks if record has changed"
  {:added "4.0"}
  [rows table-key id record]
  (var changed (-/get-changed-single rows table-key id record))
  (return (< 0 (xt/x:len (xt/x:obj-keys changed)))))

(defn.xt get-link-attrs
  "find link attributes"
  {:added "4.0"}
  [schema table-key field]
  (let [attr (xtd/get-in schema [table-key field "ref"])
        _    (if (not attr)
               (xt/x:err (xt/x:cat "Not a valid link type: " (xt/x:json-encode [table-key field]))))
        #{ns type rval} attr
        [table-link
         inverse-link] (xt/x:get-key {:reverse ["rev_links" "ref_links"]
                                   :forward ["ref_links" "rev_links"]}
                                  type)]
    (return {:table-key     table-key
             :table-link    table-link
             :table-field   field
             :inverse-key   ns
             :inverse-link  inverse-link
             :inverse-field rval})))

;;
;; REMOVE ENTRY
;;

(defn.xt remove-single-link-entry
  "removes single link for entry"
  {:added "4.0"}
  [rows table-key id
   table-link table-field link-id link-cb]
  (var remove-fn
       (fn [record]
         (let [link (xt/x:get-key record table-link)
               lrec (xt/x:get-key link table-field)]
           (when (and lrec (xt/x:has-key? lrec link-id))
             (xt/x:del-key lrec link-id)
             (if (== 0 (xt/x:len (xt/x:obj-keys lrec)))
               (xt/x:del-key link table-field))
             (if link-cb (link-cb link-id))))))
  (return (-/swap-if-entry rows table-key id remove-fn)))

(defn.xt remove-single-link
  "removes single link"
  {:added "4.0"}
  [rows schema table-key id field link-id]
  (let [attrs  (-/get-link-attrs schema table-key field)
        #{table-link
          table-field
          inverse-key
          inverse-link
          inverse-field} attrs 
        l-arr [false false]
        t-has-fn (fn [_] (:= (xt/x:first l-arr) true)) 
        t-entry (-/remove-single-link-entry
                 rows table-key id table-link table-field link-id
                 t-has-fn)
        i-has-fn (fn [_] (:= (xt/x:second l-arr) true)) 
        i-entry (-/remove-single-link-entry
                 rows inverse-key link-id inverse-link inverse-field id
                 i-has-fn)]
    (return l-arr)))

(defn.xt remove-single
  "removes a single entry"
  {:added "4.0"}
  [rows schema table-key id]
  (var entry (-/get-entry rows table-key id))
  (when entry
    (var rec (xt/x:get-key entry "record"))
    (var #{ref-links rev-links} rec)
    (var links (xt/x:arr-append (xt/x:obj-pairs ref-links)
                             (xt/x:obj-pairs rev-links)))
    (xt/for:array [pair links]
      (var [field m] pair)
      (var attrs (-/get-link-attrs schema table-key field))
      (var #{inverse-key
             inverse-link
             inverse-field} attrs)
      (xt/for:array [link-id (xt/x:obj-keys m)]
        (-/remove-single-link-entry
         rows inverse-key link-id inverse-link inverse-field id nil)))
    (xt/x:del-key (xt/x:get-key rows table-key) id)
    (return [entry])))

(defn.xt remove-bulk
  "removes bulk data"
  {:added "4.0"}
  ([rows schema table-key ids]
   (return (-> (xt/x:arr-keep ids
                           (fn [id]
                             (return (-/remove-single rows schema table-key id))))
               (xtd/arr-mapcat (fn [x] (return x)))))))


;;
;; ADD ENTRY
;;

(defn.xt add-single-link-entry
  "adds single link entry for one side"
  {:added "4.0"}
  [rows table-key id
   table-link table-field link-id link-cb inverse-key inverse-field]
  (var add-fn
       (fn [record]
         (var link (xt/x:get-key record table-link))
         (var lrec (xt/x:get-key link table-field))
         (cond (xt/x:nil? lrec)
               (do (:= lrec {})
                   (xt/x:set-key link table-field lrec)
                   (xt/x:set-key lrec link-id true))
               
               (== table-link "rev_links")
               (xt/x:set-key lrec link-id true)

               :else
               (do (xt/for:object [[prev-id _] lrec]
                     (-/remove-single-link-entry
                      rows
                      inverse-key
                      prev-id
                      "rev_links"
                      inverse-field
                      id
                      nil))
                   (xt/x:set-key link table-field {link-id true})))
         
         (when link-cb (link-cb link-id))))
  (return (-/swap-if-entry rows table-key id add-fn)))

(defn.xt add-single-link
  "adds single link"
  {:added "4.0"}
  [rows schema table-key id field link-id]
  (var attrs  (-/get-link-attrs schema table-key field))
  (var #{table-link
         table-field
         inverse-key
         inverse-link
         inverse-field} attrs)
  (var l-arr [false false])
  (var t-has-fn (fn [_]
                  (:= (xt/x:first l-arr) true)))
  (var t-entry-fn
       (fn:> (-/add-single-link-entry
              rows table-key id table-link table-field link-id
              t-has-fn inverse-key inverse-field)))
  (var i-has-fn
       (fn [_]
         (:= (xt/x:second l-arr) true))) 
  (var i-entry-fn
       (fn:> (-/add-single-link-entry
              rows inverse-key link-id inverse-link inverse-field id
              i-has-fn table-key field)))
  (cond (== table-link "ref_links")
        (do (t-entry-fn)
            (i-entry-fn))

        (== table-link "rev_links")
        (do (i-entry-fn)
            (t-entry-fn)))
  (return l-arr))
  
(defn.xt add-bulk-links
  "adding bulk links from external data (to be doubly sure)"
  {:added "4.0"}
  [rows schema flat]
  (var out [])
  (xt/for:object [[table-key bulk] flat]
    (xt/for:object [[row-id record] bulk]
      (var #{ref-links
             rev-links} record)
      (xt/for:object [[field links] ref-links]
        (xt/for:object [[link-id _] links]
          (-/add-single-link rows schema table-key row-id field link-id)
          (xt/x:arr-push out {:table table-key
                           :id row-id
                           :field field
                           :link-id link-id})))
      (xt/for:object [[field links] rev-links]
        (xt/for:object [[link-id _] links]
          (-/add-single-link rows schema table-key row-id field link-id)
          (xt/x:arr-push out {:table table-key
                           :id row-id
                           :field field
                           :link-id link-id})))))
  (return out))

