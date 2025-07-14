(ns documentation.std-lib-stream
  (:use code.test)
  (:require [std.lib.stream :as s]))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [xyz.zcaudate/std.lib \"{{PROJECT.version}}\"]"


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


[[:chapter {:title "Summary and Examples"}]]

"The `std.lib.stream` library provides a powerful and flexible way to process data by composing transformations into pipelines. It extends Clojure's sequence and transducer capabilities, allowing you to define how data flows from various sources to different types of sinks."

     
[[:chapter {:title "core api" :link "std.lib.stream"}]]

[[:api {:namespace "std.lib.stream"}]]

[[:chapter {:title "xform api" :link "std.lib.stream.xform"}]]

[[:api {:namespace "std.lib.stream.xform"}]] 
