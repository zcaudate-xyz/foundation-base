(ns xt.lang.event-test-helpers
  (:require [std.lang :as l]))

(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(defn.xt walk
  [obj pre-fn post-fn]
  (:= obj (pre-fn obj))
  (cond (xt/x:nil? obj)
        (return (post-fn obj))

        (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/walk v pre-fn post-fn)))
            (return (post-fn out)))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn obj))))

(defn.xt get-data
  [obj]
  (var data-fn
       (fn [obj]
         (if (or (xt/x:is-string? obj)
                 (xt/x:is-number? obj)
                 (xt/x:is-boolean? obj)
                 (xt/x:is-object? obj)
                 (xt/x:is-array? obj)
                 (xt/x:nil? obj))
           (return obj)
           (return (xt/x:cat "<" (k/type-native obj) ">")))))
  (return (-/walk obj k/identity data-fn)))

(defn.xt get-spec
  [obj]
  (var spec-fn
       (fn [obj]
         (if (not (or (xt/x:is-object? obj)
                      (xt/x:is-array? obj)))
           (return (k/type-native obj))
           (return obj))))
  (return (-/walk obj k/identity spec-fn)))

(defn.xt id-fn
  [x]
  (return (xt/x:get-key x "id")))
