(ns code.test.manage-test
  (:require [code.test.manage :refer :all]
            [code.test :refer [fact]]))

^{:refer code.test.manage/fact:global-map :added "3.0"}
(fact "sets and gets the global map"

  (fact:global-map *ns* {}))

^{:refer code.test.manage/fact:global-fn :added "3.0"}
(fact "global getter and setter"

  (fact:global-fn :get []))

^{:refer code.test.manage/fact:global :added "3.0"}
(fact "fact global getter and setter"

  (fact:global))

^{:refer code.test.manage/fact:ns-load :added "3.0"}
(fact "loads a test namespace")

^{:refer code.test.manage/fact:ns-unload :added "3.0"}
(fact "unloads a test namespace")

^{:refer code.test.manage/fact:ns-alias :added "3.0"}
(fact "imports all aliases into current namespace")

^{:refer code.test.manage/fact:ns-unalias :added "3.0"}
(fact "removes all aliases from current namespace")

^{:refer code.test.manage/fact:ns-intern :added "3.0"}
(fact "imports all interns into current namespace")

^{:refer code.test.manage/fact:ns-unintern :added "3.0"}
(fact "removes all interns into current namespace")

^{:refer code.test.manage/fact:ns-import :added "3.0"}
(fact "loads, imports and aliases current namespace")

^{:refer code.test.manage/fact:ns-unimport :added "3.0"}
(fact "unload, unimports and unalias current namespace")

^{:refer code.test.manage/fact:ns-fn :added "3.0"}
(fact "fact ns getter and setter")

^{:refer code.test.manage/fact:ns :added "3.0"}
(fact "provides a macro for managing namespace-related operations within facts")
