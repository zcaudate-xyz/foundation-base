(ns std.lib.transform.allow-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.allow :as allow])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus]
   :normalise-branch [allow/wrap-branch-model-allow graph/wrap-key-path]
   :normalise-attr   [allow/wrap-attr-model-allow graph/wrap-key-path]})

^{:refer std.lib.transform.allow/wrap-branch-model-allow :added "3.0"}
(fact "Works together with wrap-attr-model-allow to control access to data"
  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:allow {}}}
                   *wrappers*)
  => (throws-info {:data {:name "Chris"}
                   :key-path [:account]
                   :id :not-allowed
                   :nsv [:account]})

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:allow {:account {:name :checked}}}}
                   *wrappers*)
  => {:account {:name "Chris"}}
  (graph/normalise {:account {:name "Chris"
                              :age 10}}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:allow {:account {:name :checked}}}}
                   *wrappers*)
  => (throws-info {:data 10
                   :key-path [:account :age]
                   :id :not-allowed
                   :nsv [:account :age]}))

^{:refer std.lib.transform.allow/wrap-attr-model-allow :added "3.0"}
(fact "wrapper function for only allowing values defined to be included"

  (graph/normalise {:account {:name "Chris"
                              :age 10}}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:allow {:account {:name :checked}}}}
                   *wrappers*)
  => (throws))

^{:refer std.lib.transform.allow/wrap-branch-model-allow.refs :added "3.0" :adopt true}
(fact "Allow with refs"
  (graph/normalise {:account/orders {:number 1}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:allow {:account {:orders {:number :checked}}}}}
                   *wrappers*)
  => {:account {:orders {:number 1}}}

  (graph/normalise {:account {:user "Chris"}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:allow {:account {:user :checked}}}}
                   *wrappers*)
  => {:account {:user "Chris"}}

  (graph/normalise {:account {:orders {:+ {:account {:user "Chris"}}}}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:allow {:account {:user :checked
                                                 :orders {:+ {:account {:user :checked}}}}}}}
                   *wrappers*)
  => {:account {:orders {:+ {:account {:user "Chris"}}}}}

  (graph/normalise {:account {:orders {:+ {:account {:user "Chris"}}}}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:allow {:account {:user :checked
                                                 :orders {:+ {:order {:number :checked}}}}}}}
                   *wrappers*)
  => (throws-info {:key-path [:account :orders :+ :account]
                   :nsv [:account]
                   :data {:user "Chris"}
                   :id :not-allowed}))

(comment
  (./import)
  (./scaffold))
