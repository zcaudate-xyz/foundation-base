(ns std.lang.model.spec-xtalk.fn-dart
  (:require [std.lib.collection :as collection]))

(defn- add-sym
  [m]
  (collection/map-entries (fn [[k v]]
                            [k (assoc v :symbol #{(symbol (name k))
                                                  (symbol (clojure.string/replace (name k) "-" ":"))})])
                          m))

(defn dart-tf-x-len
  [[_ arr]]
  (list '. arr 'length))

(defn dart-tf-x-cat
  [[_ & args]]
  (apply list '+ args))

(defn dart-tf-x-print
  [[_ & args]]
  (apply list 'print args))

(defn dart-tf-x-arr-push
  [[_ arr item]]
  (list '. arr (list 'add item)))

(def +dart-core+
  (add-sym
   {:x-print    {:macro #'dart-tf-x-print    :emit :macro :value true}
    :x-len      {:macro #'dart-tf-x-len      :emit :macro :value true}
    :x-cat      {:macro #'dart-tf-x-cat      :emit :macro :value true}
    :x-arr-push {:macro #'dart-tf-x-arr-push :emit :macro}}))

(def +dart+
  (merge +dart-core+))
