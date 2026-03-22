(ns std.lib.transform.base.ref-test
  (:require [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.base.ref :refer :all])
  (:use code.test))

(def -schema- (schema/schema
               [:profile  [:id    {:type :text}
                           :name  {:type :text}]
                :student  [:id      {:type :text}
                           :class   {:type :ref :ref {:ns :class}}
                           :profile {:type :ref :ref {:ns :profile}}]
                :class    [:id    {:type :text}
                           :name  {:type :text}]]))

^{:refer std.lib.transform.base.ref/wrap-keyword-id :added "3.0"}
(fact "Allow keywords for refs"

  (graph/normalise-base {:student {:id "a"
                                   :profile :profile.id/a}}
                        {:schema -schema-}
                        {:normalise-single [wrap-keyword-id]})
  => {:student {:id "a", :profile :profile.id/a}} ^:hidden

  (graph/normalise-base {:student {:id "a"
                                   :profile "profile.id/a"}}
                        {:schema -schema-}
                        {:normalise-single [wrap-keyword-id]})
  => (throws))
