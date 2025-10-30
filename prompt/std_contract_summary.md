## std.contract: A Comprehensive Summary (including submodules)

The `std.contract` module provides a powerful and flexible system for defining and enforcing contracts (schemas) for data and functions in Clojure applications. Built on top of the Malli library, it offers a declarative way to specify data shapes, validate inputs and outputs, and ensure type safety. This module is crucial for improving code reliability, maintainability, and for facilitating robust API design.

### `std.contract` (Main Namespace)

This namespace serves as the primary entry point for the contract system, aggregating and re-exporting key functionalities from its submodules. It provides a high-level interface for defining and working with contracts.

**Key Re-exported Functions:**

*   From `std.contract.sketch`: `maybe`, `opt`, `fn`, `lax`, `opened`, `tighten`, `closed`, `norm`, `remove`, `as:sketch`, `as:schema`.
*   From `std.contract.type`: `defcase`, `defmultispec`, `defspec`, `spec?`, `common-spec`, `multi-spec`, `valid?`.
*   From `std.contract.binding`: `defcontract`.
*   From `malli.core`: `schema`, `schema?`.

### `std.contract.sketch` (Schema Sketching and Transformation)

This sub-namespace provides utilities for creating and transforming Malli schemas, offering a more convenient and expressive way to define data shapes. It introduces concepts like optional keys and nullable values directly into the schema definition process.

**Core Concepts:**

*   **Optional (`Optional` record):** Represents a key that may or may not be present in a map.
*   **Maybe (`Maybe` record):** Represents a value that can be `nil`.
*   **Func (`Func` record):** Represents a function schema, allowing validation of function arity or other properties.
*   **Schema Transformations:** Functions to modify schemas, such as making keys optional, values nullable, or closing/opening map schemas.

**Key Functions:**

*   **`optional-string`, `maybe-string`**: String representations for `Optional` and `Maybe` records.
*   **`as:optional`**: Creates an `Optional` record for a key.
*   **`optional?`**: Checks if an object is an `Optional` record.
*   **`as:maybe`**: Creates a `Maybe` record for a value.
*   **`maybe?`**: Checks if an object is a `Maybe` record.
*   **`func-string`, `func-invoke`**: String representation and invocation for `Func` records.
*   **`fn-sym`**: Extracts the symbol from a function.
*   **`func-form`, `func` (macro)**: Creates a `Func` record from a function form.
*   **`func?`**: Checks if an object is a `Func` record.
*   **`from-schema-map`**: Converts a Malli AST map representation into a sketch (Clojure map with `Optional`/`Maybe` records).
*   **`from-schema`**: Converts a Malli schema into a sketch.
*   **`to-schema-extend` (multimethod)**: An extension point for converting custom types to Malli schemas.
*   **`to-schema`**: Converts a sketch or other Clojure data into a Malli schema.
*   **`lax`**: Transforms a map schema to make all keys optional and all values nullable (`maybe`).
*   **`norm`**: Transforms a map schema to make all keys required (removes optionality).
*   **`closed`**: Closes a map schema, disallowing extra keys.
*   **`opened`**: Opens a map schema, allowing extra keys.
*   **`tighten`**: Transforms a map schema to make all keys required and all values non-nullable (removes `maybe`).
*   **`remove`**: Removes keys from a map schema.

### `std.contract.type` (Contract Definition and Validation)

This sub-namespace provides the core mechanisms for defining and validating data against schemas. It introduces `CommonSpec` for single schemas and `MultiSpec` for multimethod-like schema dispatch.

**Core Concepts:**

*   **`CommonSpec`:** A record that wraps a single Malli schema, providing methods for validation and introspection.
*   **`MultiSpec`:** A record that manages multiple schemas, dispatching to the appropriate one based on a dispatch function, similar to `defmulti`.
*   **Schema Validation:** Uses Malli's `validate` and `explain` functions for robust data validation.

**Key Functions:**

*   **`check`**: Validates data against a schema and throws an informative exception if invalid.
*   **`common-spec-invoke`, `common-spec-string`**: Invocation and string representation for `CommonSpec`.
*   **`combine`**: Merges multiple schemas (typically map schemas).
*   **`common-spec`**: Creates a `CommonSpec` from one or more sketches/schemas.
*   **`defspec` (macro)**: Defines a named `CommonSpec`.
*   **`multi-spec-invoke`, `multi-spec-string`**: Invocation and string representation for `MultiSpec`.
*   **`multi-gen-final`**: Generates the final Malli schema for a `MultiSpec`.
*   **`multi-spec-add`**: Adds a new case (dispatch value and schema) to a `MultiSpec`.
*   **`multi-spec-remove`**: Removes a case from a `MultiSpec`.
*   **`multi-spec`**: Creates a `MultiSpec` from a dispatch function and a map of cases.
*   **`defmultispec` (macro)**: Defines a named `MultiSpec`.
*   **`defcase` (macro)**: Adds a case to an existing `MultiSpec`.
*   **`spec?`**: Checks if an object is a `CommonSpec` or `MultiSpec`.
*   **`valid?`**: Checks if data is valid against a spec, returning `true` or `false`.

### `std.contract.binding` (Function Contract Binding)

This sub-namespace provides a mechanism to bind contracts directly to functions, enabling automatic input and output validation when the function is invoked.

**Core Concepts:**

*   **`Contract` record:** Encapsulates a function, its input schemas, and its output schema.
*   **Function Interception:** When a contract is bound to a var, the contract itself becomes the var's value, intercepting calls to perform validation.

**Key Functions:**

*   **`contract-info`**: Returns information about a contract.
*   **`contract-invoke`**: Invokes the underlying function of a contract, performing input and output validation.
*   **`contract-string`**: String representation for `Contract`.
*   **`Contract` (defimpl record)**: The concrete record type for a function contract.
*   **`contract?`**: Checks if an object is a `Contract` record.
*   **`contract-var?`**: Checks if a var's value is a `Contract`.
*   **`bound?`**: Checks if a contract is currently bound to its target var.
*   **`unbind`**: Removes a contract from a var, restoring the original function.
*   **`bind`**: Binds a contract to a var, replacing the original function with the contract.
*   **`parse-arg`**: Parses an input/output argument definition into a spec and options.
*   **`contract`**: Creates a `Contract` record.
*   **`defcontract` (macro)**: Defines a contract and binds it to a function var, enabling automatic validation.

### Usage Pattern:

The `std.contract` module is invaluable for:
*   **API Design and Enforcement:** Clearly defining the expected structure of data for APIs and ensuring adherence.
*   **Robustness:** Catching data inconsistencies and invalid inputs early, preventing bugs.
*   **Documentation:** Schemas serve as living documentation for data structures.
*   **Code Clarity:** Making explicit the assumptions about data shapes.
*   **Metaprogramming:** Programmatically generating and manipulating schemas.
*   **Test-Driven Development:** Writing tests that validate data against contracts.

By integrating Malli and providing a Clojure-idiomatic way to define and enforce contracts, `std.contract` significantly enhances the reliability and maintainability of `foundation-base` applications.