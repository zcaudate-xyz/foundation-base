# `std.lib.foundation` Recommendations

`std.lib.foundation` provides the most basic utility functions for the entire `foundation-base` ecosystem. Its goal is to be a lightweight, universal "prelude" for all other modules. Here are some recommendations for new functionality that would align with this goal and further enhance the developer experience.

*   **More String Utilities:**
    *   **Justification:** String manipulation is a very common task. While `std.string.common` exists, `std.lib.foundation` would benefit from having a few core, frequently used string functions directly available. This would reduce the need for other modules to import `std.string.common`, simplifying dependencies and improving code readability.
    *   **Recommendations:**
        *   `trim`: Removes leading and trailing whitespace from a string.
        *   `split`: Splits a string into a sequence of strings based on a delimiter.
        *   `join`: Joins a sequence of strings into a single string with a separator.
        *   `replace`: Replaces all occurrences of a substring or pattern with another string.
        *   `starts-with?`, `ends-with?`: Checks if a string starts or ends with a given prefix or suffix.
    *   **Example Usage:**
        ```clojure
        (-> "  hello world  " h/trim (h/split #" ") (h/join "-"))
        ;; => "hello-world"
        ```

*   **Enhanced Type Checking:**
    *   **Justification:** The codebase already has predicates like `string?`, `keyword?`, and `symbol?`. Expanding this set would provide a more complete and consistent way to perform type checks, which is especially useful for validation and multi-method dispatch.
    *   **Recommendations:**
        *   `mapp?`: Checks if an object is a map.
        *   `vector?`: Checks if an object is a vector.
        *   `set?`: Checks if an object is a set.
        *   `fn?`: Checks if an object is a function.
        *   `coll?`: Checks if an object is a collection (list, vector, set, or map).
    *   **Example Usage:**
        ```clojure
        (when (h/mapp? my-data)
          (h/map-vals process-value my-data))
        ```

*   **More Math Functions:**
    *   **Justification:** While the project has a `math` namespace, some very basic math functions are so common that they would be a good fit for `std.lib.foundation`.
    *   **Recommendations:**
        *   `abs`: Returns the absolute value of a number.
        *   `min`, `max`: Returns the minimum or maximum of a set of numbers.
        *   `round`, `floor`, `ceil`: For rounding numbers.
    *   **Example Usage:**
        ```clojure
        (h/round 3.14159) ;; => 3
        (h/max 1 5 2 8 3) ;; => 8
        ```

*   **`identity` function:**
    *   **Justification:** The `identity` function is a fundamental building block in functional programming. It's often used as a default function or in higher-order functions. Its absence is a noticeable omission.
    *   **Recommendation:** Add `(defn identity [x] x)`.
    *   **Example Usage:**
        ```clojure
        (get-in my-map [:a :b] :default-value)
        ;; vs
        (-> (get-in my-map [:a :b]) (or :default-value))
        ;; with identity
        (-> (get-in my-map [:a :b]) (h/call identity) (or :default-value))
        ```

*   **`constantly` function:**
    *   **Justification:** `constantly` is another useful higher-order function that creates a function that always returns the same value. It's great for stubbing out functions or for use with functions like `map`.
    *   **Recommendation:** Add `(defn constantly [x] (fn [& args] x))`.
    *   **Example Usage:**
        ```clojure
        (map (h/constantly 0) (range 5)) ;; => (0 0 0 0 0)
        ```

*   **`juxt` function:**
    *   **Justification:** `juxt` is a powerful function for applying multiple functions to the same arguments and collecting the results. It can simplify code and make it more expressive.
    *   **Recommendation:** Add a `juxt` implementation.
    *   **Example Usage:**
        ```clojure
        (let [stats (h/juxt min max count)]
          (stats [1 5 2 8 3])) ;; => [1 8 5]
        ```
