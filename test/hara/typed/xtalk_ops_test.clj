tahto/typed/xtalk_ops_test.clj:1:(ns tahto.typed.xtalk-ops-test
  (:use code.test)
tahto/typed/xtalk_ops_test.clj:3:  (:require [tahto.typed.xtalk-common :as types]
tahto/typed/xtalk_ops_test.clj:4:            [tahto.typed.xtalk-ops :refer :all]))

tahto/typed/xtalk_ops_test.clj:6:^{:refer tahto.typed.xtalk-ops/op-table-vars :added "4.1"}
(fact "finds grammar op table vars"
tahto/typed/xtalk_ops_test.clj:8:  (pos? (count (op-table-vars 'tahto.common.grammar-spec)))
  => true)

tahto/typed/xtalk_ops_test.clj:11:^{:refer tahto.typed.xtalk-ops/op-entries :added "4.1"}
(fact "collects builtin op entries"
  (boolean (some #(contains? (:symbol %) 'x:get-key) (op-entries)))
  => true)

tahto/typed/xtalk_ops_test.clj:16:^{:refer tahto.typed.xtalk-ops/canonical-symbol-from-entry :added "4.1"}
(fact "prefers canonical x symbols from op entries"
  (canonical-symbol-from-entry {:emit :alias
                                :raw 'xt.lang.common-lib/get-key
                                :symbol #{'xt.lang.common-lib/get-key 'x:get-key}})
  => 'x:get-key)

tahto/typed/xtalk_ops_test.clj:23:^{:refer tahto.typed.xtalk-ops/builtin-entry :added "4.1"}
(fact "looks up builtin entries"
  [(contains? (builtin-entry 'x:get-key) :symbol)
   (contains? (builtin-entry 'if) :symbol)]
  => [true true])

tahto/typed/xtalk_ops_test.clj:29:^{:refer tahto.typed.xtalk-ops/canonical-entry :added "4.1"}
(fact "augments entries with canonical symbol"
  (:canonical-symbol (canonical-entry 'x:get-key))
  => 'x:get-key)

tahto/typed/xtalk_ops_test.clj:34:^{:refer tahto.typed.xtalk-ops/canonical-symbol :added "4.1"}
(fact "returns canonical builtin symbols when indexed"
  [(canonical-symbol 'x:get-key)
   (canonical-symbol 'xt.lang.common-lib/get-key)
   (canonical-symbol 'xt.lang.common-lib/len)]
  => '[x:get-key x:get-key x:len])

tahto/typed/xtalk_ops_test.clj:41:^{:refer tahto.typed.xtalk-ops/op-arglists :added "4.1"}
(fact "exposes builtin op arglists from op-spec"
  (op-arglists (canonical-entry 'x:get-key))
  => '([obj key] [obj key default]))

tahto/typed/xtalk_ops_test.clj:46:^{:refer tahto.typed.xtalk-ops/op-type-forms :added "4.1"}
(fact "extracts singular and plural type forms from op-spec"
  [(op-type-forms (canonical-entry 'x:add))
   (op-type-forms {:op-spec {:types [[:fn [:xt/num] :xt/num]
                                     [:fn [:xt/str] :xt/str]]}})
   (op-type-forms {:op-spec {:arglists '([value])}})
   (op-type-forms {})]
  => '([[:fn [:xt/num :xt/num] :xt/num]]
        [[:fn [:xt/num] :xt/num]
         [:fn [:xt/str] :xt/str]]
        []
        []))

tahto/typed/xtalk_ops_test.clj:59:^{:refer tahto.typed.xtalk-ops/op-types :added "4.1"}
(fact "normalizes builtin op types from op-spec"
  (mapv types/type->data (op-types (canonical-entry 'x:add)))
  => '[{:kind :fn
         :inputs [{:kind :primitive :name :xt/num}
                  {:kind :primitive :name :xt/num}]
         :output {:kind :primitive :name :xt/num}}])

tahto/typed/xtalk_ops_test.clj:67:^{:refer tahto.typed.xtalk-ops/builtin-type :added "4.1"}
(fact "returns callable builtin types when op-spec declares them"
  (types/type->data (builtin-type 'x:len))
  => '{:kind :fn
       :inputs [{:kind :primitive :name :xt/any}]
       :output {:kind :primitive :name :xt/int}})

tahto/typed/xtalk_ops_test.clj:74:^{:refer tahto.typed.xtalk-ops/builtin? :added "4.1"}
(fact "detects builtin operators"
  [(builtin? 'x:get-key)
   (builtin? 'sample.route/missing)]
  => [true false])