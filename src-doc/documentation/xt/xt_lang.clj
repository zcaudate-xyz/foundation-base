(ns documentation.xt-lang
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-string :as xts]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.lang"
         :subtitle "Portable language primitives and common libraries."
         :lead "`xt.lang` defines reusable xtalk libraries that target JS, Lua, Python, Dart, and other runtimes through hara.lang emission."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Target languages differ in collection APIs, nil handling, string operations, promises, modules, and resource access. `xt.lang` gives generated programs one shared vocabulary for those behaviors."

[[:chapter {:title "How to use it" :link "usage"}]]

"A hara script requires the libraries it needs, then emitted xtalk code calls those portable helpers. Application examples in `src-build/play/*xtalk*` and tests under `test-lang/xt/lang` show this pattern."

(comment
  (l/script :xtalk
    {:require [[xt.lang.spec-base :as xt]
               [xt.lang.common-data :as data]
               [xt.lang.common-string :as string]]}))

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Portable iteration"}]]

"`xt.lang.spec-base` provides loop forms that expand to idiomatic loops on each target: `for:array`, `for:object`, and `for:index`."

(fact "iterate arrays and objects portably"
  ^{:refer xt.lang.spec-base/for:array :added "4.0"}
  (!.js
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  ^{:refer xt.lang.spec-base/for:object :added "4.0"}
  (!.js
    (var out [])
    (xt/for:object [[k v] {:a 1 :b 2}]
      (xt/x:arr-push out [k v]))
    out)
  => (just [["a" 1] ["b" 2]] :in-any-order))

[[:section {:title "Type predicates"}]]

"`xt.lang.common-lib` exposes portable type checks so emitted code behaves consistently even when target truthiness differs."

(fact "check object type"
  ^{:refer xt.lang.common-lib/is-object? :added "4.0"}
  (!.js
    [(k/is-object? {:a 1})
     (k/is-object? [1 2 3])])
  => [true false]

  ^{:refer xt.lang.common-lib/is-string? :added "4.0"}
  (!.js
    [(k/is-string? "hello")
     (k/is-string? 1)])
  => [true false]

  ^{:refer xt.lang.common-lib/is-array? :added "4.0"}
  (!.js
    [(k/is-array? [1 2 3])
     (k/is-array? {:a 1})])
  => [true false])

[[:section {:title "String helpers"}]]

"`xt.lang.common-string` provides portable split, join, replace, and symbol-path helpers."

(fact "split, join, and inspect symbol paths"
  ^{:refer xt.lang.common-string/split :added "4.0"}
  (!.js
    (xts/split "hello/world" "/"))
  => ["hello" "world"]

  ^{:refer xt.lang.common-string/join :added "4.0"}
  (!.js
    (xts/join "/" ["hello" "world"]))
  => "hello/world"

  ^{:refer xt.lang.common-string/sym-pair :added "4.0"}
  (!.js
    (xts/sym-pair "xt.lang/common-string"))
  => ["xt.lang" "common-string"])

[[:section {:title "End-to-end: a tiny word counter"}]]

"Combining loops and string helpers gives a portable word count that runs the same in every target runtime."

(fact "count words in a sentence"
  ^{:refer xt.lang.common-string/split :added "4.0"}
  (!.js
    (var words (xts/split "hello portable world" " "))
    (var count 0)
    (xt/for:array [w words]
      (:= count (+ count 1)))
    count)
  => 3)

[[:chapter {:title "Internal usage" :link "internal"}]]

"The common libraries are used by xt.db, xt.event, xt.net, xt.substrate, and by generated single-source examples. The `test-lang/xtbench` tests exercise cross-target parity for these helpers."

[[:chapter {:title "API" :link "api"}]]

;; BEGIN merged documentation: plans/slop/summary/xt_lang_base_lib_recommendations.md
;; sha256: 29af64a5a11c59ae464dcfc5e824451805abd6c00c9bc95c0c814e469c972bf0
[[:chapter {:title "xt.lang.base-lib Recommendations" :link "merged-plans-slop-summary-xt-lang-base-lib-recommendations-md"}]]

"`xt.lang.base-lib` provides a solid foundation for cross-language development. Here are some recommendations for new functionality that could make it even more useful:"

"*   **More Collection Functions:**\n    *   `map`, `filter`, `reduce`: These are fundamental higher-order functions for working with collections. Adding them to `xt.lang.base-lib` would make it much easier to write functional-style code in a cross-language way.\n    *   `sort`, `sort-by`: For sorting collections.\n    *   `group-by`: For grouping elements of a collection based on a key.\n*   **More String Functions:**\n    *   `trim`, `trim-left`, `trim-right`: For removing whitespace from strings.\n    *   `pad-left`, `pad-right`: For padding strings to a certain length.\n    *   `replace`: For replacing substrings.\n*   **Date/Time Functions:**\n    *   `now`: Returns the current time as a timestamp or a date object.\n    *   `format-date`: Formats a date object into a string.\n    *   `parse-date`: Parses a string into a date object.\n*   **URL Functions:**\n    *   `url-encode`, `url-decode`: For encoding and decoding URL components.\n    *   `parse-url`: For parsing a URL into its components (protocol, host, path, etc.).\n*   **Randomness:**\n    *   `rand-int`: Generates a random integer within a given range.\n    *   `rand-nth`: Returns a random element from a collection.\n    *   `shuffle`: Shuffles the elements of a collection."
;; END merged documentation: plans/slop/summary/xt_lang_base_lib_recommendations.md

;; BEGIN merged documentation: plans/slop/summary/xt_lang_summary.md
;; sha256: 0b0b89b92ec2e344541adca2cecf897ace96b0cc62e261337977ed7b740dd059
[[:chapter {:title "xt.lang and xtalk Summary" :link "merged-plans-slop-summary-xt-lang-summary-md"}]]

"The `xt.lang` namespace and the `xtalk` mechanism are at the heart of the `foundation-base` cross-language interoperability story. `xtalk` (cross-talk) is a system that allows code written in one language to call code written in another language, with automatic type conversion and marshalling."

"**Core Concepts:**"

"*   **`xtalk`:** `xtalk` is a protocol and a set of conventions for defining and calling functions across different languages.\n*   **`xt.lang.base-lib`:** This namespace provides a set of common utility functions that are designed to be used in a cross-language context. These functions have implementations in all the languages supported by `foundation-base`.\n*   **Language-Specific Implementations:** Each language supported by `foundation-base` has its own implementation of the `xtalk` protocol and the `xt.lang.base-lib` functions. For example, `xt.lang.model.spec-js` and `xt.lang.model.spec-lua` provide the JavaScript and Lua implementations, respectively."

"**How `xtalk` Works:**"

"1.  **Function Definition:** When you define a function using `defn.xt`, it is registered with the `xtalk` system.\n2.  **Function Call:** When you call an `xtalk` function from a different language, the `xtalk` system intercepts the call and performs the following steps:\n    *   **Type Conversion:** The arguments are converted from the source language's data types to a common `xtalk` representation.\n    *   **Function Invocation:** The target function is invoked with the converted arguments.\n    *   **Return Value Conversion:** The return value is converted from the target language's data type back to the source language's data type."

"**`xt.lang.base-lib`:**"

"The `xt.lang.base-lib` namespace provides a set of common utility functions that are essential for writing cross-language code. These functions include:"

"*   **Type Predicates:** `is-string?`, `is-number?`, `is-boolean?`, `is-function?`, `is-object?`, `is-array?`\n*   **Collection Functions:** `get-key`, `set-key`, `get-path`, `obj-keys`, `obj-vals`, `arr-clone`, `arr-slice`\n*   **String Functions:** `str-len`, `str-char`, `str-split`, `str-join`\n*   **Math Functions:** `m-abs`, `m-max`, `m-min`, `m-pow`"

"**Example: Using `xtalk`**"

^{:id merged-plans-slop-summary-xt-lang-summary-md-example-1 :added "4.0"}
(fact "xt.lang and xtalk Summary example"
  ;; In a Clojure file
  (ns my-clojure-ns
    (:require [xt.lang :as l]))

  (l/script :xtalk
    {:require [[xt.lang.base-lib :as k]]})

  (l/defn.xt my-clj-fn [s]
    (k/to-uppercase s))

  ;; In a JavaScript file
  (ns my-js-ns
    (:require [xt.lang :as l]))

  (l/script :js
    {:require [[my-clojure-ns :as clj]]})

  (l/defn.js my-js-fn []
    (console.log (clj/my-clj-fn "hello")))
)

"In this example, the `my-js-fn` function in JavaScript calls the `my-clj-fn` function in Clojure. The `xtalk` system automatically handles the conversion of the string argument and the return value between JavaScript and Clojure."

"`xtalk` is a powerful mechanism that makes it easy to write polyglot applications with `foundation-base`. By providing a common set of utility functions and a seamless way to call functions across languages, `xtalk` greatly simplifies the process of building complex, multi-language systems."
;; END merged documentation: plans/slop/summary/xt_lang_summary.md
