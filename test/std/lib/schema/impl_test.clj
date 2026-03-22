(ns std.lib.schema.impl-test
  (:require [std.lib.schema.impl :refer :all])
  (:use code.test))

^{:refer std.lib.schema.impl/simplify :added "3.0"}
(fact "helper function for easier display of spirit schema"
  ^:hidden
  
  (simplify {:account/name  [{:type :long}]
             :account/email [{:type :string
                              :cardinality :many}]
             :email/accounts [{:type :ref
                               :cardinality :many
                               :ref {:ns :account}}]})
  => {:email {:accounts :&account<*>}
      :account {:email :string<*> :name :long}})

^{:refer std.lib.schema.impl/create-lookup :added "3.0"}
(fact "lookup from flat schema mainly for reverse refs"
  ^:hidden
  
  (create-lookup
   {:account/name   [{}]
    :account/email  [{}]
    :email/accounts [{:ident :email/accounts
                      :type :ref
                      :ref {:type :reverse
                            :key :account/_email}}]})
  => {:email/accounts :email/accounts
      :account/_email :email/accounts
      :account/email :account/email
      :account/name :account/name})

^{:refer std.lib.schema.impl/create-flat-schema :added "3.0"}
(fact "creates a flat schema from an input map"
  ^:hidden
  
  (create-flat-schema {:account {:email [{:type    :ref
                                          :ref     {:ns  :email}}]}})
  => {:email/accounts [{:ident :email/accounts
                        :type :ref
                        :cardinality :many
                        :ref {:ns :account
                              :type :reverse
                              :key :account/_email
                              :val :accounts
                              :rval :email
                              :rkey :account/email
                              :rident :account/email}}]
      :account/email [{:ident :account/email
                       :type :ref
                       :cardinality :one
                       :ref  {:ns :email
                              :type :forward
                              :key :account/email
                              :val :email
                              :rval :accounts
                              :rkey :account/_email
                              :rident :email/accounts}}]})

^{:refer std.lib.schema.impl/vec->map :added "3.0"}
(fact "turns a vec schema to a map"
  ^:hidden
  
  (vec->map [:account [:id    {:type :long}
                       :name  {:type :text}]])
  => {:account {:id [{:type :long, :order 0}],
                :name [{:type :text, :order 1}]}})

^{:refer std.lib.schema.impl/schema-map :added "3.0"}
(fact "creates a schema from a map"
  ^:hidden
  
  (-> (schema-map {:account/name   [{}]
                   :account/email  [{:ident   :account/email
                                     :type    :ref
                                     :ref     {:ns  :email}}]})
      :flat
      simplify)
  => {:account {:name :string, :email :&email},
      :email {:accounts :&account<*>}})

^{:refer std.lib.schema.impl/schema :added "3.0"}
(fact "creates an extended schema for use by spirit"
  ^:hidden
  
  (-> (schema [:account [:name  {}
                         :email {:type :ref :ref {:ns :email}}]])
      :flat
      simplify)
  => {:email {:accounts :&account<*>}
      :account {:email :&email
                :name :string}})

^{:refer std.lib.schema.impl/schema? :added "3.0"}
(fact "checks if object is a schema"
  ^:hidden
  
  (schema? (schema {:user/email [{}]}))
  => true)

(comment
  (use 'jvm.tool)
  (./arrange)
  (./scaffold)
  (./import))
