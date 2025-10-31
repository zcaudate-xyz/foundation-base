(ns code.framework.common-test
  (:use code.test)
  (:require [code.framework.common :refer :all]
            [code.framework.test.clojure]
            [code.framework.test.fact]
            [code.edit :as nav]
            [std.block :as block]))

^{:refer code.framework.common/display-entry :added "3.0"}
(fact "creates a map represenation of the entry"

  (display-entry {:ns {:a {:test {}
                           :source {}}
                       :b {:test {}}
                       :c {:source {}}}})
  => {:source {:ns [:a :c]}
      :test {:ns [:a :b]}})

^{:refer code.framework.common/entry :added "3.0"}
(fact "creates an entry for analysis"

  (entry {:ns {:a {:test {}
                   :source {}}
               :b {:test {}}
               :c {:source {}}}})
  ;;#code{:source {:ns [:a :c]}, :test {:ns [:a :b]}}
  => code.framework.common.Entry)

^{:refer code.framework.common/entry? :added "3.0"}
(fact "checks if object is an entry"

  (entry? (entry {}))
  => true)

^{:refer code.framework.common/test-frameworks :added "3.0"}
(fact "lists the framework that a namespace uses"

  (test-frameworks 'code.test) => :fact

  (test-frameworks 'clojure.test) => :clojure)

^{:refer code.framework.common/analyse-test :added "3.0"}
(fact "serves as the entry point for analyzing test code, taking a test framework keyword and a parsed code structure"

  (analyse-test :fact
                (nav/parse-root (slurp "test/code/framework_test.clj"))))

^{:refer code.framework.common/gather-meta :added "3.0"}
(fact "gets the metadata for a particular form"
  (-> (nav/parse-string "^{:refer clojure.core/+ :added \"1.1\"}\n(fact ...)")
      nav/down nav/right nav/down
      gather-meta)
  => '{:added "1.1", :ns clojure.core, :var +, :refer clojure.core/+})

^{:refer code.framework.common/gather-string :added "3.0"}
(fact "creates correctly spaced code string from normal docstring"

  (-> (nav/parse-string "\"hello\nworld\nalready\"")
      (gather-string)
      (block/string))
  => "\"hello\n  world\n  already\"")

^{:refer code.framework.common/line-lookup :added "3.0"}
(fact "creates a function lookup for the project")

(comment
  (code.manage/import {:write true})
  (./run '[code.manage])
  (def a (nav/parse-string "\"hello\n\""))
  (block/height (block/block "\n"))

  (block/value (block/block "\n"))

  (block/string (block/block "\\n"))

  (seq (std.string/split-lines "\n\n"))
  (block/value (nav/block a))
  (block/string (nav/block a))
  "\"hello\\n\""
  "hello\n"
  9)
