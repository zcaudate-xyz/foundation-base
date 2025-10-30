## std.lib.transform: A Comprehensive Summary

The `std.lib.transform` module provides a powerful and extensible framework for transforming and normalizing data based on a schema. It's designed to process complex, nested data structures, particularly those used in database modeling or API definitions. The core of the module is the `normalise` function, which orchestrates a pipeline of transformations defined by various sub-namespaces. This module is crucial for ensuring data quality and consistency within the `foundation-base` project.

The module is organized into several sub-namespaces:

### `std.lib.transform.allow`

This namespace provides transformation wrappers related to allowing or disallowing certain data based on schema definitions.

*   **`wrap-branch-model-allow [f]`**: A wrapper that filters branches (sub-maps) based on schema rules.
*   **`wrap-attr-model-allow [f]`**: A wrapper that filters attributes (values) based on schema rules.

### `std.lib.transform.apply`

This namespace likely contains functions for applying transformations. (No direct `apply.clj` found, but `std.lib.transform.apply` is referenced in `std.lib.transform.clj`).

### `std.lib.transform.base.alias`

This namespace handles aliasing of keys during transformation.

*   **`wrap-alias [f]`**: A wrapper that applies key aliasing based on schema definitions.

### `std.lib.transform.base.enum`

This namespace provides transformations related to enum types.

*   **`wrap-single-enum [f]`**: A wrapper that handles single enum values, likely for validation or coercion.

### `std.lib.transform.base.keyword`

This namespace provides transformations related to keywords.

*   **`wrap-single-keyword [f]`**: A wrapper that handles single keyword values, likely for validation or coercion.

### `std.lib.transform.base.type-check`

This namespace provides transformations related to type checking.

*   **`wrap-single-type-check [f]`**: A wrapper that performs type checking on single values.

### `std.lib.transform.convert`

This namespace provides transformations for converting data types.

*   **`wrap-single-model-convert [f]`**: A wrapper that converts single data values based on schema type definitions.

### `std.lib.transform.fill-assoc`

This namespace provides transformations for associating default values.

*   **`wrap-model-fill-assoc [f]`**: A wrapper that fills in associated default values based on schema definitions.

### `std.lib.transform.fill-empty`

This namespace provides transformations for filling in empty values.

*   **`wrap-model-fill-empty [f]`**: A wrapper that fills in empty values based on schema definitions.

### `std.lib.transform.ignore`

This namespace provides transformations for ignoring certain data.

*   **`wrap-nil-model-ignore [f]`**: A wrapper that ignores `nil` values based on schema rules.

### `std.lib.transform.mask`

This namespace provides transformations for masking data.

*   **`wrap-model-pre-mask [f]`**: A wrapper that applies masking to data before other transformations.
*   **`wrap-model-post-mask [f]`**: A wrapper that applies masking to data after other transformations.

### `std.lib.transform.require`

This namespace provides transformations related to required fields.

*   **`wrap-model-pre-require [f]`**: A wrapper that checks for required fields before other transformations.
*   **`wrap-model-post-require [f]`**: A wrapper that checks for required fields after other transformations.

### `std.lib.transform.validate`

This namespace provides transformations for validating data.

*   **`wrap-single-model-validate [f]`**: A wrapper that validates single data values based on schema rules.

### `std.lib.transform` (Facade Namespace)

This namespace acts as a facade, orchestrating the transformation pipeline and providing the main `normalise` function. It also defines common transformation directives and helper functions.

*   **`tree-directives`**: A set of keywords representing the different stages or types of transformations that can be applied (e.g., `:pre-require`, `:fill-assoc`, `:validate`, `:convert`).
*   **`submaps [m options subk]`**: Extracts sub-maps from a map `m` based on a set of `options` and a `subk` (subkey).
*   **`wrap-plus [f]`**: A wrapper that handles additional attributes (denoted by `:+`) in the data, allowing for recursive normalization of these attributes.
*   **`wrap-ref-path [f]`**: A wrapper used for tracing the reference path during normalization, useful for debugging or error reporting.
*   **`wrap-key-path [f]`**: A wrapper used for tracing the key path during normalization.
*   **`normalise-loop [tdata tsch nsv interim fns datasource]`**: The core recursive loop for the `normalise` function, iterating through data and applying transformations based on the schema.
*   **`normalise-nil [subdata _ nsv interim datasource]`**: Handles cases where a sub-schema is not found, typically throwing an error.
*   **`normalise-attr [subdata [attr] nsv interim fns datasource]`**: Handles the normalization of individual attributes, including collections of attributes.
*   **`normalise-single [subdata [attr] nsv interim fns datasource]`**: Handles the normalization of single attribute values, including reference resolution.
*   **`normalise-expression [subdata [attr] nsv interim datasource]`**: Normalizes expressions within the data.
*   **`normalise-wrap [fns wrappers]`**: Applies a series of wrappers to a set of normalization functions.
*   **`normalise-wrapper-fns`**: A map linking wrapper keywords (e.g., `:plus`, `:fill-assoc`, `:alias`) to their corresponding wrapper functions.
*   **`normalise-wrappers [datasource & [additions fns]]`**: Constructs a map of wrapped normalization functions based on the pipeline configuration and available wrappers.
*   **`normalise-base [tdata datasource wrappers]`**: The base function that applies the wrapped normalization functions to the data.
*   **`normalise [data datasource & [wrappers]]`**: The main function for transforming and normalizing data. It takes `data`, a `datasource` (containing the schema and pipeline configuration), and optional `wrappers`. It orchestrates the entire transformation pipeline, including pre- and post-processing steps.

**Overall Importance:**

The `std.lib.transform` module is a critical component for data management and processing within the `foundation-base` project. Its key contributions include:

*   **Schema-Driven Data Transformation:** Provides a declarative and extensible way to transform data based on a defined schema, ensuring data consistency and adherence to rules.
*   **Robust Data Validation:** Integrates various validation steps (required fields, type checks, custom checks) into the transformation pipeline.
*   **Automated Data Coercion and Defaulting:** Handles automatic type conversion, filling in default values, and associating related data.
*   **Flexible Transformation Pipeline:** Allows for a highly customizable sequence of transformations, including pre- and post-processing hooks.
*   **Error Reporting and Tracing:** Provides mechanisms for reporting errors during transformation, including tracing the path of data and references.
*   **Modularity and Extensibility:** The use of wrappers and sub-namespaces allows for easy extension with new transformation types and rules.

By offering these comprehensive data transformation capabilities, `std.lib.transform` significantly enhances the `foundation-base` project's ability to manage and process diverse data models accurately and consistently, which is vital for its multi-language development ecosystem.
