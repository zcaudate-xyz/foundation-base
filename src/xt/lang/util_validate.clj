(ns xt.lang.util-validate
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defspec.xt ValidationFieldResult
  [:xt/record
   ["status" :xt/str]])

(defspec.xt ValidationResult
  [:xt/record
   ["::" :xt/str]
   ["status" :xt/str]
   ["fields" [:xt/dict :xt/str ValidationFieldResult]]])

(defspec.xt create-result
  [:fn [[:xt/dict :xt/str :xt/any]] ValidationResult])

(defspec.xt validate-field
  [:fn [:xt/any :xt/str [:xt/dict :xt/str :xt/any] ValidationResult :xt/any :xt/any] :xt/any])

(defspec.xt validate-all
  [:fn [:xt/any [:xt/dict :xt/str :xt/any] ValidationResult :xt/any :xt/any] :xt/any])

(defn.xt validate-step
  "validates a single step"
  {:added "4.0"}
  [form field guards index result hook-fn complete-fn]
  (:= guards (or guards []))
  (cond (< index (xt/x:len guards))
        (do (var guard (xt/x:get-idx guards (x:offset index)))
            (var [id m] guard)
            (var #{check message} m)
            (var error-fn
                 (fn []
                   (xt/x:obj-assign (xt/x:get-path result ["fields" field])
                                    {:status "errored"
                                     :id id
                                     :data (xt/x:get-key form field)
                                     :message message})
                   (when hook-fn (hook-fn id false))
                   (when complete-fn (complete-fn false result))))
            (return (xt/for:async [[ok err] (check (xt/x:get-key form field) form)]
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
        (do (var entry (xt/x:get-path result ["fields" field]))
            (when entry
              (xt/x:del-key entry "id")
              (xt/x:del-key entry "data")
              (xt/x:del-key entry "message")
              (xt/x:obj-assign entry {:status "ok"}))
            (when complete-fn
              (complete-fn true result))
            (return result))))

(defn.xt validate-field
  "validates a single field"
  {:added "4.0"}
  [form field validators result hook-fn complete-fn]
  (var guards (xt/x:get-key validators field))
  (var index 0)
  (return (-/validate-step form field guards index result
                           hook-fn
                           (fn [passed status]
                             (when (not passed)
                               (xt/x:set-key result "status" "errored"))
                             (when complete-fn
                               (complete-fn passed status))))))

(defn.xt validate-all
  "validates all data"
  {:added "4.0"}
  [form validators result hook-fn complete-fn]
  (var fields (xt/x:obj-keys validators))
  (var complete-check-fn
       (fn [success]
         (when (not success) (xt/x:set-key result "status" "errored"))
         (when (== "errored" (xt/x:get-key result "status"))
           (when complete-fn (complete-fn false result))
           (return))
         (when (xt/x:arr-every (xt/x:obj-vals (xt/x:get-key result "fields"))
                            (fn [e]
                              (return (== (xt/x:get-key e "status") "ok"))))
           (xt/x:set-key result "status" "ok")
           (when complete-fn (complete-fn true result))
           (return))))
  (return (xt/x:arr-map fields
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
                :fields   (xtd/obj-map validators
                                       (fn [_]
                                         (return {:status "pending"})))})
  (return result))
