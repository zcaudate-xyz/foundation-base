(ns rt.postgres.typed-test
  (:require [rt.postgres.grammar.typed-common :as types]
            [rt.postgres.typed :as typed])
  (:use code.test))

^{:refer rt.postgres.typed/clear-registry! :added "4.1"}
(fact "clear-registry! empties the type registry"
  (typed/clear-registry!)
  (typed/register-type! 'test/Type (types/make-type-ref :primitive nil :test))
  (some? (typed/get-type 'test/Type)) => true
  (typed/clear-registry!)
  (typed/get-type 'test/Type) => nil)

^{:refer rt.postgres.typed/register-type! :added "4.1"}
(fact "register-type! adds a type to the registry"
  (typed/clear-registry!)
  (let [type-ref (types/make-type-ref :primitive nil :uuid)]
    (typed/register-type! 'test/Uuid type-ref)
    (typed/get-type 'test/Uuid) => type-ref))

^{:refer rt.postgres.typed/get-type :added "4.1"}
(fact "get-type retrieves a registered type"
  (typed/clear-registry!)
  (typed/get-type 'nonexistent/Type) => nil
  (let [type-ref (types/make-type-ref :primitive nil :text)]
    (typed/register-type! 'test/Text type-ref)
    (typed/get-type 'test/Text) => type-ref))

^{:refer rt.postgres.typed/get-table-shape :added "4.1"}
(fact "get-table-shape returns shape for a registered table"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User"
                                    [(types/make-column-def :id (types/make-type-ref :primitive nil :uuid) {:required true})]
                                    :id)]
    (typed/register-type! 'test/User table)
    (let [shape (typed/get-table-shape 'test/User)]
      (types/jsonb-shape? shape) => true
      (contains? (:fields shape) :id) => true)))

^{:refer rt.postgres.typed/list-tables :added "4.1"}
(fact "list-tables returns all registered table definitions"
  (typed/clear-registry!)
  (let [table (types/make-table-def "test" "User" [] :id)]
    (typed/register-type! 'test/User table)
    (let [tables (typed/list-tables)]
      (count tables) => 1
      (:name (first tables)) => "User")))

^{:refer rt.postgres.typed/list-functions :added "4.1"}
(fact "list-functions returns all registered function definitions"
  (typed/clear-registry!)
  (let [fn-def (types/make-fn-def "test" "get-user" [] [:jsonb] {} nil)]
    (typed/register-type! 'test/get-user fn-def)
    (let [fns (typed/list-functions)]
      (count fns) => 1
      (:name (first fns)) => "get-user")))

^{:refer rt.postgres.typed/list-enums :added "4.1"}
(fact "list-enums returns all registered enum definitions"
  (typed/clear-registry!)
  (let [enum (types/make-enum-def "test" "Status" #{:active :inactive} nil)]
    (typed/register-type! 'test/Status enum)
    (let [enums (typed/list-enums)]
      (count enums) => 1
      (:name (first enums)) => "Status")))

^{:refer rt.postgres.typed/load-runtime-tables :added "4.1"}
(fact "load-runtime-tables loads tables from runtime format"
  (let [tables-map {:User [:id {:type :uuid :primary true}
                           :name {:type :text}]
                    :Org [:id {:type :uuid :primary true}
                          :handle {:type :citext}]}
        loaded (typed/load-runtime-tables tables-map)]
    (count loaded) => 2
    (contains? loaded :User) => true
    (contains? loaded :Org) => true))

^{:refer rt.postgres.typed/register-runtime-tables! :added "4.1"}
(fact "register-runtime-tables! registers runtime tables in the registry"
  (typed/clear-registry!)
  (let [tables-map {:TestTable [:id {:type :uuid :primary true}]}
        loaded (typed/load-runtime-tables tables-map)]
    (typed/register-runtime-tables! loaded)
    (some? (typed/get-type :TestTable)) => true))
