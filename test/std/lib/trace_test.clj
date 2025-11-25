(ns std.lib.trace-test
  (:use code.test)
  (:require [std.lib.trace :refer :all]))

^{:refer std.lib.trace/trace? :added "4.0"}
(fact "checks if object is a trace"
  (resolve 'trace?) => var?)

^{:refer std.lib.trace/get-trace :added "4.0"}
(fact "gets a trace from var"
  (resolve 'get-trace) => var?)

^{:refer std.lib.trace/make-trace :added "4.0"}
(fact "makes a trace from var"

  (make-trace #'get-trace :basic)
  => trace?)

^{:refer std.lib.trace/has-trace? :added "4.0"}
(fact "checks if var has trace"
  (resolve 'has-trace?) => var?)

^{:refer std.lib.trace/apply-trace :added "4.0"}
(fact "applies a trace with arguments"
  
  (apply-trace identity
               (make-trace #'get-trace :basic)
               identity
               [1])
  => 1)



^{:refer std.lib.trace/wrap-basic :added "4.0"}
(fact "wraps an identity transform"
  (resolve 'wrap-basic) => var?)

^{:refer std.lib.trace/wrap-print :added "4.0"}
(fact "wraps a print transform")

^{:refer std.lib.trace/wrap-stack :added "4.0"}
(fact "wraps a stack transform")

^{:refer std.lib.trace/add-raw-trace :added "4.0"}
(fact "parent function for adding traces")

^{:refer std.lib.trace/add-base-trace :added "4.0"}
(fact "adds a base trace")

^{:refer std.lib.trace/add-print-trace :added "4.0"}
(fact "adds a print trace")

^{:refer std.lib.trace/add-stack-trace :added "4.0"}
(fact "adds a stack trace")

^{:refer std.lib.trace/remove-trace :added "4.0"}
(fact "removes a trace from var")

^{:refer std.lib.trace/trace-ns :added "4.0"}
(fact "adds a trace to entire namespace")

^{:refer std.lib.trace/trace-print-ns :added "4.0"}
(fact "adds a print trace to entire namespace")

^{:refer std.lib.trace/untrace-ns :added "4.0"}
(fact "removes traces for entire namespace")

^{:refer std.lib.trace/output-trace :added "4.0"}
(fact "outputs a trace form")
