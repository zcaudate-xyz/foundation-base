(ns hara.lang.rewrite.truthy
  (:require [hara.lang.rewrite.common :as common]
            [std.lib.collection :as collection]))

(defn dot-boolish-call?
  [form dot-boolish-calls]
  (and (collection/form? form)
       (= '. (first form))
       (collection/form? (nth form 2 nil))
       (contains? dot-boolish-calls
                  (first (nth form 2)))))

(defn boolish-form?
  ([form]
   (boolish-form? form {}))
  ([form {:keys [boolish-ops dot-boolish-calls recursive-not? recursive-and-or?]
          :or {boolish-ops #{}
               dot-boolish-calls #{}
               recursive-not? false
               recursive-and-or? false}}]
   (cond
     (instance? Boolean form)
     true

     (and recursive-not?
          (collection/form? form)
          (= 'not (first form)))
     (boolish-form? (second form)
                    {:boolish-ops boolish-ops
                     :dot-boolish-calls dot-boolish-calls
                     :recursive-not? recursive-not?
                     :recursive-and-or? recursive-and-or?})

     (and recursive-and-or?
          (collection/form? form)
          (#{'and 'or} (first form)))
     (every? #(boolish-form? %
                             {:boolish-ops boolish-ops
                              :dot-boolish-calls dot-boolish-calls
                              :recursive-not? recursive-not?
                              :recursive-and-or? recursive-and-or?})
             (rest form))

     (dot-boolish-call? form dot-boolish-calls)
     true

     (and (collection/form? form)
          (contains? boolish-ops (first form)))
     true

     :else
     false)))

(defn truthy-check-form
  [value]
  (list 'and
        (list 'x:not-nil? value)
        (list 'not= false value)))

(defn wrap-truthy-check
  ([source form]
   (wrap-truthy-check source form truthy-check-form))
  ([source form truthy-check]
   (common/with-form-meta source
      (truthy-check form))))

(defn truthy-form
  ([source form boolish?]
   (truthy-form source form boolish? wrap-truthy-check))
  ([source form boolish? wrap-truthy]
   (if (boolish? form)
     form
     (wrap-truthy source form))))

(defn truthy-or-form
  ([source value fallback]
   (truthy-or-form source value fallback truthy-check-form))
  ([source value fallback truthy-check]
   (common/with-form-meta source
      (list :?
            (truthy-check value)
            value
           fallback))))
