(ns std.lib.transform-test
  (:use code.test)
  (:require [std.lib.transform :refer :all]
            [std.lib.schema :as schema]
            [example.data :as examples]))

^{:refer std.lib.transform/submaps :added "3.0"}
(fact "creates a submap based upon a lookup subkey"
  (submaps {:allow  {:account :check}
            :ignore {:account :check}} #{:allow :ignore} :account)
  => {:allow :check, :ignore :check})

^{:refer std.lib.transform/wrap-plus :added "3.0"}
(fact "Allows additional attributes (besides the link :ns) to be added to the entity"
  (normalise {:account {:orders {:+ {:account {:user "Chris"}}}}}
             {:schema (schema/schema examples/account-orders-items-image)}
             {:normalise [wrap-plus]})
  => {:account {:orders {:+ {:account {:user "Chris"}}}}}
  ^:hidden
  (normalise {:account {:orders {:+ {:account {:user "Chris"}}}}}
             {:schema (schema/schema examples/account-orders-items-image)}
             {})
  => (throws))

^{:refer std.lib.transform/wrap-ref-path :added "3.0"}
(fact "Used for tracing the entities through `normalise`"
  (normalise {:account {:orders {:+ {:account {:WRONG "Chris"}}}}}
             {:schema (schema/schema examples/account-orders-items-image)}
             {:normalise [wrap-ref-path wrap-plus]})

  => (throws-info {:ref-path
                   [{:account {:orders {:+ {:account {:WRONG "Chris"}}}}}
                    {:account {:WRONG "Chris"}}]}))

^{:refer std.lib.transform/wrap-key-path :added "3.0"}
(fact "Used for tracing the keys through `normalise`"
  (normalise {:account {:orders {:+ {:account {:WRONG "Chris"}}}}}
             {:schema (schema/schema examples/account-orders-items-image)}
             {:normalise [wrap-plus]
              :normalise-branch [wrap-key-path]
              :normalise-attr [wrap-key-path]})

  =>  (throws-info {:key-path [:account :orders :+ :account]}))

^{:refer std.lib.transform/normalise-loop :added "3.0"}
(fact "base loop for the normalise function"

  (normalise-loop {:name "Chris", :age 10}
                  {:name [{:type :string,
                           :cardinality :one,
                           :ident :account/name}],
                   :age [{:type :long,
                          :cardinality :one,
                          :ident :account/age}],
                   :sex [{:type :enum,
                          :cardinality :one,
                          :enum {:ns :account.sex, :values #{:m :f}},
                          :ident :account/sex}]}
                  [:account]
                  {}
                  {:normalise normalise-loop
                   :normalise-single normalise-single
                   :normalise-attr normalise-attr}
                  {:schema (schema/schema examples/account-name-age-sex)})
  => {:name "Chris", :age 10})

^{:refer std.lib.transform/normalise-nil :added "3.0"}
(fact "base function for treating nil values"

  (normalise-nil nil [:user :password] {} {} nil)
  => (throws))

^{:refer std.lib.transform/normalise-attr :added "3.0"}
(fact "base function for treating attributes"

  (normalise-attr "Chris"
                  [{:type :string, :cardinality :one, :ident :account/name}]
                  [:account :name]
                  {}
                  {:normalise-single normalise-single}
                  {})
  => "Chris")

^{:refer std.lib.transform/normalise-single :added "3.0"}
(fact "verifies and constructs a ref value"

  (normalise-single {:value "world"}
                    [{:type :ref,
                      :ident :link/next
                      :cardinality :one,
                      :ref {:ns :link,
                            :rval :prev,
                            :type :forward,
                            :key :link/next,
                            :val :next,
                            :rkey :link/_next,
                            :rident :link/prev}}]

                    [:link :next]
                    {}
                    {:normalise-attr normalise-attr
                     :normalise normalise-loop
                     :normalise-single normalise-single}
                    {:schema (schema/schema examples/link-value-next)})
  => {:value "world"})

^{:refer std.lib.transform/normalise-expression :added "3.0"}
(fact "normalises an expression"
  (resolve 'normalise-expression) => var?)

^{:refer std.lib.transform/normalise-wrap :added "3.0"}
(fact "helper function for normalise-wrappers"
  (resolve 'normalise-wrap) => var?)

^{:refer std.lib.transform/normalise-wrappers :added "3.0"}
(fact "adds function wrappers to the normalise functions"
  (resolve 'normalise-wrappers) => var?)

^{:refer std.lib.transform/normalise-base :added "3.0"}
(fact "base normalise function" ^:hidden

  (normalise-base {:account {:name "Chris"
                             :age 10}}
                  {:schema (schema/schema examples/account-name-age-sex)}
                  {})
  => {:account {:name "Chris", :age 10}})

^{:refer std.lib.transform/normalise :added "3.0"}
(fact "base normalise function"

  (normalise {:account/name "Chris"
              :account/age 10}
             {:schema (schema/schema examples/account-name-age-sex)}
             {})
  => {:account {:age 10, :name "Chris"}}

  (normalise {:link/value "hello"}
             {:schema (schema/schema examples/link-value-next)}
             {})
  => {:link {:value "hello"}}

  (normalise {:link/value "hello"
              :link {:next/value "world"
                     :next/next {:value "!"}}}
             {:schema (schema/schema examples/link-value-next)}
             {})

  => {:link {:next {:next {:value "!"}
                    :value "world"}
             :value "hello"}})

(comment
  (./import)
  (./scaffold))
