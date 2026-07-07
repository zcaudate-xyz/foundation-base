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

;; BEGIN merged documentation: plans/slop/summary/std_lib_schema_summary.md
;; sha256: 0c49e791228991ef7bda0883866a03ff48f9bca047cb467522114afc371422d1
[[:chapter {:title "std.lib.schema: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-schema-summary-md"}]]

"The `std.lib.schema` module provides a powerful and flexible framework for defining, manipulating, and validating data schemas. It is designed to handle complex, nested data structures, particularly those used in database modeling or API definitions. The module offers functionalities for schema creation, flattening, nesting, reference handling, and various assertion checks, making it a central component for data integrity and consistency within the `foundation-base` project."

"The module is organized into several sub-namespaces:"

[[:section {:title "std.lib.schema.base" :link "merged-plans-slop-summary-std-lib-schema-summary-md-std-lib-schema-base"}]]

"This namespace defines fundamental concepts and utilities for schema processing, including scope definitions, default attribute values, and type checking."

"*   **`+scope-brief+`**: A map defining hierarchical scopes (e.g., `:*/min`, `:*/info`, `:*/data`) for schema attributes, allowing for grouping and filtering of columns based on their relevance.\n*   **`expand-scopes [m & [expanded]]`**: Expands globbed scope keywords (e.g., `:*/data`) into their constituent concrete scope keywords (e.g., `:-/info`, `:-/id`, `:-/data`).\n*   **`+scope+`**: A pre-expanded version of `+scope-brief+`, providing a complete mapping of globbed scopes to their concrete keywords.\n*   **`check-scope [scope]`**: Validates if a given scope keyword is a valid, known scope.\n*   **`base-meta`**: A map defining metadata for base schema attributes (e.g., `:ident`, `:type`, `:cardinality`), including their required status, default values, and validation checks.\n*   **`attr-add-ident [[k [attr :as v]]]`**: Adds the key `k` as the `:ident` attribute to a schema property.\n*   **`attr-add-defaults [[k [attr :as v]] dfts]`**: Adds default values to schema properties based on a list of default attribute definitions.\n*   **`defaults [[k prop]]`**: Extracts default and auto-generation properties from a schema attribute definition.\n*   **`all-auto-defaults [& [meta]]`**: Collects all attributes that have an `:auto` default value from `base-meta` or a custom meta.\n*   **`all-defaults [& [meta]]`**: Collects all attributes that have a `:default` value from `base-meta` or a custom meta.\n*   **`type-checks [t k]`**: A multimethod for retrieving type-checking functions based on a category `t` and type `k` (e.g., `:default :string` returns `string?`)."

[[:section {:title "std.lib.schema.impl" :link "merged-plans-slop-summary-std-lib-schema-summary-md-std-lib-schema-impl"}]]

"This namespace provides the core implementation for creating and manipulating `Schema` objects, including flattening, nesting, and creating lookup structures."

"*   **`simplify [flat]`**: A helper function to simplify a flattened schema for easier display, converting complex attribute maps into concise keyword representations.\n*   **`Schema` Record**: The central record for a schema, holding:\n    *   `flat`: A flattened representation of the schema (dot-separated keys).\n    *   `tree`: A nested (tree-like) representation of the schema.\n    *   `lu`: A lookup map for reverse references.\n    *   `vec`: An optional vector representation of the schema.\n    *   It implements `Object/toString` for informative display.\n*   **`create-lookup [fschm]`**: Creates a lookup map from a flattened schema, primarily used for resolving reverse references.\n*   **`create-flat-schema [m & [defaults]]`**: Creates a flattened schema from an input map `m`, applying default attribute values. It handles adding `:ident` and default attributes, and processes reference attributes.\n*   **`vec->map [v]`**: Converts a vector-based schema definition into a map-based representation.\n*   **`schema-map [m & [defaults]]`**: Creates a `Schema` object from a map-based schema definition.\n*   **`schema [x & [defaults]]`**: The main function for creating a `Schema` object. It can accept either a map or a vector representation of the schema.\n*   **`schema? [obj]`**: A predicate to check if an object is a `Schema` instance."

[[:section {:title "std.lib.schema.ref" :link "merged-plans-slop-summary-std-lib-schema-summary-md-std-lib-schema-ref"}]]

"This namespace focuses on handling reference attributes within a schema, including creating forward and reverse references and managing their properties."

"*   **`*ref-fn*`**: A dynamic var that can be bound to a function for adding additional parameters to reverse reference attributes.\n*   **`with:ref-fn [[ref-fn] & body]`**: A macro to bind `*ref-fn*` for a block of code.\n*   **`keyword-reverse [k]`**: Reverses a keyword by adding or removing an underscore prefix (e.g., `:a/b` -> `:a/_b`, `:a/_b` -> `:a/b`).\n*   **`keyword-reversed? [k]`**: Checks if a keyword is in its \"reversed\" form (starts with an underscore).\n*   **`is-reversible? [attr]`**: Determines if a reference attribute is eligible for automatic reverse reference generation.\n*   **`determine-rval [entry]`**: Determines the `:rval` (reverse value) for a reference attribute, handling pluralization and naming conventions.\n*   **`forward-ref-attr [[attr]]`**: Creates the `:ref` schema attribute for a forward reference, populating its properties (e.g., `:type`, `:key`, `:val`, `:rkey`, `:rident`).\n*   **`reverse-ref-attr [[attr]]`**: Creates the reverse `:ref` schema attribute for a backward reference, defining its properties and linking it back to the forward reference.\n*   **`forward-ref-attr-fn [entry]`**: A helper function for `forward-ref-attr`.\n*   **`attr-ns-pair [[attr]]`**: Constructs a `[:ns :ident-root]` pair for a schema attribute.\n*   **`mark-multiple [nsgroups & [output]]`**: Marks groups of namespace/ident pairs that have multiple entries, used in reference processing.\n*   **`ref-attrs [fschm]`**: Creates both forward and reverse reference attributes for a flattened schema, automatically generating reverse references where applicable."

[[:section {:title "std.lib.schema (Facade Namespace)" :link "merged-plans-slop-summary-std-lib-schema-summary-md-std-lib-schema-facade-namespace"}]]

"This namespace acts as a facade, re-exporting key functions from its sub-namespaces and providing additional utilities for schema introspection and validation."

"*   **`h/intern-in`**: Interns `impl/schema`, `impl/schema?`, `base/check-scope`, and `ref/with:ref-fn` into this namespace.\n*   **`expand-scopes [k]`**: Expands globbed scope keywords (e.g., `:*/data`) into their constituent concrete scope keywords using `base/+scope+`.\n*   **`linked-primary [tsch k schema]`**: Retrieves the primary key attribute of a linked table within the schema.\n*   **`order-keys [tsch ks]`**: Orders a list of keys based on their `:order` attribute defined in the schema.\n*   **`get-defaults [tsch]`**: Collects default values from the `sql` attributes of schema columns.\n*   **`check-valid-columns [tsch columns]`**: Checks if a given set of `columns` are valid (exist) within the schema.\n*   **`check-missing-columns [tsch columns required-fn]`**: Checks if any required columns (as determined by `required-fn`) are missing from a given set of `columns`.\n*   **`check-fn-columns [tsch data]`**: Performs validation checks on data using `:check` functions defined in the schema attributes.\n*   **`get-returning [tsch returning]`**: Collects a list of columns to be returned, expanding scope keywords (e.g., `:*/data`) and validating column names."

"**Overall Importance:**"

"The `std.lib.schema` module is a critical component for managing data definitions and ensuring data integrity within the `foundation-base` project. Its key contributions include:"

"*   **Centralized Schema Definition:** Provides a unified way to define and manage complex data schemas, including relationships and validation rules.\n*   **Data Transformation:** Offers tools for flattening and nesting schema representations, facilitating data processing and API interactions.\n*   **Automated Reference Handling:** Simplifies the creation and management of forward and reverse references between tables/entities, crucial for relational data.\n*   **Robust Validation:** Provides mechanisms for checking column validity, identifying missing required columns, and performing custom data validation.\n*   **Extensibility:** The modular design allows for easy extension with new attribute types, validation rules, and processing logic.\n*   **Code Generation Support:** The structured schema representation is ideal for generating database schemas, API documentation, or client-side data models."

"By offering these comprehensive schema management capabilities, `std.lib.schema` significantly enhances the `foundation-base` project's ability to handle diverse data models accurately and consistently, which is vital for its multi-language development ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_schema_summary.md
