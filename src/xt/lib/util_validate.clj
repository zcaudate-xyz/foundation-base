(ns xt.lang.util-validate
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
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
  (:= guards (:? (xt/x:nil? guards) [] guards))
  (cond (< index (xt/x:len guards))
        (do (var guard (xt/x:get-idx guards (xt/x:offset index)))
            (var [id m] guard)
            (var #{check message} m)
             (var error-fn
                  (fn []
                    (xt/x:obj-assign (xt/x:get-path result ["fields" field])
                                     {:status "errored"
                                      :id id
                                      :data (xt/x:get-key form field)
                                      :message message})
                     (when (xt/x:not-nil? hook-fn) (hook-fn id false))
                     (when (xt/x:not-nil? complete-fn) (complete-fn false result))))
             (xt/for:async [[ok err] (check (xt/x:get-key form field) form)]
               {:success  (cond (== ok false)
                                (return (error-fn))
                                
                                :else
                                (do (when (xt/x:not-nil? hook-fn) (hook-fn id true))
                                    (return (-/validate-step form field guards
                                                             (+ index 1)
                                                             result
                                                             hook-fn
                                                             complete-fn))))
                :error    (error-fn)}))
        
         :else
         (do (var entry (xt/x:get-path result ["fields" field]))
             (when (xt/x:not-nil? entry)
               (when (xt/x:has-key? entry "id")
                 (xt/x:del-key entry "id"))
               (when (xt/x:has-key? entry "data")
                 (xt/x:del-key entry "data"))
               (when (xt/x:has-key? entry "message")
                 (xt/x:del-key entry "message"))
               (xt/x:obj-assign entry {:status "ok"}))
             (when (xt/x:not-nil? complete-fn)
               (complete-fn true result))
            (return result))))

(defn.xt validate-field
  "validates a single field"
  {:added "4.0"}
  [form field validators result hook-fn complete-fn]
  (var guards (xt/x:get-key validators field))
  (var index 0)
  (var complete-status-fn
       (fn [passed status]
         (when (not passed)
           (xt/x:set-key result "status" "errored"))
         (when (xt/x:not-nil? complete-fn)
           (complete-fn passed status))))
  (return (-/validate-step form field guards index result
                            hook-fn
                            complete-status-fn)))

(defn.xt validate-all
  "validates all data"
  {:added "4.0"}
  [form validators result hook-fn complete-fn]
  (var fields (xt/x:obj-keys validators))
  (var complete-check-fn
       (fn [success _]
          (when (not success) (xt/x:set-key result "status" "errored"))
          (when (== "errored" (xt/x:get-key result "status"))
            (when (xt/x:not-nil? complete-fn) (complete-fn false result))
           (return))
          (when (xt/x:arr-every (xt/x:obj-vals (xt/x:get-key result "fields"))
                             (fn [e]
                               (return (== (xt/x:get-key e "status") "ok"))))
            (xt/x:set-key result "status" "ok")
            (when (xt/x:not-nil? complete-fn) (complete-fn true result))
            (return))))
  (xt/for:array [field fields]
    (-/validate-field
     form field validators result
     hook-fn complete-check-fn))
  (return true))

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
