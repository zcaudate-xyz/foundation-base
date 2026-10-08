(ns xt.db.node.table-util
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]]})


;;
;; TABLE FUNCTIONS
;;

(defn.xt table-impl-entry
  "gets the required entry"
  {:added "0.1"}
  [impl request-type]
  (return
   (xtd/arr-foldl ["base" request-type]
                  (fn [acc key]
                    (return
                     (xtd/obj-assign acc (. impl [key]))))
                  {})))

(defn.xt table-check-entry
  "gets the required entry"
  {:added "0.1"}
  [entry sel-input]
  (when (or (k/nil? entry)
            (k/nil? (. entry list)))
    (return false))

  (var check-fn (or (. entry list check)
                    k/T))
  (return (check-fn sel-input)))

(defn.xt table-ids
  "gets the required table-ids"
  {:added "0.1"}
  [impl request-type sel-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
  
  (var #{[type
          (:= wrap k/identity)]} (. entry list))
  #_#_#_(var args-fn (or (. entry list input)
                    k/identity))
  (var args (args-fn sel-input context))
  (cond (== type "custom")
        (do (var f  (wrap (. entry list fn)))
            (return (f (cl/get-cell context)
                       (k/unpack args))))
        
        (== request-type "remote")
        (do (var f  (-> -/view-query-nosync
                        ut/wrap-remote
                        ut/wrap-ids))
            (return (f (cl/get-cell context)
                       (. impl table)
                       args)))
        
        (== request-type "local")
        (cond (== type "raw")
              (do (var f  (ut/wrap-ids link-local/raw-pull))
                  (return (f (cl/get-cell context)
                             [(. impl table)
                              args
                              ["id"]])))
              
              :else
              (do (var f  (ut/wrap-ids link-local/view-query))
                  (return (f (cl/get-cell context)
                             (. impl table)
                             args))))))

(defn.xt table-ids-updated
  "gets updated info"
  {:added "0.1"}
  [impl request-type sel-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
    
  (var #{[type
          (:= update-key "time_updated")]} (. entry list))
  (var args-fn (or (. entry list input)
                    k/identity))
  (var args (args-fn sel-input context))
  (cond (== request-type "remote")
        (do (var f  (-> -/view-query-nosync
                        ut/wrap-remote))
            (return (f (cl/get-cell context)
                       (. impl table)
                       (j/assign {:return-query ["id" update-key]}
                                 args))))
        
        (== request-type "local")
        (cond (== type "raw")
              (do (var f  link-local/raw-pull)
                  (return (f (cl/get-cell context)
                             [(. impl table)
                              args
                              ["id" update-key]])))
              
              :else
              (do (var f  link-local/view-query)
                  (return (f (cl/get-cell context)
                             (. impl table)
                             (j/assign {:return-query ["id" update-key]}
                                       args)))))))

(defn.xt table-ids-data
  "gets the table ids"
  {:added "0.1"}
  [impl request-type request-level ids ret-input context]
  (:= ids (k/arr-filter ids k/identity))
  (var entry (-/table-impl-entry impl request-type))
  (when (or (k/nil? entry)
            (k/nil? (. entry [request-level])))
    (return))
  
  (var #{[type]} (. entry [request-level]))
  (var args-fn (or (. entry [request-level] input)
                    k/identity))
  (var args  (args-fn ret-input context))
  (cond (== request-type "remote")
        (do (var f  (-> -/view-query
                        ut/wrap-remote))
            (return (f (cl/get-cell context)
                       (. impl table)
                       (j/assign {:return-bulk ids}
                                 args))))
        
        (== request-type "local")
        (cond (== type "raw")
              (do (var f  link-local/raw-pull)
                  (return (f (cl/get-cell context)
                             [(. impl table)
                              {:id ["in" [ids]]}
                              args])))
              
              :else
              (do (var f  link-local/view-query)
                  (return (f (cl/get-cell context)
                             (. impl table)
                             (j/assign {:return-bulk ids}
                                       args)))))))

(defn.xt table-pull
  "pulls data from impl"
  {:added "0.1"}
  [impl request-type request-level sel-input ret-input context]
  (var entry (-/table-impl-entry impl request-type))
  (when (not (-/table-check-entry entry sel-input))
    (return))
  (cond (and (== request-type "remote")
             (== "view" (. entry [request-level] type))
             (== "view" (. entry list type)))
        (do (var f (ut/wrap-remote -/view-query))
            (var sel-fn (or (. entry list input)
                            k/identity))
            (var ret-fn (or (. entry [request-level] input)
                            k/identity))
            (var qm (j/assign (sel-fn sel-input context)
                              (ret-fn ret-input context)))
            (return (f (cl/get-cell context)
                       (. impl table)
                       qm)))
        :else
        (return (. (-/table-ids impl request-type sel-input context)
                   (then (fn [ids]
                           (return
                            (-/table-ids-data impl request-type request-level
                                              (k/arr-filter ids k/identity)
                                              ret-input
                                              context))))))))

(defn.xt sync-unknown-ids
  "gets unknown-ids for syncing"
  {:added "0.1"}
  [impl sel-input context]
  (return
   (. (j/onAll [(-/table-ids impl "local" sel-input context)
                (-/table-ids impl "remote" sel-input context)])
      (then (fn [[(:= local-ids [])
                  (:= remote-ids [])]]
              (return (k/arr-append
                       (k/arr-difference local-ids remote-ids)
                       (k/arr-difference remote-ids local-ids))))))))

(defn.xt sync-outdated-ids
  "gets outdated-ids for syncing"
  {:added "0.1"}
  [impl sel-input context]
  (var entry (-/table-impl-entry impl "local"))
  (when (not (-/table-check-entry entry sel-input))
    (return))

  (var #{[(:= update-key "time_updated")]}
       (. entry list))
  (return
   (. (j/onAll [(-/table-ids-updated impl "local" sel-input context)
                (-/table-ids-updated impl "remote" sel-input context)]) 
      (then (fn [[(:= local-data [])
                  (:= remote-data [])]]
              (return (k/obj-keys
                       (k/obj-diff (k/arr-juxt (or local-data [])
                                               k/id-fn
                                               (k/key-fn update-key))
                                   (k/arr-juxt (or remote-data [])
                                               k/id-fn
                                               (k/key-fn update-key))))))))))

(defn.xt sync-unknown
  "syncs unknown table records"
  {:added "0.1"}
  [impl request-level sel-input ret-input context]
  (return
   (. (-/sync-unknown-ids impl sel-input context)
      (then (fn [ids]
              (return
               (:? (k/not-empty? ids)
                   (-/table-ids-data impl "remote" request-level ids ret-input context)
                   [])))))))

(defn.xt sync-outdated
  "syncs outdated table records"
  {:added "0.1"}
  [impl request-level sel-input ret-input context]
  (return
   (. (-/sync-outdated-ids impl sel-input context)
      (then (fn:> [ids]
              (:? (k/not-empty? ids)
                  (-/table-ids-data impl "remote" request-level ids ret-input context)
                  []))))))

(defn.xt action-fn
  [action context]
  (var #{[(:= wrap k/identity)
          (:= output k/identity)]} action)
  (var f (wrap (. action fn)))
  (var input-fn (or (. action input)
                    k/identity))
  (return (fn [...args]
            (var input (input-fn [...args] context))
            (return (. (f (k/unpack input))
                       (then output))))))

(defn.xt make-actions
  [impl context]
  (return
   (k/obj-map (. impl actions)
              (fn:> [action] (-/action-fn action context)))))

(comment
  (code.manage/scaffold)
  )
