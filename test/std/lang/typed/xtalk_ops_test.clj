(ns std.lang.typed.xtalk-ops-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-ops :refer :all]))

^{:refer std.lang.typed.xtalk-ops/op-table-vars :added "4.1"}
(fact "finds grammar op table vars"
  (pos? (count (op-table-vars 'std.lang.base.grammar-spec)))
  => true)

^{:refer std.lang.typed.xtalk-ops/op-entries :added "4.1"}
(fact "collects builtin op entries"
  (boolean (some #(contains? (:symbol %) 'x:get-key) (op-entries)))
  => true)

^{:refer std.lang.typed.xtalk-ops/canonical-symbol-from-entry :added "4.1"}
(fact "prefers canonical x symbols from op entries"
  (canonical-symbol-from-entry {:emit :alias
                                :raw 'xt.lang.base-lib/get-key
                                :symbol #{'xt.lang.base-lib/get-key 'x:get-key}})
  => 'xt.lang.base-lib/get-key)

^{:refer std.lang.typed.xtalk-ops/builtin-entry :added "4.1"}
(fact "looks up builtin entries"
  (contains? (builtin-entry 'x:get-key) :symbol)
  => true)

^{:refer std.lang.typed.xtalk-ops/canonical-entry :added "4.1"}
(fact "augments entries with canonical symbol"
  (:canonical-symbol (canonical-entry 'x:get-key))
  => 'x:get-key)

^{:refer std.lang.typed.xtalk-ops/canonical-symbol :added "4.1"}
(fact "returns canonical builtin symbols when indexed"
  [(canonical-symbol 'x:get-key)
   (canonical-symbol 'xt.lang.base-lib/get-key)]
  => '[x:get-key xt.lang.base-lib/get-key])

^{:refer std.lang.typed.xtalk-ops/builtin? :added "4.1"}
(fact "detects builtin operators"
  [(builtin? 'x:get-key)
   (builtin? 'sample.route/missing)]
  => [true false])
