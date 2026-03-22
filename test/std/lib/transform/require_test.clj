(ns std.lib.transform.require-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.require :as require])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus require/wrap-model-pre-require]
   :normalise-branch [graph/wrap-key-path]
   :normalise-attr   [graph/wrap-key-path]})

^{:refer std.lib.transform.require/process-require :added "3.0"}
(fact "Checks for correct entry"

  (require/process-require {:name :checked}
                           :no-required
                           {:name "Chris"}
                           [:account]
                           (-> (schema/schema examples/account-name-age-sex)
                               :tree
                               :account)
                           {})
  => {:name "Chris"})

^{:refer std.lib.transform.require/wrap-model-pre-require :added "3.0"}
(fact "Checks for data across elements and schema pre transforms"
  (graph/normalise
   {:account/orders #{{:number 1}
                      {:number 2}}}
   {:schema (schema/schema examples/account-orders-items-image)
    :pipeline {:pre-require
               {:account {:orders
                          {:number :checked}}}}}
   *wrappers*)
  => {:account {:orders #{{:number 1}
                          {:number 2}}}}

  (graph/normalise
   {:account/orders #{{:items {:name "stuff"}}
                      {:number 2}}}
   {:schema (schema/schema
             examples/account-orders-items-image)
    :pipeline {:pre-require
               {:account
                {:orders {:number :checked}}}}}
   *wrappers*)
  => (throws-info {:data {:items {:name "stuff"}}
                   :nsv [:order :number]
                   :id :require-key}))

^{:refer std.lib.transform.require/wrap-model-post-require :added "3.0"}
(fact "Checks for data across elements and schema post transforms"

  (graph/normalise
   {:account/name "Chris"}
   {:schema (schema/schema examples/account-name-age-sex)
    :pipeline {:post-require {:account {:name :checked}}}}
   {:normalise [require/wrap-model-post-require]})
  => {:account {:name "Chris"}}

  (graph/normalise
   {:account/age 10}
   {:schema (schema/schema examples/account-name-age-sex)
    :pipeline {:post-require
               {:account {:name :checked}}}}
   {:normalise [require/wrap-model-post-require]})
  => (throws-info {:nsv [:account :name]
                   :id :require-key}))

(comment
  (./import)
  (./scaffold))
