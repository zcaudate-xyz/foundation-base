(ns code.test.base.print-test
  (:use code.test)
  (:require [code.test.base.print :refer :all]
            [std.string :as str]
            [std.lib :as h]
            [std.print :as print]))

^{:refer code.test.base.print/print-failure :added "3.0"}
(fact "outputs the description for a failed test")

^{:refer code.test.base.print/print-thrown :added "3.0"}
(fact "outputs the description for a form that throws an exception")

^{:refer code.test.base.print/pad-left :added "4.0"}
(fact "pads a string to the left"
  (#'code.test.base.print/pad-left 5 "abc")
  => "  abc")

^{:refer code.test.base.print/format-diff-map :added "4.0"}
(fact "formats a map diff"
  (str/includes? (format-diff-map {:+ {:a 1}} 2) "+")
  => true)

^{:refer code.test.base.print/format-diff-seq :added "4.0"}
(fact "formats a seq diff"
  (str/includes? (format-diff-seq [[:+ 0 1]] 2) "+")
  => true)

^{:refer code.test.base.print/format-diff :added "4.0"}
(fact "formats a diff"
  (str/includes? (format-diff {:+ {:a 1}}) "+")
  => true)

^{:refer code.test.base.print/print-preliminary :added "4.1"}
(fact "prints preliminary info"
  (str/includes? (print-preliminary "TITLE" :red {:path "path" :line 10 :desc "desc" :form '(+ 1 1)}) "TITLE")
  => true)

^{:refer code.test.base.print/print-success :added "3.0"}
(fact "outputs the description for a successful test")

^{:refer code.test.base.print/print-throw :added "4.1"}
(fact "prints throw info"
  (str/includes? (h/with-out-str
                   (print-throw {:name "test" :data (ex-info "error" {})}))
                 "THROW")
  => true)

^{:refer code.test.base.print/print-timeout :added "4.0"}
(fact "prints timeout info"
  (str/includes? (h/with-out-str
                   (print-timeout {:name "test" :data 100 :check "check"}))
                 "TIMEOUT")
  => true)

^{:refer code.test.base.print/print-failed :added "4.1"}
(fact "prints failed info"
  (str/includes? (h/with-out-str
                   (print-failed {:name "test" :actual {:data 1} :check "check" :checker (fn [x] false)}))
                 "FAILED")
  => true)

^{:refer code.test.base.print/print-fact :added "3.0"}
(fact "outputs the description for a fact form that contains many statements")

^{:refer code.test.base.print/print-summary :added "3.0"}
(fact "outputs the description for an entire test run")
