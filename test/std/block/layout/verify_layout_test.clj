(ns std.block.layout.verify-layout-test
  (:use code.test)
  (:require [std.block.layout.bind :as bind]
            [std.block.construct :as construct]))

(def cond-form '(cond (= a 1) :a
                      (= b 2) :b
                      :else :c))

(def defmethod-form '(defmethod my-multi :default
                       [x]
                       (println x)
                       x))

(def letfn-form '(letfn [(f [x] (* x 2))
                         (g [y] (* y 3))]
                   (println (f 1))
                   (g 2)))

(def locking-form '(locking x
                     (println "locked because this is a very long line that should force a break")))

(def defonce-form '(defonce my-var
                     (atom {})))

(fact "layout for new forms"

  (construct/get-lines (bind/layout-main cond-form))
  => ["(cond (= a 1) :a"
      "      (= b 2) :b"
      "      :else   :c)"]

  (construct/get-lines (bind/layout-main defmethod-form))
  => ["(defmethod my-multi :default"
      "  [x]"
      "  (println x)"
      "  x)"]

  (construct/get-lines (bind/layout-main letfn-form))
  => ["(letfn [(f [x] (* x 2)) (g [y] (* y 3))]"
      "  (println (f 1))"
      "  (g 2))"]

  (construct/get-lines (bind/layout-main locking-form))
  => ["(locking x"
      "  (println \"locked because this is a very long line that should force a break\"))"]

  (construct/get-lines (bind/layout-main defonce-form))
  => ["(defonce my-var (atom {}))"])
