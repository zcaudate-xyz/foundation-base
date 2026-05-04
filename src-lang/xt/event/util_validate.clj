(ns xt.event.util-validate
  (:require [hara.lang :as l]
            [hara.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]]})

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

(defspec.xt validate-step
  [:fn [:xt/any :xt/str [:xt/maybe [:xt/array :xt/any]] :xt/int ValidationResult :xt/any :xt/any] :xt/promise])

(defspec.xt validate-field
  [:fn [:xt/any :xt/str [:xt/dict :xt/str :xt/any] ValidationResult :xt/any :xt/any] :xt/promise])

(defspec.xt validate-fields-loop
  [:fn [:xt/any [:xt/dict :xt/str :xt/any] ValidationResult [:xt/array :xt/any] :xt/int :xt/any :xt/any] :xt/promise])

(defspec.xt validate-all
  [:fn [:xt/any [:xt/dict :xt/str :xt/any] ValidationResult :xt/any :xt/any] :xt/promise])

(defn.xt validate-step
  "validates a single step"
  {:added "4.1"}
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
                   (when (xt/x:not-nil? hook-fn)
                     (hook-fn id false))
                   (when (xt/x:not-nil? complete-fn)
                     (complete-fn false result))
                   (return result)))
            (return
             (spec-promise/x:promise-catch
              (spec-promise/x:promise-then
               (spec-promise/x:promise-run (check (xt/x:get-key form field) form))
               (fn [ok]
                 (cond (== ok false)
                       (return (error-fn))

                       :else
                       (do (when (xt/x:not-nil? hook-fn)
                             (hook-fn id true))
                           (return (-/validate-step form
                                                    field
                                                    guards
                                                    (+ index 1)
                                                    result
                                                    hook-fn
                                                    complete-fn))))))
              (fn [_]
                (return (error-fn))))))

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
            (return (spec-promise/x:promise-run result)))))

(defn.xt validate-field
  "validates a single field"
  {:added "4.1"}
  [form field validators result hook-fn complete-fn]
  (var guards (xt/x:get-key validators field))
  (var complete-status-fn
       (fn [passed status]
         (when (not passed)
           (xt/x:set-key result "status" "errored"))
         (when (xt/x:not-nil? complete-fn)
           (complete-fn passed status))))
  (return (-/validate-step form field guards 0 result hook-fn complete-status-fn)))

(defn.xt validate-fields-loop
  "walks fields through a single validation promise chain"
  {:added "4.1"}
  [form validators result fields index hook-fn complete-fn]
  (when (== "errored" (xt/x:get-key result "status"))
    (when (xt/x:not-nil? complete-fn)
      (complete-fn false result))
    (return (spec-promise/x:promise-run result)))
  (when (>= index (xt/x:len fields))
    (xt/x:set-key result "status" "ok")
    (when (xt/x:not-nil? complete-fn)
      (complete-fn true result))
    (return (spec-promise/x:promise-run result)))
  (var field (xt/x:get-idx fields index))
  (return
   (spec-promise/x:promise-then
    (-/validate-field form field validators result hook-fn nil)
    (fn [_]
      (return (-/validate-fields-loop form
                                      validators
                                      result
                                      fields
                                      (+ index 1)
                                      hook-fn
                                      complete-fn))))))

(defn.xt validate-all
  "validates all fields and resolves to the mutated result"
  {:added "4.1"}
  [form validators result hook-fn complete-fn]
  (var fields (xt/x:obj-keys validators))
  (return (-/validate-fields-loop form validators result fields 0 hook-fn complete-fn)))

(defn.xt create-result
  "creates a validation result datastructure"
  {:added "4.1"}
  [validators]
  (var result {"::" "validation.result"
               :status "pending"
               :fields (xtd/obj-map validators
                                    (fn [_]
                                      (return {:status "pending"})))})
  (return result))
