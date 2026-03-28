(ns std.lang.base.grammar-xtalk-ops-test
  (:require [std.lang.base.grammar-xtalk :refer :all])
  (:use code.test))

;; generated from xtalk_ops.edn

^{:refer std.lang.base.grammar-xtalk/tf-bit-and, :added "4.0"}
(fact "bit and transform")

^{:refer std.lang.base.grammar-xtalk/tf-bit-lshift, :added "4.0"}
(fact "bit left shift transform")

^{:refer std.lang.base.grammar-xtalk/tf-bit-or, :added "4.0"}
(fact "bit or transform")

^{:refer std.lang.base.grammar-xtalk/tf-bit-rshift, :added "4.0"}
(fact "bit right shift transform")

^{:refer std.lang.base.grammar-xtalk/tf-bit-xor, :added "4.0"}
(fact "bit xor transform")

^{:refer std.lang.base.grammar-xtalk/tf-throw, :added "4.0"}
(fact "wrapper for throw transform")

^{:refer std.lang.base.grammar-xtalk/tf-copy-key, :added "4.0"}
(fact "copy-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-del-key, :added "4.0"}
(fact "del-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-get-key, :added "4.0"}
(fact "get-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-get-key, :added "4.0"}
(fact "get-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-get-path, :added "4.0"}
(fact "get-in transform")

^{:refer std.lang.base.grammar-xtalk/tf-has-key?, :added "4.0"}
(fact "has key default transform")

^{:refer std.lang.base.grammar-xtalk/tf-eq-nil?, :added "4.0"}
(fact "equals nil transform")

^{:refer std.lang.base.grammar-xtalk/tf-not-nil?, :added "4.0"}
(fact "not nil transform")

^{:refer std.lang.base.grammar-xtalk/tf-offset, :added "4.0"}
(fact "gets the offset")

^{:refer std.lang.base.grammar-xtalk/tf-offset-len, :added "4.0"}
(fact "gets the length offset")

^{:refer std.lang.base.grammar-xtalk/tf-offset-rev, :added "4.0"}
(fact "gets the reverse offset")

^{:refer std.lang.base.grammar-xtalk/tf-offset-rlen, :added "4.0"}
(fact "gets the reverse length offset")

^{:refer std.lang.base.grammar-xtalk/tf-set-key, :added "4.0"}
(fact "set-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-set-key, :added "4.0"}
(fact "set-key transform")

^{:refer std.lang.base.grammar-xtalk/tf-global-del, :added "4.0"}
(fact "default global del transform")

^{:refer std.lang.base.grammar-xtalk/tf-global-has?, :added "4.0"}
(fact "default global has transform")

^{:refer std.lang.base.grammar-xtalk/tf-global-set, :added "4.0"}
(fact "default global set transform")

^{:refer std.lang.base.grammar-xtalk/tf-lu-eq, :added "4.0"}
(fact "lookup equals transform")

^{:refer std.lang.base.grammar-xtalk/tf-proto-create, :added "4.0"}
(fact "creates the prototype map")
