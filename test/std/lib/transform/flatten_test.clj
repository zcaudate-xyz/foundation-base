(ns std.lib.transform.flatten-test
  (:use code.test)
  (:require [std.lib.transform.flatten :refer :all]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph])
  (:refer-clojure :exclude [flatten]))

(def -schema- (schema/schema
               [:profile  [:id    {:type :text}
                           :name  {:type :text}]
                :student  [:id      {:type :text}
                           :class   {:type :ref :ref {:ns :class}}
                           :profile {:type :ref :ref {:ns :profile}}]
                :class    [:id    {:type :text}
                           :name  {:type :text}]]))

^{:refer std.lib.transform.flatten/clean-output :added "3.0"}
(fact "cleans ref keys in data"

  (clean-output {:id "student-a" :profile {:id "a" :name "Alice"}}
                (get-in -schema- [:tree :student]))
  => {:id "student-a", :profile :profile.id/a}

  (clean-output {:id "math-5" :students []}
                (get-in -schema- [:tree :class]))
  => {:id "math-5"})

^{:refer std.lib.transform.flatten/wrap-output :added "3.0"}
(fact "adding cleaned up data to table"
  (resolve 'wrap-output) => var?)

^{:refer std.lib.transform.flatten/flatten :added "3.0"}
(fact "converts a graph datastructure into a table"

  (flatten {:class
            {:id "maths-5"
             :name "MATHS 5"
             :students [{:id "student-a"
                         :profile {:id "a"
                                   :name "Alice"}}
                        {:id "student-b"
                         :profile {:id "b"
                                   :name "Bob"}}]}}
           -schema-)
  => {:class [{:id "maths-5", :name "MATHS 5"}],
      :student [{:id "student-a", :profile :profile.id/a, :class :class.id/maths-5}
                {:id "student-b", :profile :profile.id/b, :class :class.id/maths-5}],
      :profile [{:id "a", :name "Alice"} {:id "b", :name "Bob"}]})

(comment

  (./import))
