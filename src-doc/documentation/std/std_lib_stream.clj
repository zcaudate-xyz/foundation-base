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
