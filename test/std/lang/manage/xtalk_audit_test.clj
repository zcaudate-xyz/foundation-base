(ns std.lang.manage.xtalk-audit-test
  (:use code.test)
  (:require [std.lang.manage.xtalk-audit :refer :all]))

^{:refer std.lang.manage.xtalk-audit/xtalk-categories :added "4.1"}
(fact "returns xtalk categories"
  (vector? (xtalk-categories))
  => true)

^{:refer std.lang.manage.xtalk-audit/xtalk-op-map :added "4.1"}
(fact "returns op map keyed by op"
  (map? (xtalk-op-map))
  => true)

^{:refer std.lang.manage.xtalk-audit/xtalk-symbols :added "4.1"}
(fact "returns xtalk symbols vector"
  (vector? (xtalk-symbols))
  => true)

^{:refer std.lang.manage.xtalk-audit/installed-languages :added "4.1"}
(fact "returns installed languages vector"
  (vector? (installed-languages))
  => true)

^{:refer std.lang.manage.xtalk-audit/xtalk-parent-languages :added "4.1"}
(fact "returns xtalk parent languages vector"
  (vector? (xtalk-parent-languages))
  => true)

^{:refer std.lang.manage.xtalk-audit/audit-languages :added "4.1"}
(fact "returns audit language selection"
  (vector? (audit-languages))
  => true)

^{:refer std.lang.manage.xtalk-audit/feature-status :added "4.1"}
(fact "feature-status returns known state keyword"
  (keyword? (feature-status :js 'x:get-key))
  => true)

^{:refer std.lang.manage.xtalk-audit/support-matrix :added "4.1"}
(fact "support-matrix returns expected map keys"
  (-> (support-matrix)
      (keys)
      set)
  => #{:languages :features :status :summary})

^{:refer std.lang.manage.xtalk-audit/missing-by-language :added "4.1"}
(fact "missing-by-language returns map"
  (map? (missing-by-language))
  => true)

^{:refer std.lang.manage.xtalk-audit/missing-by-feature :added "4.1"}
(fact "missing-by-feature returns map"
  (map? (missing-by-feature))
  => true)

^{:refer std.lang.manage.xtalk-audit/visualize-support :added "4.1"}
(fact "visualize-support returns printable output"
  (string? (visualize-support))
  => true)