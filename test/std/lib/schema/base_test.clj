(ns std.lib.schema.base-test
  (:require [std.lib.schema.base :as base])
  (:use code.test))

^{:refer std.lib.schema.base/expand-scopes :added "4.0"}
(fact "expand scopes for all globbed keywords"

  (base/expand-scopes base/+scope-brief+)
  => #:*{:min #{:-/id :-/key},
         :info #{:-/info :-/id :-/key},
         :data #{:-/info :-/id :-/data :-/key},
         :default #{:-/info :-/id :-/ref :-/data :-/key},
         :detail #{:-/detail :-/info :-/id :-/data :-/key},
         :standard #{:-/detail :-/info :-/id :-/ref :-/data :-/key},
         :all #{:-/detail :-/info :-/id :-/ref :-/data :-/system :-/key},
         :everything #{:-/detail
                       :-/info
                       :-/hidden
                       :-/id
                       :-/ref
                       :-/data
                       :-/system
                       :-/key}})

^{:refer std.lib.schema.base/check-scope :added "4.0"}
(fact "check if a scope is valid"

  (base/check-scope :-/info)
  => :-/info

  (base/check-scope :-/WRONG)
  => (throws))

^{:refer std.lib.schema.base/attr-add-ident :added "3.0"}
(fact "adds the key of a pair as :ident to a schema property pair"

  (base/attr-add-ident [:person [{}]])
  => [:person [{:ident :person}]]

  (base/attr-add-ident [:person/address [{}]])
  => [:person/address [{:ident :person/address}]])

^{:refer std.lib.schema.base/attr-add-defaults :added "3.0"}
(fact "adds defaults to a given schema property pair"

  (base/attr-add-defaults [:person [{}]] [])
  => [:person [{}]]

  (base/attr-add-defaults [:person [{}]]
                          [{:default :string, :auto true, :id :type}
                           {:default :one, :auto true, :id :cardinality}])
  => [:person [{:cardinality :one :type :string}]]

  (base/attr-add-defaults [:person [{:cardinality :many :type :long}]]
                          [{:default :string, :auto true, :id :type}
                           {:default :one, :auto true, :id :cardinality}])
  => [:person [{:cardinality :many
                :type :long}]]

  (base/attr-add-defaults [:person [{}]]
                          [{:default false, :id :index}
                           {:default false, :id :fulltext}
                           {:default false, :id :noHistory}
                           {:default :string, :auto true, :id :type}
                           {:default :one, :auto true, :id :cardinality}])
  => [:person [{:index false
                :fulltext false
                :cardinality :one
                :noHistory false
                :type :string}]])

^{:refer std.lib.schema.base/defaults :added "3.0"}
(fact "constructs a map according to specifics"

  (base/defaults [:person/name {:default "Unknown"
                                :auto true}])
  => {:default "Unknown", :auto true, :id :person/name})

^{:refer std.lib.schema.base/all-auto-defaults :added "3.0"}
(fact "all automatic defaults for the schema"

  (base/all-auto-defaults)
  => [{:default :string, :auto true, :id :type}
      {:default :one, :auto true, :id :cardinality}])

^{:refer std.lib.schema.base/all-defaults :added "3.0"}
(fact "all defaults for the schema"

  (base/all-defaults)
  => [{:default :string, :auto true, :id :type}
      {:default :one, :auto true, :id :cardinality}])

^{:refer std.lib.schema.base/type-checks :added "3.0"}
(fact "gets type-checks according to category"

  ((base/type-checks :default :string) "Hello")
  => true)

(comment
  (./import)
  (./scaffold))
