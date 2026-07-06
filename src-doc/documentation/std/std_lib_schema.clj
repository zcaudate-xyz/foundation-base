(ns documentation.std-lib-schema
  (:require [std.lib.schema :as schema])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.schema` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Scope expansion"}]]

"Schemas tag columns with scopes such as `:-/id`, `:-/data`, and `:-/info`. The wildcard scopes `:` are expanded by `expand-scopes` into concrete scope keywords."

(fact "expand wildcard scopes"
  (schema/expand-scopes :*/data)
  => #{:-/info :-/id :-/data :-/key}

  (schema/expand-scopes :*/info)
  => #{:-/info :-/id :-/key})

[[:section {:title "Ordering and defaults"}]]

"`order-keys` sorts a list of columns by their declared `:order`. `get-defaults` collects SQL defaults such as auto-generated UUIDs and boolean flags."

(fact "order keys according to schema"
  (let [tsch {:cache  [{:order 3}]
              :status [{:order 1}]
              :id     [{:order 0}]
              :name   [{:order 2}]}]
    (schema/order-keys tsch [:cache :status :id :WRONG]))
  => '(:id :status :name :cache :WRONG))

(fact "collect default values"
  (let [tsch {:__deleted__ [{:sql {:default false} :order 8}]
              :id          [{:sql {:default '(uuid)} :order 0}]}]
    (schema/get-defaults tsch))
  => '{:__deleted__ false :id (uuid)})

[[:section {:title "Validation checks"}]]

"Validate incoming data against a schema: `check-valid-columns` rejects unknown columns, `check-missing-columns` ensures required columns are present, and `check-fn-columns` runs custom `:check` predicates."

(fact "check column validity"
  (let [tsch {:id     [{:required true :order 0}]
              :status [{:required true :order 1}]
              :name   [{:required true :order 2}]}]
    (schema/check-valid-columns tsch [:id :status])
    => [true]

    (schema/check-valid-columns tsch [:id :WRONG])
    => (contains-in [false {:not-allowed #{:WRONG}}])))

(fact "check required columns"
  (let [tsch {:id     [{:required true :order 0}]
              :status [{:required true :order 1}]
              :name   [{:required true :order 2}]}]
    (schema/check-missing-columns tsch [:status] :required)
    => [false {:missing #{:id :name}
               :required #{:id :status :name}}]

    (schema/check-missing-columns tsch [:status :name :id] :required)
    => [true]))

(fact "run per-column check functions"
  (let [tsch {:status [{:check keyword? :order 1}]}]
    (schema/check-fn-columns tsch {:status 'a})
    => {}))

[[:section {:title "Returning columns"}]]

"`get-returning` builds the column list for a `RETURNING` clause. It accepts explicit columns and wildcard scopes, and returns ordered attribute entries."

(fact "collect returning columns"
  (let [tsch {:__deleted__ [{:scope :-/hidden :order 8}]
              :id          [{:scope :-/id :order 0}]
              :status      [{:scope :-/info :order 1}]
              :name        [{:scope :-/data :order 2}]
              :cache       [{:scope :-/ref :order 3}]}]
    (->> (schema/get-returning tsch [:*/data :cache])
         (map first)))
  => '(:id :status :name :cache))

[[:section {:title "End-to-end: validate input and compute a return set"}]]

"A typical insert flow validates the requested columns, checks required fields, and then expands a scope-based return list."

(fact "validate data and build a RETURNING clause"
  (let [tsch   {:id          [{:scope :-/id    :required true :order 0}]
                :status      [{:scope :-/info  :required true :order 1}]
                :name        [{:scope :-/data  :required true :order 2}]
                :time-created [{:scope :-/system :order 3}]
                :__deleted__  [{:scope :-/hidden :sql {:default false} :order 4}]}
        input  [:id :status :name]]
    (schema/check-valid-columns tsch input)
    => [true]

    (schema/check-missing-columns tsch input :required)
    => [true]

    (->> (schema/get-returning tsch [:*/data :*/id])
         (map first))
    => '(:id :status :name)))

[[:chapter {:title "API" :link "std.lib.schema"}]]

[[:api {:namespace "std.lib.schema"}]]
