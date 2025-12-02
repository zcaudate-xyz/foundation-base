(ns code.framework.common-test
  (:use code.test)
  (:require [code.framework.common :refer :all]
            [code.framework.test.clojure]
            [code.framework.test.fact]
            [code.edit :as nav]
            [code.query :as query]
            [std.block :as block]))

(require 'code.framework.test.fact :reload)

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

  (let [code "^{:refer code.manage/import :added \"3.0\"}
              (fact \"imports a map\"
                (import {:write true}) => nil)"]
    (analyse-test :fact (nav/parse-root code)))
  => (contains {'code.manage (contains {'import (contains {:intro "imports a map"})})}))

(fact "debug analyse-test logic"
  (let [code "^{:refer code.manage/import :added \"3.0\"}
              (fact \"imports a map\"
                (import {:write true}) => nil)"
        nav (nav/parse-root code)
        fns (query/$* (nav/down nav) ['(#{fact comment} | & _)] {:return :zipper :walk :top})
        facts (keep code.framework.test.fact/gather-fact fns)]
    (-> (first facts) :intro))
  => "imports a map")


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
(fact "creates a function lookup for the project"
  (line-lookup 'code.manage
               {'code.manage {'a {:source {:line {:row 10 :end-row 12}}}
                              'b {:test {:line {:row 20 :end-row 22}}}}})
  => {10 'a 11 'a 12 'a
      19 'b 20 'b 21 'b 22 'b})

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
