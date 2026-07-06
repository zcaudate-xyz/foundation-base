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
