(ns std.lib.transform.mask-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.mask :as mask])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus mask/wrap-model-pre-mask]
   :normalise-branch [graph/wrap-key-path]
   :normalise-attr   [graph/wrap-key-path]})

^{:refer std.lib.transform.mask/process-mask :added "3.0"}
(fact "Determines correct output given data and mask"

  (mask/process-mask {:name :checked}
                     {:name "Chris" :age 10}
                     [:account]
                     {}
                     (-> (schema/schema examples/account-name-age-sex)
                         :tree
                         :person)
                     {})
  => {:age 10})

^{:refer std.lib.transform.mask/wrap-model-pre-mask :added "3.0"}
(fact "Masks data across elements and schema"

  (graph/normalise {:account/age 10}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:pre-mask {:account :checked}}}
                   *wrappers*)
  => {}

  (graph/normalise {:account/orders #{{:number 1 :items {:name "one"}}
                                      {:number 2 :items {:name "two"}}}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:pre-mask {:account {:orders {:number :checked}}}}}
                   *wrappers*)
  => {:account {:orders #{{:items {:name "one"}}
                          {:items {:name "two"}}}}}

  (graph/normalise {:account/age 10}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:pre-mask {:account {:name :checked}}}}
                   *wrappers*)
  => {:account {:age 10}})

^{:refer std.lib.transform.mask/wrap-model-post-mask :added "3.0"}
(fact "Masks data in pipeline post transforms "

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:post-mask {:account {:name :checked}}}}
                   {:normalise  [mask/wrap-model-post-mask]})
  => {:account {}})

(comment
  (./import)
  (./scaffold))

