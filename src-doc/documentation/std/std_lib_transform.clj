(ns documentation.std-lib-transform
  (:require [std.lib.schema :as schema]
            [std.lib.transform :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.transform` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Normalise flat keys into nested records"}]]

"`normalise` takes data with slash-separated keys and a schema, and returns a nested record tree. It is the main entry point for the transform pipeline."

(fact "normalise flat keys against a schema"
  (let [sch (schema/schema [:Account
                            [:id   {:type :uuid :scope :-/id}]
                            [:name {:type :string :scope :-/data}]])]
    (normalise {:Account/name "Chris"
                :Account/age  10}
               {:schema sch}
               {}))
  => {:Account {:name "Chris" :age 10}})

(fact "normalise nested data with references"
  (let [sch (schema/schema [:Link
                            [:value {:type :string :scope :-/data}]
                            [:next  {:type :ref :ref {:ns :Link}}]])]
    (normalise {:Link/value "hello"
                :Link {:next/value "world"
                       :next/next {:value "!"}}}
               {:schema sch}
               {}))
  => {:Link {:value "hello"
             :next {:value "world"
                    :next {:value "!"}}}})

[[:section {:title "Submaps and wrappers"}]]

"`submaps` extracts the configuration for a single key across multiple directives. The wrapper helpers are combined into the normalise pipeline to enable tracing and extension."

(fact "extract submaps for a key"
  (submaps {:allow  {:account :check}
            :ignore {:account :check}}
           #{:allow :ignore}
           :account)
  => {:allow :check :ignore :check})

(fact "allow additional attributes with wrap-plus"
  (let [sch (schema/schema [:Account
                            [:id   {:type :uuid :scope :-/id}]
                            [:name {:type :string :scope :-/data}]])]
    (normalise {:Account {:name "Main"
                          :+    {:Account {:name "Extra"}}}}
               {:schema sch}
               {:normalise [wrap-plus]}))
  => {:Account {:name "Main"
                :+    {:Account {:name "Extra"}}}})

(fact "trace key paths through normalisation"
  (let [sch (schema/schema [:Account
                            [:id   {:type :uuid :scope :-/id}]
                            [:name {:type :string :scope :-/data}]])]
    (normalise {:Account {:+ {:Account {:WRONG "Chris"}}}}
               {:schema sch}
               {:normalise [wrap-plus]
                :normalise-branch [wrap-key-path]
                :normalise-attr [wrap-key-path]}))
  => (throws-info {:key-path [:Account :+ :Account :WRONG]}))

[[:section {:title "Lower-level normalisation loops"}]]

"`normalise-loop` walks a nested data map, `normalise-attr` processes a single attribute, and `normalise-single` follows reference attributes into their target entities."

(fact "normalise-loop over a simple map"
  (normalise-loop {:name "Chris" :age 10}
                  {:name [{:type :string :cardinality :one :ident :Account/name}]
                   :age  [{:type :long   :cardinality :one :ident :Account/age}]
                   :sex  [{:type :enum   :cardinality :one
                           :enum {:ns :Account.sex :values #{:m :f}}
                           :ident :Account/sex}]}
                  [:Account]
                  {}
                  {:normalise normalise-loop
                   :normalise-single normalise-single
                   :normalise-attr normalise-attr}
                  {})
  => {:name "Chris" :age 10})

(fact "normalise-attr on a single value"
  (normalise-attr "Chris"
                  [{:type :string :cardinality :one :ident :Account/name}]
                  [:Account :name]
                  {}
                  {:normalise-single normalise-single}
                  {})
  => "Chris")

(fact "normalise-single follows a reference"
  (normalise-single {:value "world"}
                    [{:type :ref
                      :ident :Link/next
                      :cardinality :one
                      :ref {:ns :Link
                            :rval :prev
                            :type :forward
                            :key :Link/next
                            :val :next
                            :rkey :Link/_next
                            :rident :Link/prev}}]
                    [:Link :next]
                    {}
                    {:normalise-attr normalise-attr
                     :normalise normalise-loop
                     :normalise-single normalise-single}
                    {:schema (schema/schema [:Link
                                             [:value {:type :string :scope :-/data}]])})
  => {:value "world"})

[[:section {:title "End-to-end: build a schema, normalise, and inspect"}]]

"Combine schema creation and normalisation to turn flat external data into a structured tree, then inspect the result."

(fact "build a schema and normalise flat input"
  (let [sch (schema/schema [:User
                            [:id    {:type :uuid :scope :-/id}]
                            [:name  {:type :string :scope :-/data}]
                            [:email {:type :string :scope :-/data}]])
        input {:User/id    #uuid "00000000-0000-0000-0000-000000000001"
               :User/name  "Ada"
               :User/email "ada@example.com"}]
    (normalise input {:schema sch} {})
    => {:User {:id    #uuid "00000000-0000-0000-0000-000000000001"
               :name  "Ada"
               :email "ada@example.com"}}))

[[:chapter {:title "API" :link "std.lib.transform"}]]

[[:api {:namespace "std.lib.transform"}]]
