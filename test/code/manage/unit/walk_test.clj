(ns code.manage.unit.walk-test
  (:require [code.manage.unit.walk :refer :all])
  (:use code.test))

^{:refer code.manage.unit.walk/walk-string :added "3.0"}
(fact "helper function for file manipulation for string output"
  (walk-string "(ns a) (defn foo [])" '_ {} (constantly identity))
  => "(ns a) (defn foo [])")

^{:refer code.manage.unit.walk/walk-file :added "3.0"}
(fact "helper function for file manipulation used by import and purge"
  (with-redefs [slurp (constantly "(ns a) (defn foo [])")
                spit (constantly nil)]
    (walk-file "a" '_ {} (constantly identity)))
  => ['foo])
