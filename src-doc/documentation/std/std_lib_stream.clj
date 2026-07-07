(ns documentation.std-lib-stream
  (:require [std.lib.stream :as s])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Installation"}]]

"Add `[xyz.zcaudate/std.lib \"4.1.5\"]` to `project.clj` dependencies."


[[:section {:title "Top Level"}]]

"
The top level functionality in `std.lib.stream` are:

*   **`produce`**: Converts different data types (like ranges, lists, or even functions) into a uniform sequence that can be processed.
*   **`collect`**: Gathers the processed data into a desired output format, such as a vector, list, or a Java collection.
*   **`pipeline`**: Creates a composite transducer from a series of individual transformation steps, enabling complex data manipulation.
*   **`pipe`**: Connects a data source, a transformation pipeline, and a data sink to execute an entire stream operation.
*   **`producer`**: Creates a lazy sequence from a source and a pipeline, allowing on-demand data generation.
*   **`collector`**: Returns a function that, when given a source, collects the processed data into a specified sink after applying a pipeline.
*   **`stream` / `*>`**: A versatile function/macro that intelligently acts as a `pipeline`, `producer`, `collector`, or `pipe` based on the arguments provided, simplifying stream construction.
"

[[:section {:title "Produce"}]]

"The `produce` function in `std.lib.stream` is designed to convert various data structures into a
sequence, making them suitable for stream processing. Here are some examples of how it's used with different
input types:"

;; **1. Producing from a Range (Clojure Sequence)**

(require '[std.lib.stream :as s])


(fact "Producing from a range"
  (s/produce (range 5))
  => '(0 1 2 3 4))

;; **2. Producing from an Object (Single Element)**

;; If a function returns a single value:

(require '[std.lib.stream :as s])
(s/produce (fn [] 1))

;; If a function returns a sequence:


[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Producing and collecting"}]]

"`produce` turns different sources into a uniform sequence. `collect` consumes a sequence into a sink such as a vector, list, or Java collection."

(fact "produce a sequence from different sources"
  (s/produce (range 5))
  => '(0 1 2 3 4)

  (s/produce [1 2 3])
  => [1 2 3]

  (s/produce (fn [] 1))
  => '(1))

(fact "collect into various sinks"
  (s/collect [] (range 5))
  => [0 1 2 3 4]

  (s/collect () (range 5))
  => '(4 3 2 1 0))

[[:section {:title "Pipelines and pipes"}]]

"A pipeline is a composition of named transform steps. `pipe` wires a source, pipeline, and sink together in one call."

(fact "build and run a pipeline"
  (s/pipe (range 5)
          [[:map inc]
           [:map inc]]
          [])
  => [2 3 4 5 6])

(fact "filter and take elements"
  (s/pipe (range 20)
          [[:filter even?]
           [:take 5]
           [:map #(/ % 2)]]
          [])
  => [0 1 2 3 4])

[[:section {:title "Producers and collectors"}]]

"`producer` returns a lazy sequence; `collector` returns a reusable function. Combine them to build reusable stream stages."

(fact "create a lazy producer"
  (take 3 (s/producer (range 100)
                      [[:map inc]
                       [:filter odd?]]))
  => '(3 5 7))

(fact "create a reusable collector"
  (let [collect-odds (s/collector [[:filter odd?]] [])]
    (collect-odds (range 10))
    => [1 3 5 7 9]

    (collect-odds [[:map inc]] (range 5))
    => [2 4]))

[[:section {:title "The stream shorthand"}]]

"`stream` (and the `*>` macro) chooses between pipe, producer, collector, and pipeline based on which connectors are supplied. Use `<*>` for a missing connector."

(fact "use stream as a pipe"
  (s/stream (range 5)
            [:map inc]
            [])
  => [1 2 3 4 5])

(fact "use stream as a producer"
  (s/stream (range 5)
            [:map inc]
            (s/<*>))
  => seq?)

(fact "use stream as a collector"
  (let [collect+1 (s/stream (s/<*>)
                            [:map inc])]
    (collect+1 (range 5))
    => [1 2 3 4 5]))

(fact "use the *> macro"
  (s/*> (range 5)
        (s/produce (range 1 6))
        [])
  => [1 2 3 4 5])

[[:section {:title "End-to-end: build a reusable data pipeline"}]]

"A complete workflow: produce from a source, filter, transform, partition, and collect into a sink."

(fact "filter even numbers, pair them, and collect"
  (-> (s/pipe (range 10)
              [[:filter even?]
               [:partition-all 2]
               [:map vec]]
              [])
      (conj [8 9]))
  => [[0 2] [4 6] [8]])

[[:chapter {:title "Summary and Examples"}]]

"The `std.lib.stream` library provides a powerful and flexible way to process data by composing transformations into pipelines. It extends Clojure's sequence and transducer capabilities, allowing you to define how data flows from various sources to different types of sinks."

     
[[:chapter {:title "core api" :link "std.lib.stream"}]]

[[:api {:namespace "std.lib.stream"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_stream_summary.md
;; sha256: 2d74bf1da9e32e5e92991e3d8434f876e592edeee10b5aab3feca9644b2a25ef
[[:chapter {:title "std.lib.stream: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-stream-summary-md"}]]

"The `std.lib.stream` module provides a powerful and flexible framework for processing sequences of data using transducers. It abstracts over various data sources (producers) and sinks (collectors), allowing for the construction of complex data pipelines with a unified API. The module extends Clojure's transducer capabilities with a rich set of custom transducers (`xform` namespace) and provides seamless integration with Java Streams and other collection types."

"The module is organized into two main sub-namespaces:"

[[:section {:title "std.lib.stream.xform" :link "merged-plans-slop-summary-std-lib-stream-summary-md-std-lib-stream-xform"}]]

"This namespace provides a comprehensive collection of transducers (transforming functions) that can be used in data processing pipelines. These transducers extend and complement Clojure's built-in transducers, offering functionalities for mapping, filtering, reducing, statistical analysis, and windowing."

"*   **`x:map [& [f]]`**: A transducer that applies a function `f` to each element.\n*   **`x:map-indexed [f]`**: A transducer that applies a function `f` to `[index element]` pairs.\n*   **`x:filter [& [pred]]`**: A transducer that retains elements for which `pred` returns true.\n*   **`x:remove [& [f]]`**: A transducer that removes elements for which `f` returns true.\n*   **`x:keep [& [f]]`**: A transducer that retains non-nil results of applying `f` to each element.\n*   **`x:keep-indexed [f]`**: A transducer that retains non-nil results of applying `f` to `[index element]` pairs.\n*   **`x:prn [& [f]]`**: A transducer that prints each element (or `(f element)`) as a side effect.\n*   **`x:peek [& [f]]`**: A transducer that applies `f` to each element as a side effect, returning the original element.\n*   **`x:delay [ms]`**: A transducer that introduces a delay (in milliseconds) after processing each element.\n*   **`x:mapcat [& [f]]`**: A transducer that maps a function `f` over elements and concatenates the results.\n*   **`x:pass [& [init]]`**: An identity transducer.\n*   **`x:apply [rf]`**: A transducer that applies a reduction function `rf` to the accumulated result.\n*   **`x:reduce [f & [init]]`**: A transducer that accumulates results using a reduction function `f`.\n*   **`x:take [& [n]]`**: A transducer that takes up to `n` elements.\n*   **`x:take-last [n]`**: A transducer that takes the last `n` elements.\n*   **`x:drop [& [n]]`**: A transducer that drops `n` elements from the beginning.\n*   **`x:drop-last [n]`**: A transducer that drops the last `n` elements.\n*   **`x:butlast []`**: A transducer that drops the last element.\n*   **`x:some [& [f]]`**: A transducer that returns the first non-nil result of applying `f` to elements, or `nil`.\n*   **`x:last []`**: A transducer that returns the last element.\n*   **`x:count []`**: A transducer that counts the number of elements.\n*   **`x:min [& [comparator]]`**: A transducer that finds the minimum element.\n*   **`x:max [& [comparator]]`**: A transducer that finds the maximum element.\n*   **`x:mean []`**: A transducer that calculates the mean of numeric elements.\n*   **`x:stdev []`**: A transducer that calculates the standard deviation of numeric elements.\n*   **`x:str []`**: A transducer that concatenates elements into a single string.\n*   **`x:sort [& [cmp]]`**: A transducer that sorts all elements.\n*   **`x:sort-by [xf & [cmp]]`**: A transducer that sorts elements based on a key function `xf`.\n*   **`x:reductions [f & [init]]`**: A transducer that produces a sequence of intermediate reduction results.\n*   **`x:wrap [open close]`**: A transducer that wraps the sequence with `open` and `close` elements.\n*   **`x:time [tag-or-f xform]`**: A transducer that measures the time taken to process elements, printing the result.\n*   **`x:window [n & [f invf]]`**: A transducer that produces a sliding window of `n` elements."

[[:section {:title "std.lib.stream (Main Namespace)" :link "merged-plans-slop-summary-std-lib-stream-summary-md-std-lib-stream-main-namespace"}]]

"This namespace provides the core API for constructing and executing data pipelines using transducers, integrating various data sources and sinks."

"*   **`produce [obj]`**: A generic function (multimethod via `protocol.stream/-produce`) that converts an object into a sequence (producer).\n*   **`collect [sink xf supply]`**: A generic function (multimethod via `protocol.stream/-collect`) that consumes a sequence `supply` (optionally transformed by `xf`) into a `sink` (collector).\n*   **`seqiter [& [xform] coll & colls]`**: Creates a non-chunking iterator from a transducer and a collection, useful for fine-grained control over iteration.\n*   **`unit [xform input]`**: Applies a transducer `xform` to a single `input`.\n*   **`extend-stream-form [type-map]`**: Helper for `extend-stream` macro.\n*   **`extend-stream [all]`**: A macro to extend `std.protocol.stream/ISource` and `std.protocol.stream/ISink` to various types based on a map of type-to-produce/collect functions.\n*   **`*transforms*`**: An atom holding a map of all available transducers (from `std.lib.stream.xform`) keyed by their symbolic names.\n*   **`add-transforms [key xform & more]`**: Adds custom transducers to `*transforms*`.\n*   **`pipeline-transform [stages]`**: Converts a list of stage definitions (e.g., `[:map inc]`) into a list of actual transducer functions.\n*   **`pipeline [stages]`**: Creates a composite transducer (pipeline) from a list of stage definitions.\n*   **`pipe [source stages sink]`**: The core function for executing a stream. It takes a `source`, a list of `stages` (transducers), and a `sink`, piping data from source through stages to sink.\n*   **`producer [source stages]`**: Creates a sequence producer from a `source` and a list of `stages`.\n*   **`collector [stages sink]`**: Creates a collection function (sink) from a list of `stages` and a `sink`.\n*   **`<*>`**: A keyword (`:stream/<*>`) used as a placeholder to denote a missing source or sink in `stream` operations.\n*   **`stream [source & args]`**: A versatile function that constructs and executes stream operations. It can act as a `pipeline`, `producer`, `collector`, or `pipe` depending on whether `<*>` is used for source or sink.\n*   **`*> [& args]`**: A shortcut macro for `stream`.\n*   **Producer/Collector Implementations**:\n    *   **`produce-nil`, `produce-object`, `produce-ifn`, `produce-callable`, `produce-supplier`**: Functions to produce sequences from `nil`, generic objects, functions, `Callable`s, and `Supplier`s.\n    *   **`collect-nil`, `collect-ifn`**: Functions to collect sequences into `nil` or by applying a function.\n    *   **`+stream:fn+`**: A map defining produce/collect implementations for various function-like types.\n    *   **`+stream:primitive+`**: A map defining produce/collect implementations for Java primitive arrays.\n    *   **`primitive-form [type-map]`**: Helper for `gen:primitives`.\n    *   **`gen:primitives []`**: A macro to generate produce/collect functions for primitive arrays.\n    *   **`collect-transient`, `collect-collection`**: Functions to collect sequences into `ITransientCollection`s and `java.util.Collection`s.\n    *   **`+stream:java+`, `+stream:clojure+`**: Maps defining produce/collect implementations for various Java and Clojure collection types.\n    *   **`to-stream [arr]`**: Converts a Clojure sequence to a `java.util.stream.Stream`.\n    *   **`produce-stream [source]`**: Produces a sequence from a `java.util.stream.Stream`.\n    *   **`collect-collector [sink xf supply]`**: Collects a sequence into a `java.util.stream.Collector`.\n    *   **`+stream:base+`**: Map defining produce/collect implementations for Java Streams and Collectors.\n    *   **`produce-deref [source]`**: Produces a sequence by dereferencing a source.\n    *   **`collect-promise [sink xf supply]`**: Collects a sequence into a `Promise`.\n    *   **`+stream:promise+`, `+stream:future+`**: Maps defining produce/collect implementations for `Promise`s and `Future`s.\n*   **Sources**:\n    *   **`atom-seq [atm f]`**: Constructs a lazy sequence by repeatedly updating and yielding values from an atom.\n    *   **`object-seq [obj f]`**: Constructs a lazy sequence by repeatedly applying a function `f` to an object and yielding the result."

"**Overall Importance:**"

"The `std.lib.stream` module is a fundamental component for data processing and transformation within the `foundation-base` project. Its key contributions include:"

"*   **Unified Data Processing API:** Provides a consistent and flexible way to define and execute data pipelines, regardless of the underlying data source or sink.\n*   **Transducer-Based Efficiency:** Leverages Clojure's transducers for efficient, composable, and lazy data transformations, minimizing intermediate allocations.\n*   **Extensibility:** The protocol-driven design allows for easy integration of new data sources, sinks, and custom transducers.\n*   **Interoperability:** Seamlessly integrates with various Java and Clojure collection types, as well as Java Streams, `Callable`, `Supplier`, `Future`, and `Promise`.\n*   **Rich Set of Transformations:** The `xform` namespace provides a comprehensive library of transducers for common data manipulation, aggregation, and statistical analysis tasks.\n*   **Declarative Pipeline Construction:** The `pipeline` and `stream` functions allow for declarative definition of data processing workflows, improving readability and maintainability."

"By offering these powerful streaming and data transformation capabilities, `std.lib.stream` significantly enhances the `foundation-base` project's ability to handle and process diverse data efficiently and flexibly, which is vital for its multi-language development ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_stream_summary.md
