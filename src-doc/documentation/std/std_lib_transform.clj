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

;; BEGIN merged documentation: plans/slop/summary/std_lib_transform_summary.md
;; sha256: 9742dbe8ac6374c44358b4017bfbe20897a6261475090b91286b3c475999a536
[[:chapter {:title "std.lib.transform: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-transform-summary-md"}]]
"## std.lib.transform: A Comprehensive Summary\n\nThe `std.lib.transform` module provides a powerful and extensible framework for transforming and normalizing data based on a schema. It's designed to process complex, nested data structures, particularly those used in database modeling or API definitions. The core of the module is the `normalise` function, which orchestrates a pipeline of transformations defined by various sub-namespaces. This module is crucial for ensuring data quality and consistency within the `foundation-base` project.\n\nThe module is organized into several sub-namespaces:\n\n### `std.lib.transform.allow`\n\nThis namespace provides transformation wrappers related to allowing or disallowing certain data based on schema definitions.\n\n*   **`wrap-branch-model-allow [f]`**: A wrapper that filters branches (sub-maps) based on schema rules.\n*   **`wrap-attr-model-allow [f]`**: A wrapper that filters attributes (values) based on schema rules.\n\n### `std.lib.transform.apply`\n\nThis namespace likely contains functions for applying transformations. (No direct `apply.clj` found, but `std.lib.transform.apply` is referenced in `std.lib.transform.clj`).\n\n### `std.lib.transform.base.alias`\n\nThis namespace handles aliasing of keys during transformation.\n\n*   **`wrap-alias [f]`**: A wrapper that applies key aliasing based on schema definitions.\n\n### `std.lib.transform.base.enum`\n\nThis namespace provides transformations related to enum types.\n\n*   **`wrap-single-enum [f]`**: A wrapper that handles single enum values, likely for validation or coercion.\n\n### `std.lib.transform.base.keyword`\n\nThis namespace provides transformations related to keywords.\n\n*   **`wrap-single-keyword [f]`**: A wrapper that handles single keyword values, likely for validation or coercion.\n\n### `std.lib.transform.base.type-check`\n\nThis namespace provides transformations related to type checking.\n\n*   **`wrap-single-type-check [f]`**: A wrapper that performs type checking on single values.\n\n### `std.lib.transform.convert`\n\nThis namespace provides transformations for converting data types.\n\n*   **`wrap-single-model-convert [f]`**: A wrapper that converts single data values based on schema type definitions.\n\n### `std.lib.transform.fill-assoc`\n\nThis namespace provides transformations for associating default values.\n\n*   **`wrap-model-fill-assoc [f]`**: A wrapper that fills in associated default values based on schema definitions.\n\n### `std.lib.transform.fill-empty`\n\nThis namespace provides transformations for filling in empty values.\n\n*   **`wrap-model-fill-empty [f]`**: A wrapper that fills in empty values based on schema definitions.\n\n### `std.lib.transform.ignore`\n\nThis namespace provides transformations for ignoring certain data.\n\n*   **`wrap-nil-model-ignore [f]`**: A wrapper that ignores `nil` values based on schema rules.\n\n### `std.lib.transform.mask`\n\nThis namespace provides transformations for masking data.\n\n*   **`wrap-model-pre-mask [f]`**: A wrapper that applies masking to data before other transformations.\n*   **`wrap-model-post-mask [f]`**: A wrapper that applies masking to data after other transformations.\n\n### `std.lib.transform.require`\n\nThis namespace provides transformations related to required fields.\n\n*   **`wrap-model-pre-require [f]`**: A wrapper that checks for required fields before other transformations.\n*   **`wrap-model-post-require [f]`**: A wrapper that checks for required fields after other transformations.\n\n### `std.lib.transform.validate`\n\nThis namespace provides transformations for validating data.\n\n*   **`wrap-single-model-validate [f]`**: A wrapper that validates single data values based on schema rules.\n\n### `std.lib.transform` (Facade Namespace)\n\nThis namespace acts as a facade, orchestrating the transformation pipeline and providing the main `normalise` function. It also defines common transformation directives and helper functions.\n\n*   **`tree-directives`**: A set of keywords representing the different stages or types of transformations that can be applied (e.g., `:pre-require`, `:fill-assoc`, `:validate`, `:convert`).\n*   **`submaps [m options subk]`**: Extracts sub-maps from a map `m` based on a set of `options` and a `subk` (subkey).\n*   **`wrap-plus [f]`**: A wrapper that handles additional attributes (denoted by `:+`) in the data, allowing for recursive normalization of these attributes.\n*   **`wrap-ref-path [f]`**: A wrapper used for tracing the reference path during normalization, useful for debugging or error reporting.\n*   **`wrap-key-path [f]`**: A wrapper used for tracing the key path during normalization.\n*   **`normalise-loop [tdata tsch nsv interim fns datasource]`**: The core recursive loop for the `normalise` function, iterating through data and applying transformations based on the schema.\n*   **`normalise-nil [subdata _ nsv interim datasource]`**: Handles cases where a sub-schema is not found, typically throwing an error.\n*   **`normalise-attr [subdata [attr] nsv interim fns datasource]`**: Handles the normalization of individual attributes, including collections of attributes.\n*   **`normalise-single [subdata [attr] nsv interim fns datasource]`**: Handles the normalization of single attribute values, including reference resolution.\n*   **`normalise-expression [subdata [attr] nsv interim datasource]`**: Normalizes expressions within the data.\n*   **`normalise-wrap [fns wrappers]`**: Applies a series of wrappers to a set of normalization functions.\n*   **`normalise-wrapper-fns`**: A map linking wrapper keywords (e.g., `:plus`, `:fill-assoc`, `:alias`) to their corresponding wrapper functions.\n*   **`normalise-wrappers [datasource & [additions fns]]`**: Constructs a map of wrapped normalization functions based on the pipeline configuration and available wrappers.\n*   **`normalise-base [tdata datasource wrappers]`**: The base function that applies the wrapped normalization functions to the data.\n*   **`normalise [data datasource & [wrappers]]`**: The main function for transforming and normalizing data. It takes `data`, a `datasource` (containing the schema and pipeline configuration), and optional `wrappers`. It orchestrates the entire transformation pipeline, including pre- and post-processing steps.\n\n**Overall Importance:**\n\nThe `std.lib.transform` module is a critical component for data management and processing within the `foundation-base` project. Its key contributions include:\n\n*   **Schema-Driven Data Transformation:** Provides a declarative and extensible way to transform data based on a defined schema, ensuring data consistency and adherence to rules.\n*   **Robust Data Validation:** Integrates various validation steps (required fields, type checks, custom checks) into the transformation pipeline.\n*   **Automated Data Coercion and Defaulting:** Handles automatic type conversion, filling in default values, and associating related data.\n*   **Flexible Transformation Pipeline:** Allows for a highly customizable sequence of transformations, including pre- and post-processing hooks.\n*   **Error Reporting and Tracing:** Provides mechanisms for reporting errors during transformation, including tracing the path of data and references.\n*   **Modularity and Extensibility:** The use of wrappers and sub-namespaces allows for easy extension with new transformation types and rules.\n\nBy offering these comprehensive data transformation capabilities, `std.lib.transform` significantly enhances the `foundation-base` project's ability to manage and process diverse data models accurately and consistently, which is vital for its multi-language development ecosystem.\n"
;; END merged documentation: plans/slop/summary/std_lib_transform_summary.md
