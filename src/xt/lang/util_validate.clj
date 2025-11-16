(ns xt.lang.util-validate
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt validate-step
  "validates a single step"
  {:added "4.0"}
  [form field guards index result hook-fn complete-fn]
  (:= guards (or guards []))
  (cond (< index (k/len guards))
        (do (var guard (k/get-idx guards (x:offset index)))
            (var [id m] guard)
            (var #{check message} m)
            (var error-fn
                 (fn []
                   (k/obj-assign (k/get-path result ["fields" field])
                                 {:status "errored"
                                  :id id
                                  :data (k/get-key form field)
                                  :message message})
                   (when hook-fn (hook-fn id false))
                   (when complete-fn (complete-fn false result))))
            (return (k/for:async [[ok err] (check (k/get-key form field) form)]
                      {:success  (cond (== ok false)
                                       (return (error-fn))
                                       
                                       :else
                                       (do (when hook-fn (hook-fn id true))
                                           (return (-/validate-step form field guards
                                                                    (+ index 1)
                                                                    result
                                                                    hook-fn
                                                                    complete-fn))))
                       :error    (error-fn)})))
        
        :else
        (do (var entry (k/get-path result ["fields" field]))
            (when entry
              (k/del-key entry "id")
              (k/del-key entry "data")
              (k/del-key entry "message")
              (k/obj-assign entry {:status "ok"}))
            (when complete-fn
              (complete-fn true result))
            (return result))))

(defn.xt validate-field
  "validates a single field"
  {:added "4.0"}
  [form field validators result hook-fn complete-fn]
  (var guards (k/get-key validators field))
  (var index 0)
  (return (-/validate-step form field guards index result
                           hook-fn
                           (fn [passed status]
                             (when (not passed)
                               (k/set-key result "status" "errored"))
                             (when complete-fn
                               (complete-fn passed status))))))

(defn.xt validate-all
  "validates all data"
  {:added "4.0"}
  [form validators result hook-fn complete-fn]
  (var fields (k/obj-keys validators))
  (var complete-check-fn
       (fn [success]
         (when (not success) (k/set-key result "status" "errored"))
         (when (== "errored" (k/get-key result "status"))
           (when complete-fn (complete-fn false result))
           (return))
         (when (k/arr-every (k/obj-vals (k/get-key result "fields"))
                            (fn [e]
                              (return (== (k/get-key e "status") "ok"))))
           (k/set-key result "status" "ok")
           (when complete-fn (complete-fn true result))
           (return))))
  (return (k/arr-map fields
                     (fn [field]
                       (return (-/validate-field
                                form field validators result
                                hook-fn complete-check-fn))))))

(defn.xt create-result
  "creates a result datastructure"
  {:added "4.0"}
  [validators]
  (var result  {"::" "validation.result"
                :status   "pending"
                :fields   (k/obj-map validators
                                     (fn [_]
                                       (return {:status "pending"})))})
  (return result))

