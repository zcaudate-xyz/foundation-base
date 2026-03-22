(ns std.object.query.filter-test
  (:require [std.object.query.filter :refer :all])
  (:use code.test))

^{:refer std.object.query.filter/filter-terms-fn :added "3.0"}
(fact "listing outputs based upon different predicate conditions"

  ((filter-terms-fn {:name ["a"]})
   [{:name "a"} {:name "b"}])
  => [{:name "a"}]

  ((filter-terms-fn {:predicate [(fn [x] (= "a" (:name x)))]})
   [{:name "a"} {:name "b"}])
  => [{:name "a"}]

  ((filter-terms-fn {:origins [#{:a :b}]})
   [{:origins #{:a}} {:origins #{:c}}])
  => [{:origins #{:a}}]

  ((filter-terms-fn {:modifiers [:a]})
   [{:modifiers #{:a}} {:modifiers #{:c}}])
  => [{:modifiers #{:a}}])