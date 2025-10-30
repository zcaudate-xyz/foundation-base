### `xt.lang.base-lib` Comprehensive Summary

The `xt.lang.base-lib` namespace, conventionally aliased as `k`, serves as a foundational cross-platform utility library within the `foundation-base` ecosystem. It provides a rich set of functions designed for common programming tasks, ensuring consistent behavior across different target languages (JavaScript, Lua, Python, etc.). The functions adhere to a clear `k/category-action` or `k/action` naming convention, making the API intuitive and predictable.

#### 1. Core Utilities and Predicates

These functions provide fundamental operations and checks.

*   **`k/identity`**: Returns its argument unchanged.
    *   Usage: `(k/identity x)`
*   **`k/T`**: A constant function that always returns `true`.
    *   Usage: `(k/T)`
*   **`k/F`**: A constant function that always returns `false`.
    *   Usage: `(k/F)`
*   **`k/NIL`**: A constant function that always returns `nil`.
    *   Usage: `(k/NIL)`
*   **`k/noop`**: A function that performs no operation and returns `nil`.
    *   Usage: `(k/noop)`
*   **`k/not-nil?`**: Checks if a value is not `nil` (i.e., not `null` or `undefined` in JS).
    *   Usage: `(k/not-nil? x)`
*   **`k/nil?`**: Checks if a value is `nil`.
    *   Usage: `(k/nil? x)`
*   **`k/is-empty?`**: Checks if a string, array, or object is empty.
    *   Usage: `(k/is-empty? coll)`
*   **`k/not-empty?`**: Checks if a string, array, or object is not empty.
    *   Usage: `(k/not-empty? coll)`
*   **`k/eq-nested`**: Performs a deep equality check on two data structures.
    *   Usage: `(k/eq-nested a b)`
*   **`k/fn?`**: Checks if a value is a function.
    *   Usage: `(k/fn? my-func)`
*   **`k/is-string?`**: Checks if a value is a string.
    *   Usage: `(k/is-string? "hello")`

#### 2. Object/Map Manipulation

Functions for working with JavaScript objects or Clojure maps.

*   **`k/get-key`**: Safely retrieves a property by key.
    *   Usage: `(k/get-key obj "prop")`
*   **`k/get-in`**: Safely retrieves a nested property using a path vector.
    *   Usage: `(k/get-in obj ["a" "b" 0])`
*   **`k/set-key`**: Sets a property on an object.
    *   Usage: `(k/set-key obj "prop" val)`
*   **`k/set-in`**: Sets a nested property using a path vector.
    *   Usage: `(k/set-in obj ["a" "b"] val)`
*   **`k/obj-assign`**: Merges properties from source objects into a target object (like `Object.assign`).
    *   Usage: `(k/obj-assign target src1 src2)`
*   **`k/obj-keys`**: Returns an array of an object's keys.
    *   Usage: `(k/obj-keys obj)`
*   **`k/obj-vals`**: Returns an array of an object's values.
    *   Usage: `(k/obj-vals obj)`
*   **`k/obj-pairs`**: Returns an array of `[key, value]` pairs (like `Object.entries`).
    *   Usage: `(k/obj-pairs obj)`
*   **`k/obj-map`**: Creates a new object by transforming each `[key, value]` pair.
    *   Usage: `(k/obj-map obj (fn [v k] ...))`
*   **`k/obj-filter`**: Creates a new object with `[key, value]` pairs that satisfy a predicate.
    *   Usage: `(k/obj-filter obj (fn [v k] ...))`
*   **`k/obj-clone`**: Creates a shallow copy of an object.
    *   Usage: `(k/obj-clone obj)`
*   **`k/obj-pick`**: Creates a new object with only specified keys.
    *   Usage: `(k/obj-pick obj ["key1" "key2"])`
*   **`k/obj-omit`**: Creates a new object excluding specified keys.
    *   Usage: `(k/obj-omit obj ["key1" "key2"])`
*   **`k/key-fn`**: Returns a function that extracts a specific key from an object.
    *   Usage: `(map arr (k/key-fn "id"))`

#### 3. Array/List Manipulation

Functions for working with JavaScript arrays or Clojure vectors/lists.

*   **`k/arr-append`**: Appends elements to an array (mutates).
    *   Usage: `(k/arr-append arr elem1 elem2)`
*   **`k/arr-pushl`**: Inserts an element at the beginning of an array (mutates, like `unshift`).
    *   Usage: `(k/arr-pushl arr elem)`
*   **`k/arr-push`**: Appends an element to an array (mutates, like `push`).
    *   Usage: `(k/arr-push arr elem)`
*   **`k/arr-popl`**: Removes and returns the first element (mutates, like `shift`).
    *   Usage: `(k/arr-popl arr)`
*   **`k/arr-pop`**: Removes and returns the last element (mutates, like `pop`).
    *   Usage: `(k/arr-pop arr)`
*   **`k/arr-insert`**: Inserts elements at a specific index (mutates).
    *   Usage: `(k/arr-insert arr index elem1 elem2)`
*   **`k/arr-remove`**: Removes elements from an array by index or value (mutates).
    *   Usage: `(k/arr-remove arr index count)` or `(k/arr-remove arr value)`
*   **`k/arr-slice`**: Returns a shallow copy of a portion of an array (non-mutating).
    *   Usage: `(k/arr-slice arr start end)`
*   **`k/arr-splice`**: Changes the contents of an array by removing or replacing existing elements and/or adding new elements (mutates).
    *   Usage: `(k/arr-splice arr start delete-count elem1)`
*   **`k/arr-concat`**: Concatenates arrays (non-mutating).
    *   Usage: `(k/arr-concat arr1 arr2)`
*   **`k/arr-map`**: Creates a new array with the results of calling a provided function on every element.
    *   Usage: `(k/arr-map arr (fn [e] ...))`
*   **`k/arr-filter`**: Creates a new array with all elements that pass the test implemented by the provided function.
    *   Usage: `(k/arr-filter arr (fn [e] ...))`
*   **`k/arr-reduce`**: Applies a function against an accumulator and each element in the array (from left to right) to reduce it to a single value.
    *   Usage: `(k/arr-reduce arr (fn [acc e] ...) initial)`
*   **`k/arr-foldl`**: Alias for `k/arr-reduce`.
*   **`k/arr-foldr`**: Applies a function against an accumulator and each element in the array (from right to left).
    *   Usage: `(k/arr-foldr arr (fn [acc e] ...) initial)`
*   **`k/arr-every`**: Checks if all elements in an array pass the test implemented by the provided function.
    *   Usage: `(k/arr-every arr (fn [e] ...))`
*   **`k/arr-some`**: Checks if at least one element in an array passes the test implemented by the provided function.
    *   Usage: `(k/arr-some arr (fn [e] ...))`
*   **`k/arr-find`**: Returns the value of the first element in the array that satisfies the provided testing function.
    *   Usage: `(k/arr-find arr (fn [e] ...))`
*   **`k/arr-find-index`**: Returns the index of the first element in the array that satisfies the provided testing function.
    *   Usage: `(k/arr-find-index arr (fn [e] ...))`
*   **`k/arr-includes`**: Checks if an array includes a certain value.
    *   Usage: `(k/arr-includes arr value)`
*   **`k/arr-join`**: Joins all elements of an array into a string.
    *   Usage: `(k/arr-join arr separator)`
*   **`k/arr-reverse`**: Reverses an array in place (mutates).
    *   Usage: `(k/arr-reverse arr)`
*   **`k/arr-sort`**: Sorts the elements of an array in place (mutates).
    *   Usage: `(k/arr-sort arr)`
*   **`k/arr-sort-by`**: Sorts the elements of an array in place based on a key function (mutates).
    *   Usage: `(k/arr-sort-by arr key-fn)`
*   **`k/arr-unique`**: Returns a new array with unique elements.
    *   Usage: `(k/arr-unique arr)`
*   **`k/arr-juxt`**: Creates a new array by applying a key function and a value function to each element.
    *   Usage: `(k/arr-juxt arr key-fn val-fn)`
*   **`k/first`**: Returns the first element of an array.
    *   Usage: `(k/first arr)`
*   **`k/last`**: Returns the last element of an array.
    *   Usage: `(k/last arr)`
*   **`k/len`**: Returns the length of an array or string.
    *   Usage: `(k/len arr)`

#### 4. Iteration Macros

Macros for iterating over collections.

*   **`k/for:array`**: Iterates over an array, providing index and element.
    *   Usage: `(k/for:array [[i e] arr] ...body...)`
*   **`k/for:object`**: Iterates over an object's key-value pairs.
    *   Usage: `(k/for:object [[k v] obj] ...body...)`

#### 5. String Manipulation

Functions for common string operations.

*   **`k/capitalize`**: Capitalizes the first letter of a string.
    *   Usage: `(k/capitalize "hello")`
*   **`k/to-uppercase`**: Converts a string to uppercase.
    *   Usage: `(k/to-uppercase "hello")`
*   **`k/to-lowercase`**: Converts a string to lowercase.
    *   Usage: `(k/to-lowercase "HELLO")`
*   **`k/trim`**: Removes whitespace from both ends of a string.
    *   Usage: `(k/trim "  hello  ")`
*   **`k/trim-left`**: Removes whitespace from the beginning of a string.
    *   Usage: `(k/trim-left "  hello")`
*   **`k/trim-right`**: Removes whitespace from the end of a string.
    *   Usage: `(k/trim-right "hello  ")`
*   **`k/split`**: Splits a string into an array of substrings.
    *   Usage: `(k/split "a,b,c" ",")`
*   **`k/join`**: Joins elements of an array into a string.
    *   Usage: `(k/join ["a" "b"] ",")`
*   **`k/starts-with?`**: Checks if a string starts with another string.
    *   Usage: `(k/starts-with? "hello" "he")`
*   **`k/ends-with?`**: Checks if a string ends with another string.
    *   Usage: `(k/ends-with? "hello" "lo")`
*   **`k/replace`**: Replaces occurrences of a substring or regex pattern.
    *   Usage: `(k/replace "hello world" "world" "there")`
*   **`k/substring`**: Extracts a part of a string.
    *   Usage: `(k/substring "hello" 1 3)`
*   **`k/pad-left`**: Pads the current string with another string (repeatedly) until the resulting string reaches the given length.
    *   Usage: `(k/pad-left "5" 2 "0")`
*   **`k/pad-right`**: Pads the current string with a given string (repeatedly) until the resulting string reaches the given length.
    *   Usage: `(k/pad-right "5" 2 "0")`

#### 6. JSON & Time

Functions for JSON serialization/deserialization and time.

*   **`k/json-encode`**: Converts a JavaScript value to a JSON string.
    *   Usage: `(k/json-encode obj)`
*   **`k/json-decode`**: Parses a JSON string, constructing the JavaScript value or object described by the string.
    *   Usage: `(k/json-decode json-str)`
*   **`k/now-ms`**: Returns the number of milliseconds elapsed since the Unix epoch.
    *   Usage: `(k/now-ms)`

#### 7. Math & Sorting

Mathematical operations and sorting utilities.

*   **`k/abs`**: Returns the absolute value of a number.
    *   Usage: `(k/abs -5)`
*   **`k/ceil`**: Returns the smallest integer greater than or equal to a given number.
    *   Usage: `(k/ceil 4.2)`
*   **`k/floor`**: Returns the largest integer less than or equal to a given number.
    *   Usage: `(k/floor 4.8)`
*   **`k/round`**: Returns the value of a number rounded to the nearest integer.
    *   Usage: `(k/round 4.5)`
*   **`k/max`**: Returns the largest of zero or more numbers.
    *   Usage: `(k/max 1 5 2)`
*   **`k/min`**: Returns the smallest of zero or more numbers.
    *   Usage: `(k/min 1 5 2)`
*   **`k/pow`**: Returns the base to the exponent power.
    *   Usage: `(k/pow 2 3)`
*   **`k/sqrt`**: Returns the square root of a number.
    *   Usage: `(k/sqrt 9)`
*   **`k/log`**: Returns the natural logarithm (base e) of a number.
    *   Usage: `(k/log 10)`
*   **`k/log10`**: Returns the base 10 logarithm of a number.
    *   Usage: `(k/log10 100)`
*   **`k/sin`**: Returns the sine of a number.
    *   Usage: `(k/sin 0)`
*   **`k/cos`**: Returns the cosine of a number.
    *   Usage: `(k/cos 0)`
*   **`k/tan`**: Returns the tangent of a number.
    *   Usage: `(k/tan 0)`
*   **`k/asin`**: Returns the arcsine of a number.
    *   Usage: `(k/asin 0)`
*   **`k/acos`**: Returns the arccosine of a number.
    *   Usage: `(k/acos 1)`
*   **`k/atan`**: Returns the arctangent of a number.
    *   Usage: `(k/atan 0)`
*   **`k/atan2`**: Returns the arctangent of the quotient of its arguments.
    *   Usage: `(k/atan2 1 1)`
*   **`k/random`**: Returns a pseudo-random number between 0 (inclusive) and 1 (exclusive).
    *   Usage: `(k/random)`
*   **`k/mod-pos`**: Positive modulo operation.
    *   Usage: `(k/mod-pos -5 3)`
*   **`k/mod-offset`**: Calculates offset for modular arithmetic.
    *   Usage: `(k/mod-offset 5 3)`
*   **`k/sort`**: Sorts an array in place (mutates).
    *   Usage: `(k/sort arr)`
*   **`k/sort-by`**: Sorts an array in place based on a key function (mutates).
    *   Usage: `(k/sort-by arr key-fn)`

#### 8. Debugging & Meta

Functions for debugging and retrieving metadata.

*   **`k/LOG!`**: Logs values along with file and line number information.
    *   Usage: `(k/LOG! my-var "message")`
*   **`k/TRACE!`**: Adds an entry to an internal trace log.
    *   Usage: `(k/TRACE! data tag)`
*   **`k/meta:info-fn`**: Gets metadata about the current function context (used internally by `LOG!`).
    *   Usage: `(k/meta:info-fn)`

---

### Recommendations for `xt.lang.base-lib` (based on naming convention)

The existing naming convention `k/category-action` or `k/action` is clear and consistent. New functions should adhere to this pattern.

#### 1. Core Utilities and Predicates

*   **`k/is-number?`**: Checks if a value is a number.
    *   Rationale: `k/is-string?` exists, so a numeric counterpart is logical.
    *   Symbol: `k/is-number?`
*   **`k/is-boolean?`**: Checks if a value is a boolean.
    *   Rationale: Completes the set of basic type predicates.
    *   Symbol: `k/is-boolean?`
*   **`k/is-object?`**: Checks if a value is a plain object (not an array, function, etc.).
    *   Rationale: Useful for distinguishing objects from other complex types.
    *   Symbol: `k/is-object?`
*   **`k/is-array?`**: Checks if a value is an array.
    *   Rationale: Completes the set of basic type predicates for collections.
    *   Symbol: `k/is-array?`

#### 2. Object/Map Manipulation

*   **`k/obj-has-key?`**: Checks if an object has a specific key (like `Object.prototype.hasOwnProperty.call`).
    *   Rationale: A common check, complements `k/get-key`.
    *   Symbol: `k/obj-has-key?`
*   **`k/obj-merge`**: Merges multiple objects into a new one (shallow merge).
    *   Rationale: `k/obj-assign` mutates, a non-mutating merge is often needed.
    *   Symbol: `k/obj-merge`
*   **`k/obj-deep-clone`**: Performs a deep clone of an object.
    *   Rationale: `k/obj-clone` is shallow, deep cloning is frequently required.
    *   Symbol: `k/obj-deep-clone`

#### 3. Array/List Manipulation

*   **`k/arr-first-index`**: Returns the index of the first element that satisfies a predicate.
    *   Rationale: `k/arr-find-index` exists, but a simpler `first-index` is common.
    *   Symbol: `k/arr-first-index`
*   **`k/arr-last-index`**: Returns the index of the last element that satisfies a predicate.
    *   Rationale: Complements `k/arr-first-index`.
    *   Symbol: `k/arr-last-index`
*   **`k/arr-flatten`**: Flattens a nested array up to a specified depth.
    *   Rationale: Common array transformation.
    *   Symbol: `k/arr-flatten`
*   **`k/arr-zip`**: Creates an array of grouped elements, the Nth element of which contains the Nth element from each of the input arrays.
    *   Rationale: Useful for combining related data from multiple arrays.
    *   Symbol: `k/arr-zip`

#### 4. String Manipulation

*   **`k/str-contains?`**: Checks if a string contains another substring.
    *   Rationale: A very common string predicate.
    *   Symbol: `k/str-contains?`
*   **`k/str-index-of`**: Returns the index within the calling `String` object of the first occurrence of the specified value.
    *   Rationale: Standard string method.
    *   Symbol: `k/str-index-of`
*   **`k/str-last-index-of`**: Returns the index within the calling `String` object of the last occurrence of the specified value.
    *   Rationale: Complements `k/str-index-of`.
    *   Symbol: `k/str-last-index-of`
*   **`k/str-repeat`**: Constructs and returns a new string which contains the specified number of copies of the string on which it was called, concatenated together.
    *   Rationale: Useful for generating repetitive strings.
    *   Symbol: `k/str-repeat`

#### 5. Math & Sorting

*   **`k/math-clamp`**: Clamps a number between an upper and lower bound.
    *   Rationale: Common utility for constraining values.
    *   Symbol: `k/math-clamp`
*   **`k/math-lerp`**: Linear interpolation between two numbers.
    *   Rationale: Useful in graphics and animation.
    *   Symbol: `k/math-lerp`
*   **`k/sort-desc`**: Sorts an array in descending order.
    *   Rationale: Complements `k/sort` (ascending).
    *   Symbol: `k/sort-desc`
*   **`k/sort-by-desc`**: Sorts an array in descending order based on a key function.
    *   Rationale: Complements `k/sort-by`.
    *   Symbol: `k/sort-by-desc`
