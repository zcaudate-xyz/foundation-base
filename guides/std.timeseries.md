# `std.timeseries` Guide

`std.timeseries` is a library for handling time-series data using a "Journal" abstraction. It supports ingestion, storage, retrieval, downsampling, and aggregation of time-ordered records.

## Core Concepts

- **Journal**: The primary data structure. It manages a sorted list of entries and associated metadata.
- **Entry**: A map representing a data point. Must have a time key (default `:s/time`).
- **Processing**: The engine for selecting, aggregating, and transforming data ranges.

## Usage

### Scenarios

#### 1. Real-time Metric Collection

**Scenario: Ingesting sensor data.**

Create a journal configured for high-frequency data (e.g., milliseconds).

```clojure
(require '[std.timeseries :as ts])

(def sensor-journal
  (ts/journal {:meta {:time {:unit :ms :key :timestamp}
                      :entry {:flatten true} ;; Save space by flattening nested maps
                      }}))

(defn on-sensor-read [data]
  ;; data: {:temp 20.5 :humidity 50 :timestamp 1600000000000}
  (alter-var-root #'sensor-journal ts/add-entry data))
```

#### 2. Downsampling for Visualization

**Scenario: Fetching a 24-hour chart with 100 data points.**

You have raw data every second, but you only want 100 points for a graph.

```clojure
(ts/select sensor-journal
           {:range [:24h :now] ;; Or specific timestamps
            :sample 100        ;; Target count
            :transform {:default {:aggregate :mean} ;; Average values in each bucket
                        :temp {:aggregate :max}}    ;; But show max temp
            })
```

#### 3. Merging Disparate Sources

**Scenario: Combining logs from two servers.**

You have two journals with potentially overlapping or interleaved time periods.

```clojure
(def combined-journal (ts/merge journal-a journal-b))

;; The merge respects time ordering.
;; Pre-requisite: Journals must share the same metadata structure.
```

#### 4. Handling Irregular Intervals

**Scenario: Data arrives sporadically, but you need a regular 1-second interval output.**

Use `derive` to create a normalized view.

```clojure
(ts/derive raw-journal
           {:range :all
            :transform {:interval :1s  ;; Force 1s buckets
                        :default {:aggregate :last ;; Use last known value
                                  :fill :previous} ;; Fill gaps with previous value
                        }})
```

#### 5. Complex Window Analysis

**Scenario: Moving average.**

While `std.timeseries` focuses on storage and retrieval, you can compute derived series during selection.

```clojure
(ts/select my-journal
           {:range :1h
            :compute {:moving-avg (fn [entries] ...)} ;; Custom computation
            })
```

#### 6. Efficient Storage with Templates

**Scenario: Storing repetitive map structures.**

If every entry looks like `{:a 1 :b {:c 2}}`, the journal can learn a "template" to store them as flat vectors internally, saving memory.

```clojure
(def j (ts/journal {:meta {:entry {:flatten true}}}))
;; The first entry added determines the template structure.
(ts/add-entry j {:a 1 :b 2})
;; Internally stored as something like [1 2] + template reference.
```
