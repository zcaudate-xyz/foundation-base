## std.object: A Comprehensive Summary (including submodules)

The `std.object` module provides a powerful and extensible framework for introspecting, manipulating, and extending Java objects in Clojure. It offers a unified API for querying class metadata, accessing fields and methods, and defining custom behaviors for various object types. This module is crucial for bridging the gap between Clojure's dynamic nature and Java's static type system, enabling seamless interoperability and advanced metaprogramming capabilities.

### `std.object` (Main Namespace)

This namespace serves as the primary entry point for object manipulation, aggregating and re-exporting key functionalities from its submodules. It provides high-level functions for common object-oriented tasks.

**Key Re-exported Functions:**

*   From `std.object.element.class`: `class-convert`.
*   From `std.object.element.common`: `element?`, `element`, `context-class`.
*   From `std.object.element`: `to-element`, `class-info`, `class-hierarchy`, `constructor?`, `method?`, `field?`, `static?`, `instance?`, `public?`, `private?`, `plain?`.
*   From `std.object.query`: `query-class`, `query-instance`, `query-hierarchy`, `delegate`.
*   From `std.object.framework.access`: `get`, `get-in`, `set`, `keys`, `meta-clear`.
*   From `std.object.framework.read`: `meta-read`, `meta-read-exact`, `meta-read-exact?`, `to-data`, `to-map`, `read-ex`, `read-getters-form`, `read-getters`, `read-all-getters`, `read-fields`.
*   From `std.object.framework.struct`: `struct-fields`, `struct-getters`, `struct-accessor`.
*   From `std.object.framework.write`: `meta-write`, `meta-write-exact`, `meta-write-exact?`, `from-data`, `write-ex`, `write-setters-form`, `write-setters`, `write-all-setters`, `write-fields`.
*   From `std.object.framework`: `vector-like`, `map-like`, `string-like`, `unextend`.

### `std.object.element` (Object Element Abstraction)

This sub-namespace provides an abstraction layer for representing Java reflection objects (methods, fields, constructors) as Clojure data structures (`Element` records). This allows for a unified way to query and manipulate these elements.

**Core Concepts:**

*   **`Element` Record:** A data structure representing a Java reflection object, containing its `name`, `tag` (method, field, constructor), `modifiers`, `type`, `container` class, and `delegate` (the raw Java reflection object).

**Key Functions:**

*   **`to-element`**: Converts a `java.lang.reflect` object to an `Element`.
*   **`element-params`**: Returns the parameter types of an `Element`.
*   **`class-info`**: Retrieves information about a class.
*   **`class-hierarchy`**: Retrieves the class and interface hierarchy.
*   **`constructor?`, `method?`, `field?`**: Predicates for element types.
*   **`static?`, `instance?`, `public?`, `private?`, `protected?`, `plain?`**: Predicates for element modifiers.

### `std.object.element.class` (Class Conversion Utilities)

This sub-namespace provides utilities for converting between different representations of Java classes and types (e.g., `Class` objects, symbols, strings, raw type signatures).

**Key Functions:**

*   **`type->raw`**: Converts a `Class` or symbol to its raw type signature string.
*   **`raw-array->string`**: Converts a raw array type signature to a human-readable string.
*   **`raw->string`**: Converts a raw type signature to a human-readable string.
*   **`string-array->raw`**: Converts a human-readable array type string to a raw type signature.
*   **`string->raw`**: Converts any string to its raw type signature.
*   **`class-convert` (multimethod)**: Converts a class or type representation to another (e.g., `:class`, `:container`, `:symbol`, `:raw`, `:string`).

### `std.object.element.common` (Common Element Utilities)

This sub-namespace provides common helper functions for working with `Element` records, such as determining the context class and checking assignability.

**Key Functions:**

*   **`context-class`**: Returns the `Class` object for a given object or class.
*   **`assignable?`**: Checks if a sequence of classes is assignable to another sequence.
*   **`-invoke-element` (multimethod)**: Base method for extending `invoke` for `Element` types.
*   **`-to-element` (multimethod)**: Base method for extending `to-element` from Java reflection objects.
*   **`-element-params` (multimethod)**: Base method for extending parameter retrieval for `Element` types.
*   **`-format-element` (multimethod)**: Base method for extending `toString` for `Element` types.
*   **`element`**: Creates an `Element` record.
*   **`element?`**: Checks if an object is an `Element`.

### `std.object.element.impl.constructor` (Constructor Element Implementation)

This sub-namespace provides the implementation details for `Element` records representing Java constructors.

**Key Functions:**

*   Extends `common/-invoke-element` and `common/-to-element` for `java.lang.reflect.Constructor`.

### `std.object.element.impl.field` (Field Element Implementation)

This sub-namespace provides the implementation details for `Element` records representing Java fields.

**Key Functions:**

*   **`patch-field`**: Makes a `Field` accessible.
*   **`arg-params`**: Returns argument parameters for field getters/setters.
*   **`throw-arg-exception`**: Throws an argument exception.
*   **`invoke-static-field`**: Invokes a static field.
*   **`invoke-instance-field`**: Invokes an instance field.
*   Extends `common/-invoke-element` and `common/-to-element` for `java.lang.reflect.Field`.

### `std.object.element.impl.hierarchy` (Class Hierarchy Utilities)

This sub-namespace provides utilities for traversing and analyzing class hierarchies, particularly for finding method origins.

**Key Functions:**

*   **`has-method`**: Checks if a method exists in a class.
*   **`methods-with-same-name-and-count`**: Finds methods with the same name and parameter count.
*   **`has-overridden-method`**: Checks if a method is overridden.
*   **`origins`**: Lists all classes that contain a particular method.

### `std.object.element.impl.method` (Method Element Implementation)

This sub-namespace provides the implementation details for `Element` records representing Java methods.

**Key Functions:**

*   **`invoke-static-method`**: Invokes a static method.
*   **`invoke-instance-method`**: Invokes an instance method.
*   Extends `common/-invoke-element` and `common/-to-element` for `java.lang.reflect.Method`.

### `std.object.element.impl.multi` (Multi-Element Handling)

This sub-namespace handles the combination of multiple `Element` records (e.g., overloaded methods) into a single multi-element.

**Key Functions:**

*   **`get-name`**: Retrieves the common name of multiple elements.
*   **`to-element-array`**: Converts a nested map of elements to a flat sequence.
*   **`multi-element`**: Combines multiple elements into one.
*   **`to-element-map-path`**: Creates a map path for an element.
*   **`elegible-candidates`**: Finds eligible candidates based on argument list.
*   **`find-method-candidate`**: Finds the best method candidate.
*   **`find-field-candidate`**: Finds the best field candidate.
*   **`find-candidate`**: Finds the best element candidate (method or field).
*   Extends `common/-invoke-element` for multi-elements.

### `std.object.element.impl.type` (Element Type Utilities)

This sub-namespace provides low-level utilities for working with Java reflection types, including setting accessible flags and extracting modifiers.

**Key Functions:**

*   **`set-accessible`**: Sets the accessible flag for a reflection object.
*   **`add-annotations`**: Adds annotations to an element.
*   **`seed`**: Returns preliminary attributes for creating an element.

### `std.object.element.modifier` (Modifier Utilities)

This sub-namespace provides utilities for converting between integer representations of Java modifiers and human-readable sets of keywords.

**Key Functions:**

*   **`flags`, `field-flags`, `method-flags`**: Maps of modifier flags.
*   **`int-to-modifiers`**: Converts an integer to a set of modifier keywords.
*   **`modifiers-to-int`**: Converts a set of modifier keywords to an integer.

### `std.object.element.util` (Element Utility Functions)

This sub-namespace provides general utility functions for working with `Element` records, such as boxing arguments and matching parameter types.

**Key Functions:**

*   **`box-arg`**: Converts a value to a primitive type if necessary.
*   **`set-field`**: Sets the value of a field.
*   **`param-arg-match`**: Checks if an argument type matches a parameter type.
*   **`param-float-match`**: Matches float parameters to integer arguments.
*   **`is-congruent`**: Checks if argument types are congruent with parameter types.
*   **`throw-arg-exception`**: Throws an argument exception.
*   **`box-args`**: Boxes arguments to match parameter types.
*   **`format-element-method`**: Formats a method element.
*   **`element-params-method`**: Returns method parameters.

### `std.object.framework` (Object Framework)

This sub-namespace provides a framework for extending Clojure's object model to seamlessly interact with Java objects, allowing them to behave like Clojure maps, vectors, or strings.

**Key Functions:**

*   **`string-like` (macro)**: Extends a class to behave like a string.
*   **`map-like` (macro)**: Extends a class to behave like a map.
*   **`vector-like` (macro)**: Extends a class to behave like a vector.
*   **`unextend`**: Removes framework extensions for a class.
*   **`invoke-intern-object`**: Creates an invoke form for an object.

### `std.object.framework.access` (Object Accessors)

This sub-namespace provides functions for accessing and modifying object properties using keywords or paths, abstracting away direct Java reflection.

**Key Functions:**

*   **`meta-clear`**: Clears all meta-read and meta-write definitions for a class.
*   **`get-with-keyword`, `get-with-array`**: Retrieves properties using keywords or arrays.
*   **`get`**: Retrieves a property.
*   **`get-in`**: Retrieves a nested property.
*   **`keys`**: Retrieves all accessible property keys.
*   **`set-with-keyword`**: Sets a property using a keyword.
*   **`set`**: Sets properties using a map or key-value pair.

### `std.object.framework.map-like` (Map-Like Extension)

This sub-namespace provides the implementation for extending Java classes to behave like Clojure maps, allowing for property access and manipulation using map-like syntax.

**Key Functions:**

*   **`key-selection`**: Selects map entries based on include/exclude keys.
*   **`read-proxy-functions`**: Creates proxy functions for reading properties.
*   **`write-proxy-functions`**: Creates proxy functions for writing properties.
*   **`extend-map-read`**: Extends a class with map-like read capabilities.
*   **`extend-map-write`**: Extends a class with map-like write capabilities.
*   **`extend-map-like` (macro)**: Extends a class to behave like a map.

### `std.object.framework.print` (Print Extension)

This sub-namespace provides utilities for extending the `print-method` for Java classes, allowing for custom string representations.

**Key Functions:**

*   **`assoc-print-vars`**: Associates print-related variables.
*   **`format-value`**: Formats an object into a readable string.
*   **`extend-print` (macro)**: Extends the `print-method` for a class.

### `std.object.framework.read` (Object Reading)

This sub-namespace provides functions for reading object properties, including metadata, fields, and getter methods, and converting objects to Clojure data structures.

**Key Functions:**

*   **`meta-read`, `meta-read-exact`, `meta-read-exact?`**: Accesses and checks read metadata.
*   **`read-fields`, `read-all-fields`**: Reads fields from an object.
*   **`+read-template+`**: Template for getter functions.
*   **`+read-has-opts+`, `+read-is-opts+`, `+read-get-opts+`**: Options for getter methods.
*   **`create-read-method-form`**: Creates a method form for reading.
*   **`read-getters-form`, `read-getters`, `read-all-getters`**: Reads getter methods.
*   **`read-ex`**: Creates a getter method that throws an exception.
*   **`to-data`**: Converts an object to Clojure data.
*   **`to-map`**: Converts an object to a map.

### `std.object.framework.string-like` (String-Like Extension)

This sub-namespace provides the implementation for extending Java classes to behave like Clojure strings, allowing for conversion to and from string representations.

**Key Functions:**

*   **`extend-string-like` (macro)**: Extends a class to behave like a string.

### `std.object.framework.struct` (Struct-Like Access)

This sub-namespace provides functions for accessing object properties in a struct-like manner, using nested maps or vectors to define the access path.

**Key Functions:**

*   **`getter-function`**: Creates a getter function for a keyword.
*   **`field-function`**: Creates a field access function.
*   **`struct-getters`**: Retrieves properties using a getter specification.
*   **`struct-field-functions`**: Constructs field access functions.
*   **`struct-fields`**: Retrieves properties using a field specification.
*   **`struct-accessor`**: Creates an accessor function.
*   **`dir`**: Explores object fields.

### `std.object.framework.vector-like` (Vector-Like Extension)

This sub-namespace provides the implementation for extending Java classes to behave like Clojure vectors, allowing for sequential access to their elements.

**Key Functions:**

*   **`extend-vector-like` (macro)**: Extends a class to behave like a vector.

### `std.object.framework.write` (Object Writing)

This sub-namespace provides functions for writing object properties, including metadata, fields, and setter methods, and converting Clojure data structures to Java objects.

**Key Functions:**

*   **`meta-write`, `meta-write-exact`, `meta-write-exact?`**: Accesses and checks write metadata.
*   **`write-fields`, `write-all-fields`**: Writes fields to an object.
*   **`create-write-method-form`**: Creates a method form for writing.
*   **`write-setters-form`, `write-setters`, `write-all-setters`**: Writes setter methods.
*   **`write-ex`**: Creates a setter method that throws an exception.
*   **`write-constructor`**: Returns a constructor element.
*   **`write-setter-element`**: Constructs array elements for setters.
*   **`write-setter-value`**: Sets a property given a keyword and value.
*   **`from-empty`**: Creates an object from an empty constructor.
*   **`from-constructor`**: Creates an object from a constructor.
*   **`from-map`**: Creates an object from a map.
*   **`from-data`**: Creates an object from Clojure data.

### `std.object.query` (Object Querying)

This sub-namespace provides functions for querying Java classes and objects using reflection, allowing for flexible selection and filtering of members (methods, fields, constructors).

**Key Functions:**

*   **`all-class-members`**: Returns all raw reflected members of a class.
*   **`all-class-elements`**: Returns all `Element` records for a class.
*   **`select-class-elements`**: Selects `Element` records from a class.
*   **`query-class`**: Queries the Java view of a class.
*   **`select-supers-elements`**: Selects elements from superclasses.
*   **`query-supers`**: Queries superclasses.
*   **`query-hierarchy`**: Queries the entire class hierarchy.
*   **`all-instance-elements`**: Returns all instance elements.
*   **`select-instance-elements`**: Selects instance elements.
*   **`query-instance`**: Queries an object instance.
*   **`query-instance-hierarchy`**: Queries an object instance hierarchy.
*   **`apply-element`**: Applies a class element to arguments.
*   **`delegate`**: Creates a delegate for transparent field access.
*   **`invoke-intern-element`**: Creates an invoke form for an element.

### `std.object.query.filter` (Query Filtering)

This sub-namespace provides functions for filtering `Element` records based on various criteria, such as name, modifiers, parameter types, and origins.

**Key Functions:**

*   **`has-predicate?`, `has-name?`, `has-modifier?`, `has-params?`, `has-num-params?`, `has-any-params?`, `has-all-params?`, `has-type?`, `has-origins?`**: Predicates for filtering.
*   **`filter-by`**: Filters elements by a given criterion.
*   **`filter-terms-fn`**: Filters elements based on a group of terms.

### `std.object.query.input` (Query Input Processing)

This sub-namespace provides utilities for classifying and converting input arguments for object queries.

**Key Functions:**

*   **`args-classify`**: Classifies input arguments.
*   **`args-convert`**: Converts arguments to primitive classes.
*   **`args-group`**: Groups input arguments into categories.

### `std.object.query.order` (Query Ordering)

This sub-namespace provides functions for sorting and ordering `Element` records based on various criteria.

**Key Functions:**

*   **`sort-fn`**: Creates a sorting function.
*   **`sort-terms-fn`**: Sorts elements by terms.
*   **`first-terms-fn`**: Returns the first element.
*   **`merge-terms-fn`**: Merges elements.
*   **`select-terms-fn`**: Selects terms to output.
*   **`order`**: Orders elements based on a group of terms.

### Usage Pattern:

The `std.object` module is essential for:
*   **Java Interoperability:** Seamlessly interacting with Java objects and classes from Clojure.
*   **Metaprogramming:** Dynamically inspecting and manipulating Java objects at runtime.
*   **Framework Development:** Building extensible frameworks that can adapt to different Java object models.
*   **Data Conversion:** Converting between Java objects and Clojure data structures.
*   **Reflection-Based Tools:** Creating tools that analyze and interact with Java code.

By providing a powerful and flexible object introspection and manipulation framework, `std.object` empowers developers to leverage the full power of the Java ecosystem within their Clojure applications.