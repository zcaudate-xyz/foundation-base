(ns documentation.std-timeseries
  (:use code.test))

[[:hero {:title "std.timeseries"
         :subtitle "journals, intervals, ranges, computation, and processing"
         :lead "`std.timeseries` is a higher-level standard library family in foundation-base. This page explains when to use it, how it fits internally, and where to find the API surface."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Use this layer when application or tooling code needs the behavior described by the page title without reaching directly into implementation namespaces. The top-level namespace is the starting point; subnamespaces expose more focused building blocks."

[[:chapter {:title "How to use it" :link "usage"}]]

"Require the top-level namespace for common workflows, then move to subnamespaces when you need a lower-level primitive. Existing tests under `test/std/timeseries` and `test/std/timeseries_test.clj` are the best executable examples for edge cases."

(comment
  (require '[std.timeseries :as lib]))

[[:chapter {:title "Internal usage" :link "internal"}]]

"This library family is used across source, tests, generated examples, and docs tooling. During detailed documentation passes, collect concrete usage with `code.manage/find-usages` and `code.manage/locate-code`, then keep only high-signal examples in the page narrative."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.timeseries"}]]
[[:api {:namespace "std.timeseries.common"}]]
[[:api {:namespace "std.timeseries.compute"}]]
[[:api {:namespace "std.timeseries.journal"}]]
[[:api {:namespace "std.timeseries.range"}]]

;; BEGIN merged documentation: guides/std.timeseries.md
;; sha256: 9aa3c020e7da282c7b391e4fe689b5f95466a0a22daf0a6a1a1ed25aacdbc57f
[[:chapter {:title "std.timeseries Guide" :link "merged-guides-std-timeseries-md"}]]

"`std.timeseries` is a library for handling time-series data using a \"Journal\" abstraction. It supports ingestion, storage, retrieval, downsampling, and aggregation of time-ordered records."

[[:section {:title "Core Concepts" :link "merged-guides-std-timeseries-md-core-concepts"}]]

"- **Journal**: The primary data structure. It manages a sorted list of entries and associated metadata.\n- **Entry**: A map representing a data point. Must have a time key (default `:s/time`).\n- **Processing**: The engine for selecting, aggregating, and transforming data ranges."

[[:section {:title "Usage" :link "merged-guides-std-timeseries-md-usage"}]]

[[:subsection {:title "Scenarios" :link "merged-guides-std-timeseries-md-scenarios"}]]

[[:subsubsection {:title "1. Real-time Metric Collection" :link "merged-guides-std-timeseries-md-1-real-time-metric-collection"}]]

"**Scenario: Ingesting sensor data.**"

"Create a journal configured for high-frequency data (e.g., milliseconds)."

[[:code {:lang "clojure"} "(require '[std.timeseries :as ts])\n\n(def sensor-journal\n  (ts/journal {:meta {:time {:unit :ms :key :timestamp}\n                      :entry {:flatten true} ;; Save space by flattening nested maps\n                      }}))\n\n(defn on-sensor-read [data]\n  ;; data: {:temp 20.5 :humidity 50 :timestamp 1600000000000}\n  (alter-var-root #'sensor-journal ts/add-entry data))"]]

[[:subsubsection {:title "2. Downsampling for Visualization" :link "merged-guides-std-timeseries-md-2-downsampling-for-visualization"}]]

"**Scenario: Fetching a 24-hour chart with 100 data points.**"

"You have raw data every second, but you only want 100 points for a graph."

[[:code {:lang "clojure"} "(ts/select sensor-journal\n           {:range [:24h :now] ;; Or specific timestamps\n            :sample 100        ;; Target count\n            :transform {:default {:aggregate :mean} ;; Average values in each bucket\n                        :temp {:aggregate :max}}    ;; But show max temp\n            })"]]

[[:subsubsection {:title "3. Merging Disparate Sources" :link "merged-guides-std-timeseries-md-3-merging-disparate-sources"}]]

"**Scenario: Combining logs from two servers.**"

"You have two journals with potentially overlapping or interleaved time periods."

[[:code {:lang "clojure"} "(def combined-journal (ts/merge journal-a journal-b))\n\n;; The merge respects time ordering.\n;; Pre-requisite: Journals must share the same metadata structure."]]

[[:subsubsection {:title "4. Handling Irregular Intervals" :link "merged-guides-std-timeseries-md-4-handling-irregular-intervals"}]]

"**Scenario: Data arrives sporadically, but you need a regular 1-second interval output.**"

"Use `derive` to create a normalized view."

[[:code {:lang "clojure"} "(ts/derive raw-journal\n           {:range :all\n            :transform {:interval :1s  ;; Force 1s buckets\n                        :default {:aggregate :last ;; Use last known value\n                                  :fill :previous} ;; Fill gaps with previous value\n                        }})"]]

[[:subsubsection {:title "5. Complex Window Analysis" :link "merged-guides-std-timeseries-md-5-complex-window-analysis"}]]

"**Scenario: Moving average.**"

"While `std.timeseries` focuses on storage and retrieval, you can compute derived series during selection."

[[:code {:lang "clojure"} "(ts/select my-journal\n           {:range :1h\n            :compute {:moving-avg (fn [entries] ...)} ;; Custom computation\n            })"]]

[[:subsubsection {:title "6. Efficient Storage with Templates" :link "merged-guides-std-timeseries-md-6-efficient-storage-with-templates"}]]

"**Scenario: Storing repetitive map structures.**"

"If every entry looks like `{:a 1 :b {:c 2}}`, the journal can learn a \"template\" to store them as flat vectors internally, saving memory."

[[:code {:lang "clojure"} "(def j (ts/journal {:meta {:entry {:flatten true}}}))\n;; The first entry added determines the template structure.\n(ts/add-entry j {:a 1 :b 2})\n;; Internally stored as something like [1 2] + template reference."]]
;; END merged documentation: guides/std.timeseries.md

;; BEGIN merged documentation: plans/slop/summary/std_timeseries_summary.md
;; sha256: 7c50ab7e9cd7f119a80898b941634977c0d57e651308ad4be1f64fc47c2ef12c
[[:chapter {:title "std.timeseries: A Comprehensive Summary" :link "merged-plans-slop-summary-std-timeseries-summary-md"}]]

"The `std.timeseries` module in `foundation-base` provides a robust framework for managing, processing, and analyzing time-series data. It offers functionalities for data aggregation, sampling, transformation, and journaling, making it suitable for applications requiring historical data tracking, performance monitoring, or event logging. The module is designed to be flexible, allowing for custom processing pipelines and integration with various data representations."

"The module is organized into several sub-namespaces:"

[[:section {:title "std.timeseries.common" :link "merged-plans-slop-summary-std-timeseries-summary-md-std-timeseries-common"}]]

"This namespace provides fundamental utilities and helper functions for time-series operations, including data manipulation, template creation, and order handling."

"*   **`linspace [arr n]`**: Generates `n` linearly spaced samples from an array.\n*   **`cluster [arr n f]`**: Clusters an array into `n` groups and applies an aggregate function `f` to each cluster.\n*   **`make-empty [entry]`**: Creates an empty entry (map) with default zero/empty values based on the structure of a sample entry.\n*   **`raw-template [entry]`**: Creates a template for nested-to-flat and flat-to-nested map transformations, including type information and empty values.\n*   **`flat-fn [template]`**: Creates a function to flatten a nested map into a dot-separated key map based on a template.\n*   **`nest-fn [template]`**: Creates a function to nest a flat map into a hierarchical map based on a template.\n*   **`create-template [entry]`**: Creates a comprehensive template for an entry, including raw template, flat, and nest transformation functions.\n*   **`order-flip [order]`**: Flips the order (`:asc` to `:desc`, or vice-versa).\n*   **`order-fn [[asc desc]]`**: Creates a function that returns an order-specific value or flips it.\n*   **`order-comp`, `order-comp-eq`, `order-op`**: Pre-defined order functions for comparison and operations.\n*   **`order-fns [order & [flip]]`**: Returns a map of commonly used order-specific functions (`:comp-fn`, `:comp-eq-fn`, `:op-fn`).\n*   **`+default+`**: A map of default options for time-series processing, including time key, unit, order, interval, and sample.\n*   **`from-ms [ms to]`**: Converts milliseconds to a specified time unit (ns, us, ms, s, m, h, d).\n*   **`to-ms [ms from]`**: Converts a time value from a specified unit to milliseconds.\n*   **`from-ns [ns to]`**: Converts nanoseconds to a specified time unit.\n*   **`duration [arr opts]`**: Calculates the duration between the first and last elements in an array based on time options.\n*   **`parse-time [s & [unit]]`**: Parses a string or keyword time representation (e.g., \":0.1m\") into milliseconds or a specified unit.\n*   **`parse-time-expr [m]`**: Parses a time expression map, converting interval strings/keywords to numeric values.\n*   **`sampling-fn`**: A multimethod for extensible sampling functions.\n*   **`sampling-parser`**: A multimethod for extensible sampling parser functions.\n*   **`parse-sample-expr [sample time-opts]`**: Parses a sample expression, converting various input formats into a standardized sample options map.\n*   **`process-sample [arr m time-opts]`**: Processes an array by applying a sampling function based on the provided sample options."

[[:section {:title "std.timeseries.journal" :link "merged-plans-slop-summary-std-timeseries-summary-md-std-timeseries-journal"}]]

"This namespace defines the `Journal` record and associated functions for managing a time-series journal, which acts as a chronological log of entries. It provides functionalities for adding, selecting, deriving, and merging journal entries."

"*   **`format-time [val opts]`**: Formats a time value according to a specified unit and format string.\n*   **`+defaults+`**: Default options for journal metadata, including time key, unit, order, entry flattening, head range, and select options.\n*   **`template-keys [template]`**: Extracts sorted keys from a template's flat representation.\n*   **`entry-display [entry meta]`**: Formats a journal entry for display, including time formatting.\n*   **`create-template [journal entry]`**: Creates and caches a template for journal entries.\n*   **`get-template [journal]`**: Retrieves an existing template or creates a new one if the journal has entries.\n*   **`entries-seq [journal]`**: Returns journal entries as a sequence, respecting the journal's limit and order.\n*   **`entries-vec [journal]`**: Returns journal entries as a vector, respecting the journal's limit and order.\n*   **`journal-entries [journal]`**: Returns journal entries in the specified time order.\n*   **`journal-info [journal]`**: Returns information about the journal, including count, order, duration, template keys, and head entries.\n*   **`journal-invoke [journal & args]`**: The invoke function for the `Journal` record, allowing it to be called directly for info or selection.\n*   **`Journal` Deftype**: The core record for a time-series journal, holding `id`, `meta`, `template`, `entries`, `limit`, and `previous` entries. It implements `clojure.lang.IDeref` and has a custom `toString` method.\n*   **`journal [& [m]]`**: Creates a new `Journal` instance, merging default metadata and ensuring proper initialization.\n*   **`add-time [entry key unit]`**: Adds a timestamp to an entry if it doesn't already exist.\n*   **`update-journal-single [journal entry]`**: Adds a single entry to the journal, managing the `limit` and `previous` entries.\n*   **`add-entry [journal entry]`**: Adds a single entry to the journal, handling flattening and timestamping.\n*   **`update-journal-bulk [journal new-entries]`**: Adds multiple entries to the journal, managing the `limit` and `previous` entries.\n*   **`add-bulk [journal entries]`**: Adds multiple entries to the journal, handling flattening and timestamping.\n*   **`update-meta [journal meta]`**: Updates the journal's metadata, typically for display purposes.\n*   **`select-series [entries series]`**: Selects data from a series of entries based on a keyword, vector, list, or map specification.\n*   **`select [journal & [params]]`**: Selects and processes data from the journal based on range, sample, transform, series, and compute parameters.\n*   **`derive [journal params]`**: Derives a new journal (or a modified version of the current one) by applying selection and transformation parameters.\n*   **`merge-sorted [coll & [key-fn comp-fn]]`**: Merges multiple sorted collections into a single sorted collection.\n*   **`merge [& journals]`**: Merges two or more journals of the same type, combining their entries while maintaining order."

[[:section {:title "std.timeseries.process" :link "merged-plans-slop-summary-std-timeseries-summary-md-std-timeseries-process"}]]

"This namespace provides the core processing pipeline for time-series data, including range selection, sampling, transformation (aggregation), and computation of indicators."

"*   **`prep-merge [m time-opts]`**: Prepares merge functions and options for aggregation, including resolving aggregate functions and parsing sample expressions.\n*   **`create-merge-fn [m time-opts]`**: Creates a merge function for aggregating data based on sample and aggregate options.\n*   **`create-custom-fns [custom time-opts]`**: Creates a map of custom merge functions for specific keys, as defined in the `custom` transform options.\n*   **`map-merge-fn [transform-opts time-opts]`**: Creates a merge function for map-based time-series data, handling default, time, and custom aggregations.\n*   **`time-merge-fn [transform-opts time-opts]`**: Creates a merge function specifically for time-based aggregations.\n*   **`parse-transform-expr [m type time-opts]`**: Parses a transform expression, converting interval strings/keywords to numeric values and creating the appropriate merge function.\n*   **`transform-interval [arr transform type time-opts]`**: Transforms an array based on a specified interval, grouping and merging data points.\n*   **`process-transform [arr opts]`**: Processes the transform stage of the time-series pipeline, applying interval-based transformations and flattening entries if configured.\n*   **`common/sampling-parser` extension**: Extends `:interval` strategy for sampling.\n*   **`common/sampling-fn` extension**: Extends `:interval` strategy for sampling.\n*   **`process-compute [arr opts]`**: Processes the compute stage, applying indicators and computations defined in the `compute` options.\n*   **`process [arr opts]`**: The main function for processing time series. It orchestrates the entire pipeline: parsing time options, processing ranges, applying transformations, and computing indicators."

[[:section {:title "std.timeseries (Facade Namespace)" :link "merged-plans-slop-summary-std-timeseries-summary-md-std-timeseries-facade-namespace"}]]

"This namespace acts as a facade, re-exporting key functions from its sub-namespaces for convenience."

"*   **`create-template`** (from `common`)\n*   **`journal`, `add-bulk`, `add-entry`, `update-meta`, `derive`, `merge`, `select`** (from `journal`)\n*   **`process`** (from `process`)"

"**Overall Importance:**"

"The `std.timeseries` module is a powerful and flexible tool for handling time-series data within the `foundation-base` project. Its key contributions include:"

"*   **Unified Data Model:** Provides a consistent way to represent and manipulate time-series data, regardless of the underlying Java time types.\n*   **Flexible Processing Pipeline:** Supports a multi-stage processing pipeline (range, sample, transform, compute) that can be customized for various analytical needs.\n*   **Data Aggregation and Sampling:** Offers robust mechanisms for aggregating data over intervals and sampling data points, crucial for reducing noise and extracting insights.\n*   **Journaling and Historical Tracking:** The `Journal` component provides a structured way to log and retrieve time-series entries, enabling historical analysis and event tracking.\n*   **Extensibility:** The extensive use of protocols and multimethods allows for easy extension with new time representations, sampling strategies, aggregation functions, and computation indicators.\n*   **Interoperability:** The module's ability to coerce between different time representations facilitates integration with various Java and Clojure libraries."

"By providing a comprehensive and extensible framework for time-series data, `std.timeseries` significantly contributes to the `foundation-base` project's capabilities in areas such as performance monitoring, event analysis, and data visualization."
;; END merged documentation: plans/slop/summary/std_timeseries_summary.md
